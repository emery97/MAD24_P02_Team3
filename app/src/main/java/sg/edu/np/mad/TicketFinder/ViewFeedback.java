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
    // UI
    private RecyclerView recyclerView;
    // Adapter and feedbackList
    private FeedbackDetailsAdapter feedbackAdapter;
    private List<Feedbackclass> feedbackList = new ArrayList<>();
    // SharedPreferences and Firebase
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_feedback);

        // Initialize Firestore and SharedPreferences
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve user ID from SharedPreferences
        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            // Fetch feedback data based on the user ID
            fetchFeedbackData(userId);
        }

        // Close page OnClickListener
        findViewById(R.id.feedbackclose).setOnClickListener(v -> finish());
    }

    // Method to fetch feedback data from Firestore based on user ID
    private void fetchFeedbackData(String userId) {
        // Matching userId to database userId
        db.collection("Feedback")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if the query result is not empty
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear the existing feedback list
                            feedbackList.clear();
                            // Iterate through the query results
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Retrieve feedback details from Firestore document
                                String category = document.getString("Category");
                                String message = document.getString("Message");
                                List<String> imageURIs = (List<String>) document.get("ImageURIs");

                                // Create Feedback object and add it to the feedback list
                                Feedbackclass feedback = new Feedbackclass(category, message, imageURIs);
                                feedbackList.add(feedback);
                            }
                            // Set list in recyclerview
                            feedbackAdapter = new FeedbackDetailsAdapter(feedbackList);
                            recyclerView.setAdapter(feedbackAdapter);
                        } else {
                            // Log message if no feedback found for the user
                            Log.d("ViewFeedback", "No feedback found");
                        }
                    } else {
                        // Log error message if there is an error fetching documents
                        Log.d("ViewFeedback", "Error getting documents: ", task.getException());
                    }
                });
    }
}
