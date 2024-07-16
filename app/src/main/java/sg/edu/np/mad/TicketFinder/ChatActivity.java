package sg.edu.np.mad.TicketFinder;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
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
    private static final int LEVENSHTEIN_THRESHOLD = 3; // Threshold for Levenshtein Distance
    private static final int CONFIDENCE_THRESHOLD = 2; // Threshold for structured questions
    private ImageView micIcon; // CHANGES MADE HERE FOR MIC IMPLEMENTATION
    private SpeechRecognizerHelper speechRecognizerHelper; // CHANGES MADE HERE FOR MIC IMPLEMENTATION

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.recyclerView);
        suggestedPromptsRecyclerView = findViewById(R.id.suggestedPromptsRecyclerView);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        exitButton = findViewById(R.id.exitButton);
        micIcon = findViewById(R.id.micIcon); // CHANGES MADE HERE FOR MIC IMPLEMENTATION

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        suggestedPromptAdapter = new SuggestedPromptAdapter(new ArrayList<>(), this::onSuggestedPromptClick);
        suggestedPromptsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedPromptsRecyclerView.setAdapter(suggestedPromptAdapter);

        dbHandler = new dbHandler();
        chatbotResponsesMap = new HashMap<>();

        // Initialize SpeechRecognizerHelper
        speechRecognizerHelper = new SpeechRecognizerHelper(this, micIcon, new SpeechRecognizerHelper.OnSpeechResultListener() {
            @Override
            public void onSpeechResult(String text) {
                messageInput.setText(text);
            }
        }); // CHANGES MADE HERE FOR MIC IMPLEMENTATION

        micIcon.setOnClickListener(new View.OnClickListener() { // CHANGES MADE HERE FOR MIC IMPLEMENTATION
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    speechRecognizerHelper.startListening(); // CHANGES MADE HERE FOR MIC IMPLEMENTATION
                } else {
                    ActivityCompat.requestPermissions(ChatActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION); // CHANGES MADE HERE FOR MIC IMPLEMENTATION
                }
            }
        });

        // Fetch chatbot responses from Firestore and store them in a map
        dbHandler.getChatbotResponses(new FirestoreCallback<ChatbotResponse>() {
            @Override
            public void onCallback(ArrayList<ChatbotResponse> list) {
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
            }
        });

        // Add a greeting message to the message list
        messageList.add(new Message("Hello! How can I assist you today?", false));
        chatAdapter.notifyDataSetChanged();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageInput.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
                    messageList.add(new Message(messageText, true));
                    messageInput.setText("");
                    chatAdapter.notifyDataSetChanged();

                    handleBotResponse(messageText);
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    @Override
    protected void onDestroy() { // CHANGES MADE HERE FOR MIC IMPLEMENTATION
        super.onDestroy();
        speechRecognizerHelper.destroy(); // CHANGES MADE HERE FOR MIC IMPLEMENTATION
    }

    private void handleBotResponse(String messageText) {
        String cleanedMessageText = cleanText(messageText);
        String correctedMessageText = correctSpelling(cleanedMessageText);
        String response = getBestResponse(cleanedMessageText);

        if (response != null) {
            response = capitalizeFirstLetter(response); // Capitalize the first letter of the response
            messageList.add(new Message(response, false));
            hideSuggestedPrompts();
        } else {
            if (!correctedMessageText.equals(cleanedMessageText)) {
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
        chatAdapter.notifyDataSetChanged();
    }


    private boolean needsCorrection(String messageText) {
        // Implement a simple heuristic to determine if the question is poorly structured
        // You can extend this logic as needed
        return messageText.split("\\s+").length <= CONFIDENCE_THRESHOLD;
    }

    private void showSuggestedPrompts() {
        List<String> suggestions = new ArrayList<>(chatbotResponsesMap.keySet());
        suggestedPromptAdapter.updateData(suggestions);
        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideSuggestedPrompts() {
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

    private void onSuggestedPromptClick(String prompt) {
        messageInput.setText(prompt);
        messageInput.setSelection(prompt.length());
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

    private String getBestResponse(String messageText) {
        String bestResponse = null;
        int bestMatchScore = 0;

        for (HashMap.Entry<String, String> entry : chatbotResponsesMap.entrySet()) {
            String question = entry.getKey();
            String answer = entry.getValue();

            int questionMatchScore = getMatchScore(messageText, question);
            int answerMatchScore = getMatchScore(messageText, answer);

            if (questionMatchScore > bestMatchScore) {
                bestMatchScore = questionMatchScore;
                bestResponse = answer;
            }

            if (answerMatchScore > bestMatchScore) {
                bestMatchScore = answerMatchScore;
                bestResponse = answer;
            }
        }

        return bestMatchScore > 0 ? bestResponse : null;
    }

    private String correctSpelling(String input) {
        Set<String> dictionary;
        synchronized (dbHandler) {
            dictionary = dbHandler.getDictionary(); // Use dynamic dictionary
            if (dictionary.isEmpty()) {
                try {
                    dbHandler.wait(); // Wait until dictionary is populated
                    dictionary = dbHandler.getDictionary();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        String[] words = input.split("\\s+");
        StringBuilder correctedInput = new StringBuilder();

        for (String word : words) {
            String correctedWord = correctWord(word);
            correctedInput.append(correctedWord).append(" ");
        }

        return correctedInput.toString().trim();
    }

    private String correctWord(String word) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        String closestWord = word;
        int minDistance = Integer.MAX_VALUE;

        for (String dictWord : dbHandler.getDictionary()) {
            int distance = levenshtein.apply(word, dictWord);
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = dictWord;
            }
        }

        // Only return the corrected word if the distance is within the threshold
        return minDistance <= LEVENSHTEIN_THRESHOLD ? closestWord : word;
    }

    private int getMatchScore(String messageText, String text) {
        // Normalize both messageText and text by removing punctuation and converting to lower case
        String cleanedMessageText = cleanText(messageText);
        String cleanedText = cleanText(text);

        String[] messageWords = cleanedMessageText.split("\\s+");
        String[] textWords = cleanedText.split("\\s+");
        int matchCount = 0;

        for (String messageWord : messageWords) {
            for (String textWord : textWords) {
                if (messageWord.equalsIgnoreCase(textWord)) {
                    matchCount++;
                }
            }
        }

        return matchCount;
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
}