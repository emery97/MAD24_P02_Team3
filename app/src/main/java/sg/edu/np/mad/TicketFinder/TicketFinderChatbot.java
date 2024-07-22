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
    RecyclerView chatRecyclerView;
    RecyclerView suggestedPromptsRecyclerView;
    EditText messageInput;
    ImageView sendButton;
    ArrayList<Message> messageList;
    ChatAdapter chatAdapter;
    SuggestedPromptAdapter suggestedPromptAdapter;
    FirebaseFirestore firestoreDb;
    FirebaseSmartReply firebaseSmartReply;
    Translator translator;

    private final Map<String, String> keywordToDocIdMap = new HashMap<>();
    private final Map<String, String> chatbotResponsesMap = new HashMap<>();
    private final List<String> questionsList = new ArrayList<>();
    private ArrayList<Event> eventsList = new ArrayList<>();
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private ImageView micIcon;
    private SpeechRecognizerHelper speechRecognizerHelper;
    private EventAdapter eventAdapter;
    private String userLanguage = "en"; // Default language
    private List<String> translatedQuestionsList = new ArrayList<>(); // Store translated prompts
    private List<Greeting> greetingsList = new ArrayList<>();



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
        eventAdapter = new EventAdapter(this, new ArrayList<>(), false);

        firestoreDb = FirebaseFirestore.getInstance();
        firebaseSmartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
        translator = new Translator();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager promptLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        suggestedPromptsRecyclerView.setAdapter(suggestedPromptAdapter);
        suggestedPromptsRecyclerView.setLayoutManager(promptLayoutManager);

        speechRecognizerHelper = new SpeechRecognizerHelper(this, micIcon, new SpeechRecognizerHelper.OnSpeechResultListener() {
            @Override
            public void onSpeechResult(String text) {
                messageInput.setText(text);
            }
        });

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

        fetchFAQFromFirestore();
        fetchEventsFromFirestore();
        fetchGreetingsFromFirestore();
        showGreetingMsg();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = messageInput.getText().toString().trim();
                Log.d("sendButton", "User message: " + userMessage);
                if (!userMessage.isEmpty()) {
                    // Add the user's message to the chat
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


    // ------------------------------- START OF: fetching collections from database -------------------------------
    private void fetchFAQFromFirestore() {
        dbHandler db = new dbHandler();
        db.getFaq(new FirestoreCallback<Faq>() {
            @Override
            public void onCallback(ArrayList<Faq> faqList) {
                Log.d("fetchFAQFromFirestore", "FAQ list size: " + faqList.size());
                for (Faq faq : faqList) {
                    String question = faq.getQuestion();
                    String answer = faq.getAnswer();
                    List<String> keywords = faq.getKeywords();
                    Log.d("fetchFAQFromFirestore", "Processing FAQ - Question: " + question + ", Answer: " + answer + ", Keywords: " + keywords);
                    if (question != null && answer != null && keywords != null) {
                        for (String keyword : keywords) {
                            keywordToDocIdMap.put(keyword.toLowerCase(), question);
                            chatbotResponsesMap.put(keyword.toLowerCase(), answer);
                        }
                        questionsList.add(question);
                        chatbotResponsesMap.put(question.toLowerCase(), answer);
                    }
                }
                Log.d("fetchQuestions", "Questions fetched successfully: " + questionsList);
                runOnUiThread(() -> showSuggestedPrompts()); // Update UI after fetching data
            }
        });
    }

    private void fetchEventsFromFirestore() {
        dbHandler db = new dbHandler();
        db.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> list) {
                eventsList = list;
                for (Event event : eventsList) {
                    Log.d("fetchEventsFromFirestore", "Artist: " + event.getArtist());
                }
                Log.d("fetchEvents", "Events fetched successfully: " + eventsList.size());
            }
        });
    }

    private void fetchGreetingsFromFirestore() {
        dbHandler db = new dbHandler();
        db.getGreetings(new FirestoreCallback<Greeting>() {
            @Override
            public void onCallback(ArrayList<Greeting> list) {
                greetingsList = list;
                for (Greeting greeting : greetingsList) {
                    Log.d("fetchGreetingsFromFirestore", "Greeting: " + greeting.getGreeting());
                }
                runOnUiThread(() -> Log.d("fetchGreetings", "Greetings fetched successfully: " + greetingsList.size()));
            }
        });
    }
    // ------------------------------- END OF: fetching collections from database -------------------------------

    private void handleUserMessage(final String message) {
        Log.d("handleUserMessage", "Received message: " + message);

        // Clean the message
        String cleanedMessage = cleanMessage(message);

        // Check if the message matches any greeting first
        String closestGreeting = null;
        int minDistance = Integer.MAX_VALUE;

        for (Greeting greeting : greetingsList) {
            String cleanedGreeting = cleanMessage(greeting.getGreeting()).toLowerCase();
            Log.d("handleUserMessage", "Checking greeting: " + cleanedGreeting);

            int distance = getLevenshteinDistance(cleanedMessage, cleanedGreeting);
            if (distance < minDistance) {
                minDistance = distance;
                closestGreeting = greeting.getGreeting();
            }
        }

        if (minDistance <= 1 && closestGreeting != null) {
            Greeting matchedGreeting = getGreetingByText(closestGreeting);
            if (matchedGreeting != null) {
                Log.d("handleUserMessage", "Greeting matched: " + closestGreeting);
                addMessageToChat(matchedGreeting.getResponse());
                showSuggestedPrompts();
                return;
            }
        }

        // Check if the message matches any artist name before detection then move on to detection and translation afterwards
        if (checkForArtistMatch(cleanedMessage)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSuggestedPrompts();
                }
            });
            return;
        }

        // Run the language detection and processing in a background thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Detect Language using Google Cloud Translation API
                    detectLanguageAndProcessMessage(message);
                } catch (Exception e) {
                    Log.e("handleUserMessage", "Error in processing message: ", e);
                }
            }
        });
    }

    private Greeting getGreetingByText(String text) {
        for (Greeting greeting : greetingsList) {
            if (greeting.getGreeting().equalsIgnoreCase(text)) {
                return greeting;
            }
        }
        return null;
    }


    // ------------------------------- START OF: detecting if message is in english, otherwise call translateMessageToEnglishAndProcess method -------------------------------
//    private void detectLanguageAndProcessMessage(final String message) {
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String detectedLanguage = translator.detectLanguage(message);
//                    Log.d("detectLanguageAndProcessMessage", "Detected language: " + detectedLanguage);
//
//                    if (!"en".equals(detectedLanguage)) {
//                        translateMessageToEnglishAndProcess(message, detectedLanguage);
//                    } else {
//                        processMessage(message, "en");
//                    }
//                } catch (Exception e) {
//                    Log.e("detectLanguageAndProcessMessage", "Error in detecting language: ", e);
//                    processMessage(message, "en");
//                }
//            }
//        });
//    }
    private void detectLanguageAndProcessMessage(final String message) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String detectedLanguage = translator.detectLanguage(message);
                    Log.d("detectLanguageAndProcessMessage", "Detected language: " + detectedLanguage);

                    if (!"en".equals(detectedLanguage)) {
                        String translatedMessage = translator.translate(detectedLanguage, "en", message);
                        Log.d("translateMessageToEnglishAndProcess", "Translated message: " + translatedMessage);

                        if (translatedMessage.equals(message)) {
                            // If the translated message is the same as the original message, process as is
                            processMessage(message, "en");
                        } else {
                            // Otherwise, process the translated message
                            processMessage(translatedMessage, detectedLanguage);
                        }
                    } else {
                        processMessage(message, "en");
                    }
                } catch (Exception e) {
                    Log.e("detectLanguageAndProcessMessage", "Error in detecting language: ", e);
                    processMessage(message, "en");
                }
            }
        });
    }

    // ------------------------------- END OF: detecting if message is in english, otherwise call translateMessageToEnglishAndProcess method -------------------------------


    // ------------------------------- START OF: translate msg to english before comparing with keywords method -------------------------------
    private void translateMessageToEnglishAndProcess(final String message, final String detectedLanguage) {
        AsyncTask.execute(new Runnable() { // this method is for messages that are not detected in english
            @Override
            public void run() {
                try {
                    String translatedMessage = translator.translate(detectedLanguage, "en", message);
                    Log.d("translateMessageToEnglishAndProcess", "Translated message: " + translatedMessage);
                    processMessage(translatedMessage, detectedLanguage);
                } catch (Exception e) {
                    Log.e("translateMessageToEnglishAndProcess", "Error in translation: ", e);
                    processMessage(message, detectedLanguage);
                }
            }
        });
    }
    // ------------------------------- END OF: translate msg to english before comparing with keywords method -------------------------------



    // ------------------------------- START OF: translate answers to detected language before sending it back to the user -------------------------------
    private void translateAndSendMessage(final String message, final String targetLanguage) {
        Log.d("translateAndSendMessage", "Starting translation from 'en' to: " + targetLanguage);
        userLanguage = targetLanguage;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String translatedMessage = translator.translate("en", targetLanguage, message);
                    Log.d("translateAndSendMessage", "Translated response: " + translatedMessage);
                    runOnUiThread(() -> addMessageToChat(translatedMessage));
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
        String cleanedMessage = cleanMessage(message);

        // Directly handle suggested prompts (original and translated)
        List<String> promptsToCheck = "en".equals(userLanguage) ? questionsList : translatedQuestionsList;
        for (int i = 0; i < promptsToCheck.size(); i++) {
            if (promptsToCheck.get(i).equalsIgnoreCase(cleanedMessage)) {
                String answer = chatbotResponsesMap.get(questionsList.get(i).toLowerCase());
                if ("en".equals(userLanguage)) {
                    addMessageToChat(answer);
                } else {
                    translateAndSendMessage(answer, userLanguage);
                }
                return;
            }
        }

        String closestKeyword = findClosestKeyword(cleanedMessage);
        Log.d("processMessage", "Closest keyword: " + closestKeyword);
        if (closestKeyword != null) {
            String answer = chatbotResponsesMap.get(closestKeyword);
            Log.d("processMessage", "Answer found for keyword: " + closestKeyword + ", Answer: " + answer);
            if ("en".equals(userLanguage)) {
                addMessageToChat(answer);
            } else {
                translateAndSendMessage(answer, userLanguage);
            }
        } else {
            generateSmartReply(cleanedMessage, userLanguage); // --------------- changes made for translation
        }
    }
    // ------------------------------- END OF: central method for checking if it is greeting msg, artist name, or if it is close to a keyword-------------------------------



    // ------------------------------- START OF: artist name check method -------------------------------
    private boolean checkForArtistMatch(String message) {
        String[] userWords = message.split(" ");
        for (Event event : eventsList) {
            String artist = event.getArtist().toLowerCase();

            // Check for full artist name match
            for (String userWord : userWords) {
                if (getLevenshteinDistance(userWord.toLowerCase(), artist) <= 1) {
                    sendEventIntroMessage(artist);
                    addEventMessageToChat(event);
                    return true;
                }
            }

            // Check for partial artist name match (two words from user match with artist name)
            for (int i = 0; i < userWords.length - 1; i++) {
                String combinedUserWords = userWords[i].toLowerCase() + " " + userWords[i + 1].toLowerCase();
                if (getLevenshteinDistance(combinedUserWords, artist) <= 2) {
                    sendEventIntroMessage(artist);
                    addEventMessageToChat(event);
                    return true;
                }
            }
        }
        return false;
    }
    // ------------------------------- END OF: artist name check method -------------------------------




    private void showGreetingMsg() {
        String welcomeMessage = "Hi my name is Joe Yi! Feel free to ask me any questions related to events!";
        messageList.add(new Message(welcomeMessage, false));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        runOnUiThread(() -> showSuggestedPrompts()); // Ensure this runs on the UI thread
    }


    private void generateSmartReply(String message, String userLanguage) {
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
                                translateAndSendMessage("I don't understand the question.", userLanguage); // --------------- changes made for translation
                                showSuggestedPrompts(); // --------------- changes made for translation
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
                                if (!suggestions.isEmpty()) {
                                    String reply = suggestions.get(0).getText();
                                    if (isGenericOrEmojiReply(reply)) {
                                        translateAndSendMessage("I don't understand the question.", userLanguage); // --------------- changes made for translation
                                        showSuggestedPrompts(); // --------------- changes made for translation
                                    } else {
                                        addMessageToChat(reply);
                                        hideSuggestedPrompts();
                                    }
                                    Log.d("SmartReply", "Reply: " + reply);
                                } else {
                                    translateAndSendMessage("I don't understand the question.", userLanguage); // --------------- changes made for translation
                                    showSuggestedPrompts(); // --------------- changes made for translation
                                    Log.d("SmartReply", "No suggestions available.");
                                }
                            }
                        } else {
                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
                        }
                    }
                });
    }


    private boolean isGenericOrEmojiReply(String reply) {
        List<String> genericReplies = Arrays.asList("Okay", "I don't know", "Sorry", "Alright", "Oh, okay", "😟", "😊", "😢");
        return genericReplies.contains(reply) || reply.matches("[\\p{So}\\p{Cn}]+");
    }

    private void sendEventIntroMessage(String artist) {
        String introMessage = "Here are some details about the event featuring " + artist + ". You can click on the event details page to find out more!";
        addMessageToChat(introMessage);
    }

    private void addEventMessageToChat(final Event event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event != null) {
                    Log.d("addEventMessageToChat", "Adding event message to chat: " + event.getArtist()); // Debugging log
                    if (messageList.isEmpty() || !messageList.get(messageList.size() - 1).getMessage().equals(event.getArtist())) {
                        messageList.add(new Message(event));
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    } else {
                        Log.d("addEventMessageToChat", "Duplicate event message not added."); // Debugging log
                    }
                } else {
                    Log.e("addEventMessageToChat", "Received null event"); // Debugging log
                }
            }
        });
    }


    private void addMessageToChat(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message != null) {
                    Log.d("addMessageToChat", "Adding message to chat: " + message); // Debugging log
                    if (messageList.isEmpty() || !messageList.get(messageList.size() - 1).getMessage().equals(message)) {
                        messageList.add(new Message(message, false));
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
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
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return levenshteinDistance.apply(s1, s2);
    }
    // ------------------------------- START OF: checking users prompt against keywords to get the levenshtein distance method -------------------------------



    // ------------------------------- START OF: matching users prompt to keywords methods -------------------------------
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
        if (minDistance <= 1) {
            return closestKeyword;
        } else {
            return null;
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
            translatePromptsToUserLanguage(userLanguage);
        } else {
            runOnUiThread(() -> {
                suggestedPromptAdapter.updateData(questionsList);
                suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
                suggestedPromptAdapter.notifyDataSetChanged();
                Log.d("showSuggestedPrompts", "Suggested prompts shown.");
            });
        }
    }


    private void translatePromptsToUserLanguage(final String targetLanguage) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> translatedPrompts = new ArrayList<>();
                    for (String prompt : questionsList) {
                        String translatedPrompt = translator.translate("en", targetLanguage, prompt);
                        translatedPrompts.add(translatedPrompt);
                        Log.d("translatePromptsToUserLanguage", "Original prompt: " + prompt + ", Translated prompt: " + translatedPrompt);
                    }
                    translatedQuestionsList = translatedPrompts; // Update the translated prompts list
                    runOnUiThread(() -> {
                        suggestedPromptAdapter.updateData(translatedPrompts);
                        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
                    });
                    Log.d("translatePromptsToUserLanguage", "Prompts updated in UI with translated language");
                } catch (Exception e) {
                    Log.e("translatePromptsToUserLanguage", "Error translating prompts: ", e);
                    runOnUiThread(() -> {
                        suggestedPromptAdapter.updateData(questionsList);
                        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }



    private void hideSuggestedPrompts() {
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

    private void onSuggestedPromptClick(String prompt) {
        messageInput.setText(prompt);
        messageInput.setSelection(prompt.length());
        suggestedPromptsRecyclerView.setVisibility(View.GONE);
    }

//    private void detectLanguageAndTranslatePrompts(String userLanguage) {
//        Log.d("detectLanguageAndTranslatePrompts", "Detecting language for prompts. User language: " + userLanguage); // Log the start of language detection
//        String textToDetect = questionsList.isEmpty() ? "" : questionsList.get(0);
//
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String detectedLanguage = translator.detectLanguage(textToDetect);
//                    Log.d("detectLanguageAndTranslatePrompts", "Detected language for prompts: " + detectedLanguage); // Log the detected language
//
//                    if (!"en".equals(userLanguage)) {
//                        translatePromptsToDetectedLanguage(userLanguage);
//                    } else {
//                        runOnUiThread(() -> {
//                            suggestedPromptAdapter.updateData(questionsList);
//                            suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
//                            Log.d("detectLanguageAndTranslatePrompts", "Prompts updated in UI with original language"); // Log when prompts are updated with the original language
//                        });
//                    }
//                } catch (Exception e) {
//                    Log.e("detectLanguageAndTranslatePrompts", "Error in detecting language: ", e);
//                    runOnUiThread(() -> {
//                        suggestedPromptAdapter.updateData(questionsList);
//                        suggestedPromptsRecyclerView.setVisibility(View.VISIBLE);
//                        Log.d("detectLanguageAndTranslatePrompts", "Fallback to original prompts in UI after detection error"); // Log when fallback to original prompts occurs
//                    });
//                }
//            }
//        });
//    }
//
//
//    private void translatePromptsToDetectedLanguage(final String targetLanguage) {
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    List<String> translatedPrompts = new ArrayList<>();
//                    for (String prompt : questionsList) {
//                        String translatedPrompt = translator.translate("en", targetLanguage, prompt);
//                        translatedPrompts.add(translatedPrompt);
//                        Log.d("translatePromptsToDetectedLanguage", "Original prompt: " + prompt + ", Translated prompt: " + translatedPrompt);
//                    }
//                    translatedQuestionsList = translatedPrompts; // Update the translated prompts list
//                    runOnUiThread(() -> suggestedPromptAdapter.updateData(translatedPrompts));
//                    runOnUiThread(() -> suggestedPromptsRecyclerView.setVisibility(View.VISIBLE));
//                    Log.d("translatePromptsToDetectedLanguage", "Prompts updated in UI with translated language");
//                } catch (Exception e) {
//                    Log.e("translatePromptsToDetectedLanguage", "Error translating prompts: ", e);
//                    runOnUiThread(() -> suggestedPromptAdapter.updateData(questionsList));
//                    runOnUiThread(() -> suggestedPromptsRecyclerView.setVisibility(View.VISIBLE));
//                }
//            }
//        });
//    }
    // ------------------------------- END OF: suugested prompt methods -------------------------------



    // ------------------------------- START OF: utility method -------------------------------
    private String cleanMessage(String message) {
        String normalizedMessage = Normalizer.normalize(message, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String cleanedMessage = pattern.matcher(normalizedMessage).replaceAll("");

        cleanedMessage = cleanedMessage.toLowerCase();
        cleanedMessage = cleanedMessage.replaceAll("\\bs\\b", "");
        cleanedMessage = cleanedMessage.replaceAll("[^a-zA-Z0-9\\s]", "");
        cleanedMessage = cleanedMessage.trim();

        return cleanedMessage;
    }
    // ------------------------------- END OF: utility method -------------------------------
}
