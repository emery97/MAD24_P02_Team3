package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private Button exitButton;
    private ChatAdapter chatAdapter;
    private ArrayList<Message> messageList;
    private dbHandler dbHandler;
    private HashMap<String, String> chatbotResponsesMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        exitButton = findViewById(R.id.exitButton);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        dbHandler = new dbHandler();
        chatbotResponsesMap = new HashMap<>();

        // Fetch chatbot responses from Firestore and store them in a map
        dbHandler.getChatbotResponses(new FirestoreCallback<ChatbotResponse>() {
            @Override
            public void onCallback(ArrayList<ChatbotResponse> list) {
                for (ChatbotResponse response : list) {
                    chatbotResponsesMap.put(response.getQuestion().toLowerCase(), response.getAnswer());
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

    private void handleBotResponse(String messageText) {
        String response = getBestResponse(messageText.toLowerCase());
        if (response != null) {
            messageList.add(new Message(response, false));
        } else {
            messageList.add(new Message("I'm not sure how to respond to that.", false));
        }
        chatAdapter.notifyDataSetChanged();
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

    private int getMatchScore(String messageText, String text) {
        // Normalize both messageText and text by removing punctuation and converting to lower case
        messageText = messageText.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
        text = text.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();

        String[] messageWords = messageText.split("\\s+");
        String[] textWords = text.split("\\s+");
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
}
