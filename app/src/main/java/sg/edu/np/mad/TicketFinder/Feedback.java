package sg.edu.np.mad.TicketFinder;

import static android.content.ContentValues.TAG;
import static com.google.android.material.internal.ContextUtils.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feedback extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image picker intent
    private SharedPreferences sharedPreferences; // SharedPreferences to store and retrieve user data
    private Spinner feedback;
    private Button submitButton;
    private Button exitButton;
    private EditText message;
    private Button attachImageButton;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseFirestore db;
    private RecyclerView recyclerView; // To display list of selected images
    private FeedbackAdapter feedbackAdapter;
    private List<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences to retrieve stored user data
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Retrieve stored user data from SharedPreferences
        String documentId = sharedPreferences.getString("Document", null);
        String userId = sharedPreferences.getString("UserId", null);
        String name = sharedPreferences.getString("Name", null);
        String email = sharedPreferences.getString("Email", null);

        // Initialize UI components
        feedback = findViewById(R.id.Feedbacktype);
        submitButton = findViewById(R.id.submitbutton_feedback);
        exitButton = findViewById(R.id.exitbutton_feedback);
        message = findViewById(R.id.Feedback_msg);
        attachImageButton = findViewById(R.id.attach_image_button);
        imageView = findViewById(R.id.imageView);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize list and adapter for RecyclerView
        imageUris = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(feedbackAdapter);

        // Set click listener for attach image button
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(); // Open file chooser to select image
            }
        });

        // Set click listener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback(documentId, name, email, userId); // Submit feedback to Firestore
            }
        });

        // Set click listener for exit button
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHomepage();
            }
        });
    }

    // Method to open file chooser for image selection (Coded with the help of ChatGPT)
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Get URI of selected image
            imageUris.add(imageUri); // Add URI to list of image URIs
            feedbackAdapter.notifyDataSetChanged(); // Notify adapter that data set has changed
            imageView.setVisibility(View.VISIBLE); // Show image view if an image is selected
        }
    }

    // Method to submit feedback to Firestore
    private void submitFeedback(String documentId, String name, String email, String userId) {
        // Get selected feedback type and inputted feedback message
        String feedbackType = feedback.getSelectedItem().toString(); // Get selected feedback type
        String feedbackMessage = message.getText().toString(); // Get feedback message

        // Create a map to store feedback data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);
        feedbackData.put("Name", name);
        feedbackData.put("Email", email);
        feedbackData.put("Message", feedbackMessage);
        feedbackData.put("Category", feedbackType);

        // Add image URIs to feedback data if any images are selected
        if (!imageUris.isEmpty()) {
            List<String> imageUrisString = new ArrayList<>();
            for (Uri uri : imageUris) {
                imageUrisString.add(uri.toString()); // Convert URI to string and add to list
            }
            feedbackData.put("ImageURIs", imageUrisString); // Add list of image URIs to feedback data
        }

        // Add feedback data to Firestore
        db.collection("Feedback")
                .add(feedbackData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Show success message and reset input fields
                        Toast.makeText(Feedback.this, "Feedback sent", Toast.LENGTH_SHORT).show();
                        message.setText("");
                        imageView.setImageURI(null);
                        imageUri = null;
                        navigateToHomepage();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show failure message
                        Toast.makeText(Feedback.this, "Feedback not sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to navigate back to the homepage
    private void navigateToHomepage() {
        // Show toast message and start homepage activity
        Toast.makeText(Feedback.this, "Going back to homepage", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Feedback.this, homepage.class);
        startActivity(intent);
        Feedback.this.finish();
    }

    // Changing orientation without restarting
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { //(Coded with the help of chatGPT)
        super.onSaveInstanceState(outState);
        // Save the current state of imageUris list
        outState.putParcelableArrayList("imageUris", new ArrayList<>(imageUris));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { //(Coded with the help of chatGPT)
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore the imageUris list from the saved state
            ArrayList<Uri> savedImageUris = savedInstanceState.getParcelableArrayList("imageUris");
            if (savedImageUris != null) {
                imageUris.clear();
                imageUris.addAll(savedImageUris);
                // Notify the adapter that data set has changed
                feedbackAdapter.notifyDataSetChanged();
                // If there are images, make the ImageView visible
                if (!imageUris.isEmpty()) {
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}