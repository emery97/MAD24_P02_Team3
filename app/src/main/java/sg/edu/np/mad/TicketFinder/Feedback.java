package sg.edu.np.mad.TicketFinder;

import static android.content.ContentValues.TAG;
import static com.google.android.material.internal.ContextUtils.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final int PICK_IMAGE_REQUEST = 1;
    private SharedPreferences sharedPreferences;
    private Spinner feedback;
    private Button submitButton;
    private Button exitButton;
    private EditText message;
    private Button attachImageButton;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
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

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        String documentId = sharedPreferences.getString("Document", null);
        String userId = sharedPreferences.getString("UserId", null);
        String name = sharedPreferences.getString("Name", null);
        String email = sharedPreferences.getString("Email", null);

        feedback = findViewById(R.id.Feedbacktype);
        submitButton = findViewById(R.id.submitbutton_feedback);
        exitButton = findViewById(R.id.exitbutton_feedback);
        message = findViewById(R.id.Feedback_msg);
        attachImageButton = findViewById(R.id.attach_image_button);
        imageView = findViewById(R.id.imageView);
        recyclerView = findViewById(R.id.recyclerView);

        imageUris = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(imageUris);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(feedbackAdapter);
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback(documentId,name, email, userId);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHomepage();
            }
        });
    }

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
            imageUri = data.getData();
            imageUris.add(imageUri);
            feedbackAdapter.notifyDataSetChanged();
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void submitFeedback(String documentId,String name, String email,String userId) {
        String feedbackType = feedback.getSelectedItem().toString();
        String feedbackMessage = message.getText().toString();
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);
        feedbackData.put("Name", name);
        feedbackData.put("Email", email);
        feedbackData.put("Message", feedbackMessage);
        feedbackData.put("Category", feedbackType);
        
        if (!imageUris.isEmpty()) {
            List<String> imageUrisString = new ArrayList<>();
            for (Uri uri : imageUris) {
                imageUrisString.add(uri.toString());
            }
            feedbackData.put("ImageURIs", imageUrisString);
        }


        db.collection("Feedback")
                .add(feedbackData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Feedback.this, "Feedback sent", Toast.LENGTH_SHORT).show();
                        message.setText("");
                        imageView.setImageURI(null);
                        imageUri = null;
                        navigateToHomepage();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Feedback.this, "Feedback not sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHomepage() {
        Toast.makeText(Feedback.this, "Going back to homepage", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Feedback.this, homepage.class);
        startActivity(intent);
        Feedback.this.finish();
    }

}