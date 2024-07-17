package sg.edu.np.mad.TicketFinder;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;
import android.Manifest;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private RecyclerView chatRecyclerView;
    private RecyclerView suggestedPromptsRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private Button exitButton;
    private ChatAdapter chatAdapter;
    private SuggestedPromptAdapter suggestedPromptAdapter;
    private ArrayList<Message> messageList;
    private dbHandler dbHandler;
    private HashMap<String, String> chatbotResponsesMap;
    private static final int LEVENSHTEIN_THRESHOLD = 3; // Adjusted threshold for better matching
    private static final int CONFIDENCE_THRESHOLD = 2; // Threshold for structured questions
    private ImageView micIcon; // For mic implementation
    private SpeechRecognizerHelper speechRecognizerHelper; // For mic implementation
    private ArrayList<Event> eventList = new ArrayList<>(); // For event data

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.recyclerView);
        suggestedPromptsRecyclerView = findViewById(R.id.suggestedPromptsRecyclerView);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        exitButton = findViewById(R.id.exitButton);
        micIcon = findViewById(R.id.micIcon); // For mic implementation

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        suggestedPromptAdapter = new SuggestedPromptAdapter(new ArrayList<>(), this::onSuggestedPromptClick);
        suggestedPromptsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedPromptsRecyclerView.setAdapter(suggestedPromptAdapter);

        dbHandler = new dbHandler();
        chatbotResponsesMap = new HashMap<>();

        // Initialize SpeechRecognizerHelper
        speechRecognizerHelper = new SpeechRecognizerHelper(this, micIcon, text -> messageInput.setText(text)); // For mic implementation

        micIcon.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                speechRecognizerHelper.startListening(); // For mic implementation
            } else {
                ActivityCompat.requestPermissions(ChatActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION); // For mic implementation
            }
        });

        // Fetch chatbot responses from Firestore and store them in a map
        dbHandler.getChatbotResponses(list -> {
            if (list != null) {
                for (ChatbotResponse response : list) {
                    if (response != null && response.getQuestion() != null && response.getAnswer() != null) {
                        String cleanedQuestion = cleanText(response.getQuestion());
                        String cleanedAnswer = cleanText(response.getAnswer());
                        chatbotResponsesMap.put(cleanedQuestion, cleanedAnswer);
                    }
                }
                synchronized (dbHandler) {
                    dbHandler.notifyAll(); // Notify that the data is ready
                }
            }
        });

        // Fetch event data from Firestore
        dbHandler.getData(list -> {
            if (list != null) {
                eventList = list;
            }
        });

        // Add a greeting message to the message list
        messageList.add(new Message("Hello! How can I assist you today?", false));
        chatAdapter.notifyDataSetChanged();

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString();
            if (!TextUtils.isEmpty(messageText)) {
                messageList.add(new Message(messageText, true));
                messageInput.setText("");
                chatAdapter.notifyDataSetChanged();

                handleBotResponse(messageText);
            }
        });

        exitButton.setOnClickListener(v -> finish()); // Close the activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizerHelper.destroy(); // For mic implementation
    }

    private void handleBotResponse(String messageText) {
        String cleanedMessageText = cleanText(messageText);
        String correctedMessageText = correctSpelling(cleanedMessageText);

        // First, check for artist matches
        ArrayList<Event> matchedEvents = findEventsByArtist(correctedMessageText);
        if (!matchedEvents.isEmpty()) {
            String artistName = matchedEvents.get(0).getArtist();
            Log.d("ChatActivity", "Artist detected: " + artistName);
            messageList.add(new Message("Here are the event details with the artist " + artistName + ". Click on the event card to find out more.", false));
            chatAdapter.notifyDataSetChanged(); // Notify adapter to refresh
            showMatchingEvents(matchedEvents);
            return; // Exit the method after displaying events
        } else {
            Log.d("ChatActivity", "No artist detected.");
        }

        // If no artist matches, proceed with usual chatbot responses
        if (isGibberish(correctedMessageText)) {
            Log.d("ChatActivity", "Message is gibberish, showing suggested prompts.");
            messageList.add(new Message("I'm not sure how to respond to that. Here are some suggestions:", false));
            showSuggestedPrompts();
        } else {
            String response = getBestResponse(correctedMessageText);
            if (response != null) {
                Log.d("ChatActivity", "Database response found.");
                response = capitalizeFirstLetter(response); // Capitalize the first letter of the response
                messageList.add(new Message(response, false));
                hideSuggestedPrompts();
            } else {
                if (!correctedMessageText.equals(cleanedMessageText)) {
                    Log.d("ChatActivity", "Corrected message text: " + correctedMessageText);
                    messageList.add(new Message("Did you mean: " + correctedMessageText + "?", false));
                    String correctedResponse = getBestResponse(correctedMessageText);
                    if (correctedResponse != null) {
                        correctedResponse = capitalizeFirstLetter(correctedResponse);
                        messageList.add(new Message(correctedResponse, false));
                        hideSuggestedPrompts();
                    } else {
                        messageList.add(new Message("I'm not sure how to respond to that.", false));
                        showSuggestedPrompts();
                    }
                } else {
                    messageList.add(new Message("I'm not sure how to respond to that.", false));
                    showSuggestedPrompts();
                }
            }
        }
        chatAdapter.notifyDataSetChanged();
    }

    private String correctSpelling(String input) {
        Set<String> dictionary = dbHandler.getDictionary(); // Ensure the dictionary is loaded
        String[] words = input.split("\\s+");
        StringBuilder correctedInput = new StringBuilder();
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (String word : words) {
            String correctedWord = word;
            int minDistance = Integer.MAX_VALUE;

            for (String dictWord : dictionary) {
                int distance = levenshtein.apply(word, dictWord);
                if (distance < minDistance) {
                    minDistance = distance;
                    correctedWord = dictWord;
                }
            }

            // Only replace the word if the minimum distance is within the threshold
            if (minDistance <= LEVENSHTEIN_THRESHOLD) {
                correctedInput.append(correctedWord).append(" ");
            } else {
                correctedInput.append(word).append(" ");
            }
        }

        return correctedInput.toString().trim();
    }

    private boolean isGibberish(String messageText) {
        Set<String> dictionary = dbHandler.getDictionary();
        String[] words = messageText.split("\\s+");
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int gibberishCount = 0;

        for (String word : words) {
            int minDistance = Integer.MAX_VALUE;
            for (String dictWord : dictionary) {
                int distance = levenshtein.apply(word, dictWord);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
            if (minDistance > LEVENSHTEIN_THRESHOLD) {
                gibberishCount++;
            }
        }

        return gibberishCount >= (words.length * 0.75); // If 75% or more words are considered gibberish
    }

    private void showSuggestedPrompts() {
        List<String> suggestions = new ArrayList<>(chatbotResponsesMap.keySet());
        suggestedPromptAdapter.updateData(suggestions);
        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideSuggestedPrompts() {
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

    private void showMatchingEvents(ArrayList<Event> events) {
        for (Event event : events) {
            messageList.add(new Message(event)); // Add event as a message
        }
        chatAdapter.notifyDataSetChanged(); // Notify adapter to refresh
    }

    private void onSuggestedPromptClick(String prompt) {
        messageInput.setText(prompt);
        messageInput.setSelection(prompt.length());
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

    private String getBestResponse(String messageText) {
        String bestResponse = null;
        double bestMatchScore = Double.MAX_VALUE; // Lower score means better match
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (HashMap.Entry<String, String> entry : chatbotResponsesMap.entrySet()) {
            String question = entry.getKey();
            String answer = entry.getValue();

            double questionMatchScore = levenshtein.apply(messageText, question) / (double) Math.max(messageText.length(), question.length());
            double answerMatchScore = levenshtein.apply(messageText, answer) / (double) Math.max(messageText.length(), answer.length());

            if (questionMatchScore < bestMatchScore) {
                bestMatchScore = questionMatchScore;
                bestResponse = answer;
            }

            if (answerMatchScore < bestMatchScore) {
                bestMatchScore = answerMatchScore;
                bestResponse = answer;
            }
        }

        return bestMatchScore < LEVENSHTEIN_THRESHOLD ? bestResponse : null;
    }

    private String cleanText(String text) {
        return text.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().trim();
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private ArrayList<Event> findEventsByArtist(String inputText) {
        ArrayList<Event> matchedEvents = new ArrayList<>();
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        String cleanedInputText = cleanText(inputText);

        Log.d("ChatActivity", "Cleaned input text for artist search: " + cleanedInputText);

        for (Event event : eventList) {
            if (event.getArtist() != null) {
                String cleanedArtistName = cleanText(event.getArtist());
                int distance = levenshtein.apply(cleanedInputText, cleanedArtistName);

                Log.d("ChatActivity", "Comparing with artist: " + cleanedArtistName + ", Distance: " + distance);

                // Adjusted condition for better matching
                if (cleanedArtistName.contains(cleanedInputText) || cleanedInputText.contains(cleanedArtistName)) {
                    Log.d("ChatActivity", "Artist match found: " + event.getArtist());
                    matchedEvents.add(event);
                }
            }
        }
        return matchedEvents;
    }
}
