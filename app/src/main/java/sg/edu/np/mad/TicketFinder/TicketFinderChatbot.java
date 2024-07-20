package sg.edu.np.mad.TicketFinder;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.text.Normalizer;
import java.util.regex.Pattern;
import android.Manifest;

public class TicketFinderChatbot extends AppCompatActivity {
    RecyclerView chatRecyclerView;
    RecyclerView suggestedPromptsRecyclerView;
    EditText messageInput;
    ImageView sendButton;
    ArrayList<Message> messageList;
    ChatAdapter chatAdapter;
    SuggestedPromptAdapter suggestedPromptAdapter;
    FirebaseFirestore firestoreDb;
    FirebaseSmartReply firebaseSmartReply;

    private final Map<String, String> keywordToDocIdMap = new HashMap<>();
    private final Map<String, String> chatbotResponsesMap = new HashMap<>();
    private final List<String> questionsList = new ArrayList<>();
    private ArrayList<Event> eventsList = new ArrayList<>(); // Store events
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private ImageView micIcon;
    private SpeechRecognizerHelper speechRecognizerHelper;
    private EventAdapter eventAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_finder_chat);
        chatRecyclerView = findViewById(R.id.chat_recycler);
        suggestedPromptsRecyclerView = findViewById(R.id.suggested_prompts_recycler);
        messageInput = findViewById(R.id.message_input_field);
        sendButton = findViewById(R.id.send_button);
        micIcon = findViewById(R.id.mic_button);
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        suggestedPromptAdapter = new SuggestedPromptAdapter(new ArrayList<>(), this::onSuggestedPromptClick);
        eventAdapter = new EventAdapter(this, new ArrayList<>(), false); // ******** changes made here

        firestoreDb = FirebaseFirestore.getInstance();
        firebaseSmartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager promptLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        suggestedPromptsRecyclerView.setAdapter(suggestedPromptAdapter);
        suggestedPromptsRecyclerView.setLayoutManager(promptLayoutManager);

        // Initialize SpeechRecognizerHelper
        speechRecognizerHelper = new SpeechRecognizerHelper(this, micIcon, new SpeechRecognizerHelper.OnSpeechResultListener() {
            @Override
            public void onSpeechResult(String text) {
                messageInput.setText(text);
            }
        });

        // MIC IMPLEMENTATION
        micIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(TicketFinderChatbot.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    speechRecognizerHelper.startListening();
                } else {
                    ActivityCompat.requestPermissions(TicketFinderChatbot.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                }
            }
        });

        fetchQuestionsFromFirestore();
        fetchEventsFromFirestore();
        showGreetingMsg();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = messageInput.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    messageList.add(new Message(userMessage, true));
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    handleUserMessage(userMessage);
                    messageInput.setText("");
                }
            }
        });

        ImageView closeChatButton = findViewById(R.id.close_button);
        closeChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizerHelper.destroy();
    }

    private void fetchQuestionsFromFirestore() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                firestoreDb.collection("FAQ").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String docId = document.getId();
                                String keywordString = document.getString("Keywords");
                                String response = document.getString("Answer");
                                String question = document.getString("Question");
                                if (keywordString != null && response != null && question != null) {
                                    String[] keywords = keywordString.split(" ");
                                    for (String keyword : keywords) {
                                        keywordToDocIdMap.put(keyword.toLowerCase(), docId);
                                        chatbotResponsesMap.put(keyword.toLowerCase(), response);
                                    }
                                    questionsList.add(question);
                                }
                            }
                            Log.d("fetchQuestions", "Questions fetched successfully: " + questionsList);
                        } else {
                            Log.w("fetchQuestions", "Error getting documents.", task.getException());
                        }
                    }
                });
            }
        });
    }


    private void fetchEventsFromFirestore() {
        dbHandler db = new dbHandler();
        db.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> list) {
                eventsList = list; // Populate eventsList with the fetched data
                for (Event event : eventsList) {
                    Log.d("fetchEventsFromFirestore", "Artist: " + event.getArtist());
                }
                Log.d("fetchEvents", "Events fetched successfully: " + eventsList.size());
            }
        });
    }

    private void showGreetingMsg() {
        String welcomeMessage = "Hi my name is Joe Yi! Feel free to ask me any questions related to events!";
        messageList.add(new Message(welcomeMessage, false));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        showSuggestedPrompts();
    }

    private void handleUserMessage(String message) {
        Log.d("handleUserMessage", "Received message: " + message);

        String cleanedMessage = cleanMessage(message);
        // Check for common greetings first
        if (isGreeting(cleanedMessage)) {
            String greetingResponse = getGreetingResponse();
            messageList.add(new Message(greetingResponse, false));
            chatAdapter.notifyDataSetChanged();
            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            hideSuggestedPrompts();
            return;
        }

        // Check for event titles or artist names
        if (checkForArtistMatch(cleanedMessage)) {
            hideSuggestedPrompts();
            return;
        }

        String closestKeyword = findClosestKeyword(cleanedMessage);
        Log.d("handleUserMessage", "Closest keyword: " + closestKeyword);
        if (closestKeyword != null) {
            String documentId = keywordToDocIdMap.get(closestKeyword);
            Log.d("handleUserMessage", "Fetching answer for document ID: " + documentId);
            fetchFaqAnswer(documentId);
        } else {
            generateSmartReply(cleanedMessage);
        }
    }

    private boolean checkForArtistMatch(String message) {
        String[] userWords = message.split(" ");
        for (Event event : eventsList) {
            String artist = event.getArtist().toLowerCase();

            // Check for full artist name match
            for (String userWord : userWords) {
                if (getLevenshteinDistance(userWord.toLowerCase(), artist) <= 1) {
                    messageList.add(new Message(event));
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    return true;
                }
            }

            // Check for partial artist name match (two words from user match with artist name)
            for (int i = 0; i < userWords.length - 1; i++) {
                String combinedUserWords = userWords[i].toLowerCase() + " " + userWords[i + 1].toLowerCase();
                if (getLevenshteinDistance(combinedUserWords, artist) <= 2) {
                    messageList.add(new Message(event));
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    return true;
                }
            }
        }
        return false;
    }

    private void generateSmartReply(String message) {
        List<FirebaseTextMessage> conversation = new ArrayList<>();
        for (Message msg : messageList) {
            if (msg.isUser()) {
                conversation.add(FirebaseTextMessage.createForLocalUser(msg.getMessage(), System.currentTimeMillis()));
            } else {
                conversation.add(FirebaseTextMessage.createForRemoteUser(msg.getMessage(), System.currentTimeMillis(), "bot"));
            }
        }

        firebaseSmartReply.suggestReplies(conversation)
                .addOnCompleteListener(new OnCompleteListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SmartReplySuggestionResult> task) {
                        if (task.isSuccessful()) {
                            SmartReplySuggestionResult result = task.getResult();
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                messageList.add(new Message("I don't understand the question.", false));
                                showSuggestedPrompts();
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
                                if (!suggestions.isEmpty()) {
                                    String reply = suggestions.get(0).getText();
                                    if (isGenericOrEmojiReply(reply)) {
                                        messageList.add(new Message("I don't understand the question.", false));
                                        showSuggestedPrompts();
                                    } else {
                                        messageList.add(new Message(reply, false));
                                        hideSuggestedPrompts();
                                    }
                                    Log.d("SmartReply", "Reply: " + reply);
                                } else {
                                    messageList.add(new Message("I don't understand the question.", false));
                                    Log.d("SmartReply", "No suggestions available.");
                                    showSuggestedPrompts();
                                }
                            }
                            chatAdapter.notifyDataSetChanged();
                            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        } else {
                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
                        }
                    }
                });
    }


//    private void generateSmartReply(String message) {
//        List<FirebaseTextMessage> conversation = new ArrayList<>();
//        for (Message msg : messageList) {
//            if (msg.isUser()) {
//                conversation.add(FirebaseTextMessage.createForLocalUser(msg.getMessage(), System.currentTimeMillis()));
//            } else {
//                conversation.add(FirebaseTextMessage.createForRemoteUser(msg.getMessage(), System.currentTimeMillis(), "bot"));
//            }
//        }
//
//        // Log the conversation for debugging
//        Log.d("SmartReply", "Conversation for Smart Reply:");
//        for (FirebaseTextMessage msg : conversation) {
//            Log.d("SmartReply", "Message: " + msg.toString());
//        }
//
//        firebaseSmartReply.suggestReplies(conversation)
//                .addOnCompleteListener(new OnCompleteListener<SmartReplySuggestionResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<SmartReplySuggestionResult> task) {
//                        if (task.isSuccessful()) {
//                            SmartReplySuggestionResult result = task.getResult();
//                            Log.d("SmartReply", "Smart Reply result status: " + result.getStatus());
//                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
//                                messageList.add(new Message("I don't understand the question.", false));
//                                showSuggestedPrompts();
//                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
//                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
//                                Log.d("SmartReply", "Number of suggestions: " + suggestions.size());
//                                if (!suggestions.isEmpty()) {
//                                    String reply = suggestions.get(0).getText();
//                                    Log.d("SmartReply", "Suggested reply: " + reply);
//                                    if (isGenericOrEmojiReply(reply)) {
//                                        messageList.add(new Message("I don't understand the question.", false));
//                                        showSuggestedPrompts();
//                                    } else {
//                                        messageList.add(new Message(reply, false));
//                                        hideSuggestedPrompts();
//                                    }
//                                } else {
//                                    messageList.add(new Message("I don't understand the question.", false));
//                                    Log.d("SmartReply", "No suggestions available.");
//                                    showSuggestedPrompts();
//                                }
//                            }
//                            chatAdapter.notifyDataSetChanged();
//                            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
//                        } else {
//                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
//                        }
//                    }
//                });
//    }

    private boolean isGenericOrEmojiReply(String reply) {
        // List of generic replies to filter out
        List<String> genericReplies = Arrays.asList("Okay", "I don't know", "Sorry", "Alright", "Oh, okay", "😟", "😊", "😢");
        return genericReplies.contains(reply) || reply.matches("[\\p{So}\\p{Cn}]+"); // Matches emojis and other symbols
    }

    private boolean isGreeting(String message) {
        List<String> greetings = Arrays.asList("hi", "hello", "hey", "good morning", "good afternoon", "good evening", "how is your day", "how are you");
        for (String greeting : greetings) {
            if (message.toLowerCase().contains(greeting)) {
                return true;
            }
        }
        return false;
    }

    private String getGreetingResponse() {
        List<String> responses = Arrays.asList("Hello! How can I assist you today?", "Hi there! What can I do for you?", "Hey! Need any help?");
        return responses.get(new Random().nextInt(responses.size()));
    }

    private void fetchFaqAnswer(final String documentId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                firestoreDb.collection("FAQ").document(documentId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful() && task.getResult().exists()) {
                                    String answer = task.getResult().getString("Answer");
                                    Log.d("fetchFaqAnswer", "Answer fetched for document ID: " + documentId + ", Answer: " + answer); // *********************
                                    messageList.add(new Message(answer, false));
                                    hideSuggestedPrompts();
                                } else {
                                    Log.d("fetchFaqAnswer", "Failed to fetch answer for document ID: " + documentId); // *********************
                                    showSuggestedPrompts();
                                }
                                chatAdapter.notifyDataSetChanged();
                                chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }
                        });
            }
        });
    }

    private void showSuggestedPrompts() {
        suggestedPromptAdapter.updateData(questionsList);
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

    private int getLevenshteinDistance(String s1, String s2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return levenshteinDistance.apply(s1, s2);
    }

    private String findClosestKeyword(String message) {
        String[] words = message.split(" ");
        Map<String, Integer> keywordDistanceMap = new HashMap<>();

        for (String word : words) {
            for (String keyword : keywordToDocIdMap.keySet()) {
                int distance = getLevenshteinDistance(word.toLowerCase(), keyword.toLowerCase());
                if (!keywordDistanceMap.containsKey(keyword) || distance < keywordDistanceMap.get(keyword)) {
                    keywordDistanceMap.put(keyword, distance);
                }
            }
        }

        String closestKeyword = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : keywordDistanceMap.entrySet()) {
            if (entry.getValue() < minDistance) {
                minDistance = entry.getValue();
                closestKeyword = entry.getKey();
            }
        }

        Log.d("findClosestKeyword", "Min Distance: " + minDistance);
        if (minDistance <= 1) { // Adjust the threshold here if needed
            return closestKeyword;
        } else {
            return null;
        }
    }
    private String cleanMessage(String message) {
        // Normalize the message to remove accents and other diacritical marks
        String normalizedMessage = Normalizer.normalize(message, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String cleanedMessage = pattern.matcher(normalizedMessage).replaceAll("");

        // Convert the message to lowercase
        cleanedMessage = cleanedMessage.toLowerCase();

        // Remove common suffixes such as 's'
        cleanedMessage = cleanedMessage.replaceAll("\\bs\\b", "");

        // Remove punctuation and special characters
        cleanedMessage = cleanedMessage.replaceAll("[^a-zA-Z0-9\\s]", "");

        // Trim any extra spaces
        cleanedMessage = cleanedMessage.trim();

        return cleanedMessage;
    }

}
