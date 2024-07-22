package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForumPage extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private EditText inputText;
    private ImageButton iconButton;
    private Spinner eventSpinner;
    private FirebaseFirestore db;
    private String selectedEvent;
    private View spinnerContainer;
    private View inputContainer;
    private ForumAdapter forumAdapter;
    private List<Forum> forumList;
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private ListenerRegistration forumListener;
    private Set<String> documentIds = new HashSet<>();
    private static final int PAGE_SIZE = 5;
    private String profilePicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum_page);

        eventSpinner = findViewById(R.id.event_spinner);
        inputText = findViewById(R.id.input_text);
        iconButton = findViewById(R.id.icon_button);
        spinnerContainer = findViewById(R.id.spinner_container);
        inputContainer = findViewById(R.id.input_container);
        Button createButton = findViewById(R.id.create_button);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userIdString = sharedPreferences.getString("UserId", null);
        String name = sharedPreferences.getString("Name", null);
        String email = sharedPreferences.getString("Email", null);

        fetchProfilePicUrl(userIdString);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupKeyboardListener();

        // Set up the Spinner with a placeholder item
        setupSpinnerWithPlaceholder();

        // Fetch event names from Firestore and update the Spinner
        fetchEventNames();

        RecyclerView recyclerView = findViewById(R.id.forum_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        forumList = new ArrayList<>();
        forumAdapter = new ForumAdapter(forumList);
        recyclerView.setAdapter(forumAdapter);

        setupRealTimeListener();

        // Set up button click listener
        iconButton.setOnClickListener(v -> {
            if (selectedEvent == null || selectedEvent.equals("Select an event")) {
                Toast.makeText(ForumPage.this, "Please select an event from the dropdown", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = inputText.getText().toString().trim();
            if (!message.isEmpty()) {
                // Create a Message object
                Forum messageData = new Forum(userIdString, name, email, message, selectedEvent,profilePicUrl);

                // Save the message data to Firestore
                db.collection("Forum").add(messageData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(ForumPage.this, "Message sent for event: " + selectedEvent, Toast.LENGTH_SHORT).show();
                            inputText.setText(""); // Clear the input text

                            fadeOutView(spinnerContainer);
                            fadeOutView(inputContainer);
                            createButton.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ForumPage.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(ForumPage.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        createButton.setOnClickListener(v -> {
            fadeInView(spinnerContainer);
            fadeInView(inputContainer);
            createButton.setVisibility(View.GONE); // Optionally hide the create button after clicked
        });

        View rootView = findViewById(R.id.main);
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inputContainer.getVisibility() == View.VISIBLE || spinnerContainer.getVisibility() == View.VISIBLE) {
                    float x = event.getX();
                    float y = event.getY();
                    if (!isTouchInsideView(inputContainer, x, y) && !isTouchInsideView(spinnerContainer, x, y)) {
                        if (inputText.getText().toString().trim().isEmpty()) {
                            fadeOutView(spinnerContainer);
                            fadeOutView(inputContainer);
                            createButton.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            return false;
        });
    }

    private void setupSpinnerWithPlaceholder() {
        // Create a list with a placeholder item
        List<String> eventNames = new ArrayList<>();
        eventNames.add("Select an event"); // Placeholder item

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);
    }

    private void fetchEventNames() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> eventNames = new ArrayList<>();
                        eventNames.add("Select an event"); // Add the placeholder item again
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventName = document.getString("Name");
                            if (eventName != null) {
                                eventNames.add(eventName);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ForumPage.this, R.layout.spinner_item, eventNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        eventSpinner.setAdapter(adapter);

                        // Set up Spinner item selected listener
                        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedEvent = parent.getItemAtPosition(position).toString();
                                if (selectedEvent.equals("Select an event")) {
                                    inputText.setEnabled(false); // Disable input
                                } else {
                                    inputText.setEnabled(true); // Enable input
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedEvent = null;
                                inputText.setEnabled(false); // Disable input
                            }
                        });
                    } else {
                        Toast.makeText(ForumPage.this, "Failed to fetch events", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean isTouchInsideView(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        return x > left && x < right && y > top && y < bottom;
    }

    private void fadeInView(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null);
    }

    private void fadeOutView(View view) {
        view.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void setupRealTimeListener() {
        forumListener = db.collection("Forum")
                .orderBy("timestamp")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(ForumPage.this, "Failed to listen for updates", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        List<Forum> updatedForums = new ArrayList<>();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            Forum forum = document.toObject(Forum.class);
                            if (forum != null && !documentIds.contains(document.getId())) {
                                updatedForums.add(forum);
                                documentIds.add(document.getId());
                            }
                        }
                        if (!updatedForums.isEmpty()) {
                            forumList.addAll(updatedForums);
                            forumAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }


    private void fetchProfilePicUrl(String userIdString) {
        if (userIdString == null || userIdString.isEmpty()) {
            Log.d("Firestore", "User ID is null or empty");
            profilePicUrl = ""; // Default to empty if user ID is null
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdString);
        } catch (NumberFormatException e) {
            Log.d("Firestore", "Invalid user ID format: " + userIdString);
            profilePicUrl = ""; // Default to empty if conversion fails
            return;
        }

        Log.d("Firestore", "Fetching profile picture URL for User ID: " + userId);

        db.collection("Account")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            if (document.exists()) {
                                profilePicUrl = document.getString("ProfilePicUrl");
                                Log.d("Firestore", "ProfilePicUrl: " + profilePicUrl);
                            } else {
                                Log.d("Firestore", "Document does not exist for userId: " + userId);
                                profilePicUrl = ""; // Default to empty if not found
                            }
                        } else {
                            Log.d("Firestore", "No documents found for userId: " + userId);
                            profilePicUrl = ""; // Default to empty if not found
                        }
                    } else {
                        Log.d("Firestore", "Error fetching profile picture URL", task.getException());
                        Toast.makeText(ForumPage.this, "Failed to fetch profile picture URL", Toast.LENGTH_SHORT).show();
                        profilePicUrl = ""; // Default to empty on failure
                    }
                });
    }

    private void setupKeyboardListener() {
        final View rootView = findViewById(R.id.main);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 is a threshold to determine if the keyboard is visible
                    // Keyboard is visible
                    adjustContainersForKeyboard(keypadHeight);
                } else {
                    // Keyboard is hidden
                    resetContainers();
                }
            }
        });
    }

    private void adjustContainersForKeyboard(int keyboardHeight) {
        // Adjust the layout of spinnerContainer and inputContainer based on the keyboardHeight
        int bottomMargin = keyboardHeight; // You can adjust this value as needed
        ViewGroup.LayoutParams paramsInput = inputContainer.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParamsInput = (ViewGroup.MarginLayoutParams) paramsInput;
        marginParamsInput.bottomMargin = bottomMargin;
        inputContainer.setLayoutParams(marginParamsInput);
    }

    private void resetContainers() {
        ViewGroup.LayoutParams paramsInput = inputContainer.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParamsInput = (ViewGroup.MarginLayoutParams) paramsInput;
        marginParamsInput.bottomMargin = 0;
        inputContainer.setLayoutParams(marginParamsInput);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the real-time listener when the activity is destroyed
        if (forumListener != null) {
            forumListener.remove();
        }
    }

}
