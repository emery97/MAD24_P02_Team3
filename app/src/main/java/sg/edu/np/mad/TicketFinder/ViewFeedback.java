package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewFeedback extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private FeedbackDetailsAdapter feedbackAdapter;
    private List<Feedbackclass> feedbackList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_feedback);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            fetchFeedbackData(userId);
        }

        findViewById(R.id.feedbackclose).setOnClickListener(v -> finish());
    }

    private void fetchFeedbackData(String userId) {
        db.collection("Feedback")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            feedbackList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String category = document.getString("Category");
                                String message = document.getString("Message");
                                List<String> imageURIs = (List<String>) document.get("ImageURIs");

                                Feedbackclass feedback = new Feedbackclass(category, message, imageURIs);
                                feedbackList.add(feedback);
                            }
                            feedbackAdapter = new FeedbackDetailsAdapter(feedbackList);
                            recyclerView.setAdapter(feedbackAdapter);
                        } else {
                            Log.d("ViewFeedback", "No feedback found");
                        }
                    } else {
                        Log.d("ViewFeedback", "Error getting documents: ", task.getException());
                    }
                });
    }
}