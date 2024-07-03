package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private ArrayList<Message> messageList;
    private dbHandler dbHandler;
    private HashMap<String, String> chatbotResponsesMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

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

        for (String question : chatbotResponsesMap.keySet()) {
            int matchScore = getMatchScore(messageText, question);
            if (matchScore > bestMatchScore) {
                bestMatchScore = matchScore;
                bestResponse = chatbotResponsesMap.get(question);
            }
        }

        return bestMatchScore > 0 ? bestResponse : null;
    }

    private int getMatchScore(String messageText, String question) {
        String[] messageWords = messageText.split("\\s+");
        String[] questionWords = question.split("\\s+");
        int matchCount = 0;

        for (String messageWord : messageWords) {
            for (String questionWord : questionWords) {
                if (messageWord.equalsIgnoreCase(questionWord)) {
                    matchCount++;
                }
            }
        }

        return matchCount;
    }
}
