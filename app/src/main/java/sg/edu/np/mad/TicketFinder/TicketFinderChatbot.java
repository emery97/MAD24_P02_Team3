package sg.edu.np.mad.TicketFinder;

import android.Manifest;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class TicketFinderChatbot extends AppCompatActivity {
    // UI Components for the chat interface
    RecyclerView chatRecyclerView; // RecyclerView for displaying chat messages
    RecyclerView suggestedPromptsRecyclerView; // RecyclerView for displaying suggested prompts
    EditText messageInput; // Input field for the user to type messages
    ImageView sendButton; // Button for sending the user's message
    ImageView micIcon; // Button for voice input

    SpeechRecognizerHelper speechRecognizerHelper; // Helper for voice input


    // Data structures to store chat messages and suggested prompts
    ArrayList<Message> messageList; // List to hold chat messages
    ChatAdapter chatAdapter; // Adapter for chat messages
    SuggestedPromptAdapter suggestedPromptAdapter; // Adapter for suggested prompts
    EventAdapter eventAdapter; // Adapter for events

    // Firebase and Smart Reply components
    FirebaseFirestore firestoreDb; // Firestore database instance
    FirebaseSmartReply firebaseSmartReply; // Firebase Smart Reply instance
    Translator translator; // Translator instance for language detection and translation



    // Maps and lists for handling data
    private final Map<String, String> keywordToDocIdMap = new HashMap<>(); // Map to store keywords and their corresponding document IDs
    private final Map<String, String> chatbotResponsesMap = new HashMap<>(); // Map to store chatbot responses
    private final List<String> questionsList = new ArrayList<>(); // List to store questions
    private ArrayList<Event> eventsList = new ArrayList<>(); // List to store events
    private List<Greeting> greetingsList = new ArrayList<>(); // List to store greetings
    private List<String> translatedQuestionsList = new ArrayList<>(); // List to store translated questions
    private String lastClickedPrompt = ""; // Store the last clicked prompt

    // Constants and permissions
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // Request code for audio recording permission
    private boolean permissionToRecordAccepted = false; // Flag to check if audio recording permission is granted
    private String[] permissions = {Manifest.permission.RECORD_AUDIO}; // Permissions array for audio recording

    private String userLanguage = "en"; // Default language set to English



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_finder_chat); // Set the content view to the activity's layout

        // Initialize UI components
        chatRecyclerView = findViewById(R.id.chat_recycler);
        suggestedPromptsRecyclerView = findViewById(R.id.suggested_prompts_recycler);
        messageInput = findViewById(R.id.message_input_field);
        sendButton = findViewById(R.id.send_button);
        micIcon = findViewById(R.id.mic_button);

        // Initialize message list and adapters
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        suggestedPromptAdapter = new SuggestedPromptAdapter(new ArrayList<>(), this::onSuggestedPromptClick);
        eventAdapter = new EventAdapter(this, new ArrayList<>(), false);

        // Initialize Firebase and Smart Reply
        firestoreDb = FirebaseFirestore.getInstance();
        firebaseSmartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
        translator = new Translator();

        // Setup RecyclerViews with layout managers and adapters
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager promptLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        suggestedPromptsRecyclerView.setAdapter(suggestedPromptAdapter);
        suggestedPromptsRecyclerView.setLayoutManager(promptLayoutManager);

        // Initialize Speech Recognizer helper
        speechRecognizerHelper = new SpeechRecognizerHelper(this, micIcon, new SpeechRecognizerHelper.OnSpeechResultListener() {
            @Override
            public void onSpeechResult(String text) {
                messageInput.setText(text);
            }
        });

        // Setup mic icon click listener to start speech recognition
        micIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for audio recording permission
                if (ContextCompat.checkSelfPermission(TicketFinderChatbot.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    speechRecognizerHelper.startListening(); // Start listening for speech input
                } else {
                    ActivityCompat.requestPermissions(TicketFinderChatbot.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION); // Request audio recording permission
                }
            }
        });

        // Fetch data from Firestore
        fetchFAQFromFirestore(); // Fetch FAQs
        fetchEventsFromFirestore(); // Fetch events
        fetchGreetingsFromFirestore(); // Fetch greetings
        showGreetingMsg(); // Show greeting message

        // Setup send button click listener to send user messages
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = messageInput.getText().toString().trim();
                Log.d("sendButton", "User message: " + userMessage);
                if (!userMessage.isEmpty()) {
                    // Add the user's message to the chat
                    messageList.add(new Message(userMessage, true));
                    chatAdapter.notifyDataSetChanged(); // Notify the adapter about the new message
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1); // Scroll to the latest message
                    handleUserMessage(userMessage); // Handle the user's message
                    messageInput.setText(""); // Clear the input field
                    hideSuggestedPrompts(); // Hide suggested prompts after sending a message
                }
            }
        });

        // Setup close chat button click listener to finish activity
        ImageView closeChatButton = findViewById(R.id.close_button);
        closeChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            } // Close the activity
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizerHelper.destroy(); // Destroy the speech recognizer helper
    }


    // ------------------------------- START OF: fetching collections from database -------------------------------
    // Fetch FAQs from Firestore
    private void fetchFAQFromFirestore() {
        dbHandler db = new dbHandler();
        db.getFaq(new FirestoreCallback<Faq>() {
            @Override
            public void onCallback(ArrayList<Faq> faqList) {
                Log.d("fetchFAQFromFirestore", "FAQ list size: " + faqList.size());
                for (Faq faq : faqList) {
                    String question = faq.getQuestion(); // Get the question from the FAQ
                    String answer = faq.getAnswer(); // Get the answer from the FAQ
                    List<String> keywords = faq.getKeywords(); // Get the keywords from the FAQ
                    Log.d("fetchFAQFromFirestore", "Processing FAQ - Question: " + question + ", Answer: " + answer + ", Keywords: " + keywords);
                    if (question != null && answer != null && keywords != null) {
                        // Map keywords to their corresponding questions and answers
                        for (String keyword : keywords) {
                            keywordToDocIdMap.put(keyword.toLowerCase(), question); // Map the keyword to the question
                            chatbotResponsesMap.put(keyword.toLowerCase(), answer); // Map the keyword to the answer
                        }
                        questionsList.add(question); // Add the question to the questions list
                        chatbotResponsesMap.put(question.toLowerCase(), answer); // Map the question to the answer
                    }
                }
                Log.d("fetchQuestions", "Questions fetched successfully: " + questionsList);
                runOnUiThread(() -> showSuggestedPrompts()); // Update UI after fetching data
            }
        });
    }

    // Fetch events from Firestore
    private void fetchEventsFromFirestore() {
        dbHandler db = new dbHandler(); // Create a new dbHandler instance
        db.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> list) {
                eventsList = list;  // Set the events list
                for (Event event : eventsList) {
                    Log.d("fetchEventsFromFirestore", "Artist: " + event.getArtist());
                }
                Log.d("fetchEvents", "Events fetched successfully: " + eventsList.size());
            }
        });
    }

    // Fetch greetings from Firestore
    private void fetchGreetingsFromFirestore() {
        dbHandler db = new dbHandler(); // Create a new dbHandler instance
        db.getGreetings(new FirestoreCallback<Greeting>() {
            @Override
            public void onCallback(ArrayList<Greeting> list) {
                greetingsList = list; // Set the greetings list
                for (Greeting greeting : greetingsList) {
                    Log.d("fetchGreetingsFromFirestore", "Greeting: " + greeting.getGreeting());
                }
                runOnUiThread(() -> Log.d("fetchGreetings", "Greetings fetched successfully: " + greetingsList.size()));
            }
        });
    }
    // ------------------------------- END OF: fetching collections from database -------------------------------

    // Handle user messages
    private void handleUserMessage(final String message) {
        Log.d("handleUserMessage", "Received message: " + message);

        // Clean the message
        String cleanedMessage = cleanMessage(message);

        // Check if the message matches any greeting first
        String closestGreeting = null; // Variable to store the closest greeting
        int minDistance = Integer.MAX_VALUE; // Variable to store the minimum distance

        for (Greeting greeting : greetingsList) {
            String cleanedGreeting = cleanMessage(greeting.getGreeting()).toLowerCase();  // Clean the greeting
            Log.d("handleUserMessage", "Checking greeting: " + cleanedGreeting);

            int distance = getLevenshteinDistance(cleanedMessage, cleanedGreeting);  // Calculate the distance between the message and the greeting
            if (distance < minDistance) {
                minDistance = distance; // Update the minimum distance
                closestGreeting = greeting.getGreeting(); // Update the closest greeting
            }
        }

        if (minDistance <= 1 && closestGreeting != null) {
            Greeting matchedGreeting = getGreetingByText(closestGreeting); // Get the matched greeting
            if (matchedGreeting != null) {
                Log.d("handleUserMessage", "Greeting matched: " + closestGreeting);
                addMessageToChat(matchedGreeting.getResponse()); // Add the matched greeting's response to the chat
                showSuggestedPrompts(); // Show suggested prompts
                return;
            }
        }

        // Check if the message matches any artist name before detection then move on to detection and translation afterwards
        if (checkForArtistMatch(cleanedMessage)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSuggestedPrompts();
                }  // Hide suggested prompts if an artist match is found
            });
            return;
        }

        // Run the language detection and processing in a background thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Detect Language using Google Cloud Translation API
                    detectLanguageAndProcessMessage(message); // Detect language and process the message
                } catch (Exception e) {
                    Log.e("handleUserMessage", "Error in processing message: ", e);
                }
            }
        });
    }

    // Get greeting by text
    private Greeting getGreetingByText(String text) {
        for (Greeting greeting : greetingsList) {
            if (greeting.getGreeting().equalsIgnoreCase(text)) {
                return greeting; // Return the greeting if it matches the text
            }
        }
        return null; // Return null if no greeting matches the text
    }


    // ------------------------------- START OF: detecting if message is in english, otherwise call translateMessageToEnglishAndProcess method -------------------------------
    private void detectLanguageAndProcessMessage(final String message) { // because if you type for example 'erferfer' the detected language may identify it as an actual language. but if the content stays the same then you know the user typed gibberish. so instead of setting the language to detected language if the content stayed the same, assign the detected language to what it was
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String detectedLanguage = translator.detectLanguage(message); // Detect the language of the message
                    Log.d("detectLanguageAndProcessMessage", "Detected language: " + detectedLanguage);

                    if (!"en".equals(detectedLanguage)) {
                        String translatedMessage = translator.translate(detectedLanguage, "en", message); // Translate the message to English
                        Log.d("translateMessageToEnglishAndProcess", "Translated message: " + translatedMessage);

                        if (translatedMessage.equals(message)) {
                            // If the translated message is the same as the original message, process as is
                            processMessage(message, userLanguage); // because user may not be speaking in english previously. so set it as userlanguage instead of 'en'
                        } else {
                            // Otherwise, process the translated message
                            processMessage(translatedMessage, detectedLanguage);
                        }
                    } else {
                        processMessage(message, "en");  // Process the message if it is in English
                    }
                } catch (Exception e) {
                    Log.e("detectLanguageAndProcessMessage", "Error in detecting language: ", e);
                    processMessage(message, "en"); // Process the message as if it is in English in case of error
                }
            }
        });
    }

    // ------------------------------- END OF: detecting if message is in english, otherwise call translateMessageToEnglishAndProcess method -------------------------------



    // ------------------------------- START OF: translate answers to detected language before sending it back to the user -------------------------------
    private void translateAndSendMessage(final String message, final String targetLanguage) {
        Log.d("translateAndSendMessage", "Starting translation from 'en' to: " + targetLanguage); // Log the translation start
        userLanguage = targetLanguage;  // Update the user's language
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String translatedMessage = translator.translate("en", targetLanguage, message); // Translate the message to the target language
                    Log.d("translateAndSendMessage", "Translated response: " + translatedMessage);  // Log the translated response
                    runOnUiThread(() -> addMessageToChat(translatedMessage)); // Update UI with the translated message
                } catch (Exception e) {
                    Log.e("translateAndSendMessage", "Error translating message: ", e);
                    // Fallback to displaying the original message
                    runOnUiThread(() -> addMessageToChat(message));
                }
            }
        });
    }
    // ------------------------------- END OF: translate answers to detected language before sending it back to the user -------------------------------




    // ------------------------------- START OF: central method for checking if it is greeting msg, artist name, or if it is close to a keyword-------------------------------
    private void processMessage(final String message, final String userLanguage) {
        String cleanedMessage = cleanMessage(message); // Clean the message

        // Directly handle suggested prompts (original and translated)
        List<String> promptsToCheck = "en".equals(userLanguage) ? questionsList : translatedQuestionsList; // Determine which list of prompts to check
        for (int i = 0; i < promptsToCheck.size(); i++) {
            if (promptsToCheck.get(i).equalsIgnoreCase(cleanedMessage) || promptsToCheck.get(i).equalsIgnoreCase(lastClickedPrompt)) {
                String answer = chatbotResponsesMap.get(questionsList.get(i).toLowerCase()); // Get the answer for the prompt
                if ("en".equals(userLanguage)) {
                    addMessageToChat(answer); // Add the answer to the chat if the language is English
                } else {
                    translateAndSendMessage(answer, userLanguage); // Translate and send the answer if the language is not English
                }
                lastClickedPrompt = ""; // Clear the last clicked prompt
                return;
            }
        }

        String closestKeyword = findClosestKeyword(cleanedMessage);  // Find the closest keyword to the message
        Log.d("processMessage", "Closest keyword: " + closestKeyword);
        if (closestKeyword != null) {
            String answer = chatbotResponsesMap.get(closestKeyword); // Get the answer for the closest keyword
            Log.d("processMessage", "Answer found for keyword: " + closestKeyword + ", Answer: " + answer);
            if ("en".equals(userLanguage)) {
                addMessageToChat(answer); // Add the answer to the chat if the language is English
            } else {
                translateAndSendMessage(answer, userLanguage); // Translate and send the answer if the language is not English
            }
        } else {
            generateSmartReply(cleanedMessage, userLanguage); // Generate a Smart Reply if no keyword match is found
        }
    }
    // ------------------------------- END OF: central method for checking if it is greeting msg, artist name, or if it is close to a keyword-------------------------------



    // ------------------------------- START OF: artist name check method -------------------------------
    private boolean checkForArtistMatch(String message) {
        String[] userWords = message.split(" "); // Split the message into words
        List<Event> matchedEvents = new ArrayList<>(); // List to store matched events

        for (Event event : eventsList) {
            String artist = event.getArtist().toLowerCase(); // Get the artist's name in lowercase

            // Check for full artist name match
            for (String userWord : userWords) {
                if (getLevenshteinDistance(userWord.toLowerCase(), artist) <= 1) {
                    matchedEvents.add(event); // Add the event if there is a close match
                }
            }

            // Check for partial artist name match (two words from user match with artist name)
            for (int i = 0; i < userWords.length - 1; i++) {
                String combinedUserWords = userWords[i].toLowerCase() + " " + userWords[i + 1].toLowerCase(); // Combine two consecutive words
                if (getLevenshteinDistance(combinedUserWords, artist) <= 2) {
                    matchedEvents.add(event);  // Add the event if there is a close match
                }
            }
        }

        if (!matchedEvents.isEmpty()) {
            sendEventIntroMessage(matchedEvents.get(0).getArtist()); // Send an message for the first matched event
            addEventMessageToChat(matchedEvents);
            return true;
        }
        return false; // Return false if no match is found
    }

    // ------------------------------- END OF: artist name check method -------------------------------



    // Show greeting message
    private void showGreetingMsg() {
        String welcomeMessage = "Hi my name is Joe Yi! Feel free to ask me any questions related to events!"; // Define the welcome message
        messageList.add(new Message(welcomeMessage, false)); // Add the welcome message to the message list
        chatAdapter.notifyDataSetChanged(); // Notify the adapter about the new message
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1); // Scroll to the latest message
        runOnUiThread(() -> showSuggestedPrompts()); // Ensure this runs on the UI thread
    }

    // Generate Smart Reply suggestions
    private void generateSmartReply(String message, String userLanguage) {
        List<FirebaseTextMessage> conversation = new ArrayList<>(); // List to store the conversation
        for (Message msg : messageList) {
            if (msg.isUser()) {
                conversation.add(FirebaseTextMessage.createForLocalUser(msg.getMessage(), System.currentTimeMillis())); // Add user messages to the conversation
            } else {
                conversation.add(FirebaseTextMessage.createForRemoteUser(msg.getMessage(), System.currentTimeMillis(), "bot")); // Add bot messages to the conversation
            }
        }

        firebaseSmartReply.suggestReplies(conversation)
                .addOnCompleteListener(new OnCompleteListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SmartReplySuggestionResult> task) {
                        if (task.isSuccessful()) {
                            SmartReplySuggestionResult result = task.getResult();
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                translateAndSendMessage("I don't understand the question.", userLanguage);  // Translate and send a default message if the language is not supported
                                showSuggestedPrompts(); // Show suggested prompts
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions(); // Get the Smart Reply suggestions
                                if (!suggestions.isEmpty()) {
                                    String reply = suggestions.get(0).getText(); // Get the first suggestion
                                    if (isGenericOrEmojiReply(reply)) {
                                        translateAndSendMessage("I don't understand the question.", userLanguage); // Translate and send a default message if the reply is generic or an emoji
                                        showSuggestedPrompts(); // Show suggested prompts
                                    } else {
                                        addMessageToChat(reply); // Add the Smart Reply to the chat
                                        hideSuggestedPrompts(); // Hide suggested prompts
                                    }
                                    Log.d("SmartReply", "Reply: " + reply);
                                } else {
                                    translateAndSendMessage("I don't understand the question.", userLanguage); // Translate and send a default message if no suggestions are available
                                    showSuggestedPrompts(); // Show suggested prompts
                                    Log.d("SmartReply", "No suggestions available.");
                                }
                            }
                        } else {
                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
                        }
                    }
                });
    }

    // Check if the reply is a generic or emoji reply
    private boolean isGenericOrEmojiReply(String reply) {
        List<String> genericReplies = Arrays.asList("Okay", "I don't know", "Sorry", "Alright", "Oh, okay", "😟", "😊", "😢"); // List of generic replies and emojis
        return genericReplies.contains(reply) || reply.matches("[\\p{So}\\p{Cn}]+"); // Return true if the reply is generic or an emoji
    }

    // Send an introduction message for an event
    private void sendEventIntroMessage(String artist) {
        runOnUiThread(() -> {
            // Add the arrow down animation
            messageList.add(new Message("arrow_down_animation", false)); // This will trigger the arrow down animation view
            chatAdapter.notifyDataSetChanged(); // Notify the adapter about the new message
            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1); // Scroll to the latest message
        });
    }

    // Add an event message to the chat
    private void addEventMessageToChat(final List<Event> events) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (events != null && !events.isEmpty()) {
                    Log.d("addEventMessageToChat", "Adding event messages to chat: " + events.size() + " events");

                    messageList.add(new Message(events)); // Add the events to the message list
                    chatAdapter.notifyDataSetChanged(); // Notify the adapter about the new messages
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1); // Scroll to the latest message
                } else {
                    Log.e("addEventMessageToChat", "Received null or empty event list");
                }
            }
        });
    }

    // Add a message to the chat
    private void addMessageToChat(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message != null) {
                    Log.d("addMessageToChat", "Adding message to chat: " + message); // Debugging log
                    if (messageList.isEmpty() || !messageList.get(messageList.size() - 1).getMessage().equals(message)) {
                        messageList.add(new Message(message, false)); // Add the message to the message list
                        chatAdapter.notifyDataSetChanged(); // Notify the adapter about the new message
                        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1); // Scroll to the latest message
                    } else {
                        Log.d("addMessageToChat", "Duplicate message not added."); // Debugging log
                    }
                } else {
                    Log.e("addMessageToChat", "Received null message"); // Debugging log
                }
            }
        });
    }



    // ------------------------------- START OF: checking users prompt against keywords to get the levenshtein distance method -------------------------------
    private int getLevenshteinDistance(String s1, String s2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance(); // Create a new LevenshteinDistance instance
        return levenshteinDistance.apply(s1, s2);  // Calculate and return the Levenshtein distance between the two strings
    }
    // ------------------------------- START OF: checking users prompt against keywords to get the levenshtein distance method -------------------------------



    // ------------------------------- START OF: matching users prompt to keywords methods -------------------------------
    private String findClosestKeyword(String message) {
        String[] words = message.split(" "); // Split the message into words
        Map<String, Integer> keywordDistanceMap = new HashMap<>(); // Map to store keywords and their distances

        for (String word : words) {
            for (String keyword : keywordToDocIdMap.keySet()) {
                int distance = getLevenshteinDistance(word.toLowerCase(), keyword.toLowerCase()); // Calculate the distance between the word and the keyword
                if (!keywordDistanceMap.containsKey(keyword) || distance < keywordDistanceMap.get(keyword)) {
                    keywordDistanceMap.put(keyword, distance);  // Update the map with the keyword and its distance
                }
            }
        }

        String closestKeyword = null; // Variable to store the closest keyword
        int minDistance = Integer.MAX_VALUE; // Variable to store the minimum distance

        for (Map.Entry<String, Integer> entry : keywordDistanceMap.entrySet()) {
            if (entry.getValue() < minDistance) {
                minDistance = entry.getValue(); // Update the minimum distance
                closestKeyword = entry.getKey(); // Update the closest keyword
            }
        }

        Log.d("findClosestKeyword", "Min Distance: " + minDistance);
        if (minDistance <= 1) {
            return closestKeyword; // Return the closest keyword if the minimum distance is less than or equal to 1
        } else {
            return null; // Return null if no close keyword is found
        }
    }
    // ------------------------------- END OF: matching users prompt to keywords methods -------------------------------



    // ------------------------------- START OF: suugested prompt methods -------------------------------
    // left to do is to translate suggested prompt to detected language
    private void showSuggestedPrompts() {
        if (questionsList.isEmpty()) {
            Log.d("showSuggestedPrompts", "No prompts to show.");
            return; // No data to show
        }

        if (!userLanguage.equals("en")) {
            translatePromptsToUserLanguage(userLanguage); // Translate prompts to the user's language if it is not English
        } else {
            runOnUiThread(() -> {
                suggestedPromptAdapter.updateData(questionsList); // Update the adapter with the questions list
                suggestedPromptsRecyclerView.setVisibility(View.VISIBLE); // Make the suggested prompts RecyclerView visible
                suggestedPromptAdapter.notifyDataSetChanged(); // Notify the adapter about the data change
                Log.d("showSuggestedPrompts", "Suggested prompts shown.");
            });
        }
    }

    // Translate prompts to the user's language
    private void translatePromptsToUserLanguage(final String targetLanguage) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> translatedPrompts = new ArrayList<>(); // List to store translated prompts
                    for (String prompt : questionsList) {
                        String translatedPrompt = translator.translate("en", targetLanguage, prompt); // Translate the prompt to the target language
                        translatedPrompts.add(translatedPrompt); // Add the translated prompt to the list
                        Log.d("translatePromptsToUserLanguage", "Original prompt: " + prompt + ", Translated prompt: " + translatedPrompt);
                    }
                    translatedQuestionsList = translatedPrompts; // Update the translated prompts list
                    runOnUiThread(() -> {
                        suggestedPromptAdapter.updateData(translatedPrompts); // Update the adapter with the translated prompts
                        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE); // Make the suggested prompts RecyclerView visible
                    });
                    Log.d("translatePromptsToUserLanguage", "Prompts updated in UI with translated language");
                } catch (Exception e) {
                    Log.e("translatePromptsToUserLanguage", "Error translating prompts: ", e);
                    runOnUiThread(() -> {
                        suggestedPromptAdapter.updateData(questionsList); // Update the adapter with the original questions list
                        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE); // Make the suggested prompts RecyclerView visible
                    });
                }
            }
        });
    }


    // Hide suggested prompts
    private void hideSuggestedPrompts() {
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

    // Handle click on suggested prompt
    private void onSuggestedPromptClick(String prompt) {
        messageInput.setText(prompt); // Set the clicked prompt in the message input field
        messageInput.setSelection(prompt.length()); // Move the cursor to the end of the prompt
        lastClickedPrompt = prompt; // Store the last clicked prompt
    }
    // ------------------------------- END OF: suugested prompt methods -------------------------------



    // ------------------------------- START OF: utility method -------------------------------
    private String cleanMessage(String message) {
        String normalizedMessage = Normalizer.normalize(message, Normalizer.Form.NFD); // Normalize the message
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); // Pattern to match diacritical marks
        String cleanedMessage = pattern.matcher(normalizedMessage).replaceAll(""); // Remove diacritical marks from the message

        cleanedMessage = cleanedMessage.toLowerCase(); // Convert the message to lowercase
        cleanedMessage = cleanedMessage.replaceAll("\\bs\\b", ""); // Remove the letter 's' if it stands alone
        cleanedMessage = cleanedMessage.replaceAll("[^a-zA-Z0-9\\s]", ""); // Remove all non-alphanumeric characters
        cleanedMessage = cleanedMessage.trim(); // Trim leading and trailing spaces

        return cleanedMessage; // Return the cleaned message
    }
    // ------------------------------- END OF: utility method -------------------------------
}
