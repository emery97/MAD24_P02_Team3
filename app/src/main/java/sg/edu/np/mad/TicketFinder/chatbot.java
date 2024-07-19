package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class chatbot extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editText;
    ArrayList<MessageModel> list;
    ChatBotAdapter adapter;
    FirebaseFirestore db;
    FirebaseSmartReply smartReply;

    private final String user = "user";
    private final String bot = "bot";
    private final Map<String, String> keywordMapping = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit);
        list = new ArrayList<>();
        adapter = new ChatBotAdapter(this, list);
        db = FirebaseFirestore.getInstance();
        smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        // Initialize keyword mappings
        initializeKeywordMappings();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        sendWelcomeMessage();

        editText.setOnTouchListener(new View.OnTouchListener() {
           @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        String message = editText.getText().toString().trim();
                        if (!message.isEmpty()) {
                            list.add(new MessageModel(message, user, getCurrentTime()));
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                            handleUserMessage(message);
                            editText.setText("");
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // Close button functionality
        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void initializeKeywordMappings() {
        // Add mappings from keywords to document IDs
        keywordMapping.put("payment", "payment_methods");
        keywordMapping.put("order", "order");
        keywordMapping.put("track", "track_order");
        // Add more mappings as needed
    }

    private void sendWelcomeMessage() {
        String welcomeMessage = "Welcome! How can I assist you today? You can ask me questions like 'how to pay' or 'what are the payment methods'.";
        list.add(new MessageModel(welcomeMessage, bot, getCurrentTime()));
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(list.size() - 1);
    }

    private void handleUserMessage(String message) {
        String documentId = getDocumentIdForMessage(message);
        if (documentId != null) {
            fetchFAQAnswer(documentId);
        } else {
            // Check for typos and find the closest matching keyword
            String closestKeyword = findClosestKeyword(message);
            if (closestKeyword != null) {
                list.add(new MessageModel("Did you mean '" + closestKeyword + "'?", bot, getCurrentTime()));
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(list.size() - 1);
            } else {
                // If no keyword is found, generate Smart Reply suggestions
                generateSmartReply(message);
            }
        }
    }




    private String getDocumentIdForMessage(String message) {
        for (String keyword : keywordMapping.keySet()) {
            if (message.toLowerCase().contains(keyword)) {
                return keywordMapping.get(keyword);
            }
        }
        return null;
    }


    private void generateSmartReply(String message) {
        List<FirebaseTextMessage> conversation = new ArrayList<>();
        for (MessageModel msg : list) {
            if (msg.getSender().equals(user)) {
                conversation.add(FirebaseTextMessage.createForLocalUser(msg.getMessage(), System.currentTimeMillis()));
            } else {
                conversation.add(FirebaseTextMessage.createForRemoteUser(msg.getMessage(), System.currentTimeMillis(), bot));
            }
        }

        smartReply.suggestReplies(conversation)
                .addOnCompleteListener(new OnCompleteListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SmartReplySuggestionResult> task) {
                        if (task.isSuccessful()) {
                            SmartReplySuggestionResult result = task.getResult();
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime()));
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
                                if (!suggestions.isEmpty()) {
                                    String reply = suggestions.get(0).getText();
                                    list.add(new MessageModel(reply, bot, getCurrentTime()));
                                    Log.d("SmartReply", "Reply: " + reply);
                                } else {
                                    list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime()));
                                    Log.d("SmartReply", "No suggestions available.");
                                }
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        } else {
                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
                        }
                    }
                });
    }
    private void fetchFAQAnswer(String documentId) {
        CollectionReference faqsRef = db.collection("FAQs");
        faqsRef.document(documentId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String answer = task.getResult().getString("answer");
                            list.add(new MessageModel(answer, bot, getCurrentTime()));
                        } else {
                            list.add(new MessageModel("Failed to fetch the answer.", bot, getCurrentTime()));
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(list.size() - 1);
                    }
                });
    }

    private int getLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }


    private String findClosestKeyword(String message) {
        String closestKeyword = null;
        int minDistance = Integer.MAX_VALUE;

        for (String keyword : keywordMapping.keySet()) {
            int distance = getLevenshteinDistance(message.toLowerCase(), keyword.toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                closestKeyword = keyword;
            }
        }

        // Consider a keyword only if the distance is below or equal to a certain threshold (e.g., 2)
        return minDistance <= 2 ? closestKeyword : null;
    }




    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }
}
