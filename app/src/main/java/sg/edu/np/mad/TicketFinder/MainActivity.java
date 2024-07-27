package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Register.OnRegistrationSuccessListener, SignIn.OnLoginSuccessListener {
    private FirebaseFirestore db; // Firestore database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase
        EdgeToEdge.enable(this); // Enable edge-to-edge mode
        setContentView(R.layout.activity_main); // Set the content view to activity_main layout

        db = FirebaseFirestore.getInstance(); // Get Firestore instance
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE); // Get SharedPreferences instance

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Get system bar insets
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Set padding for system bars
            return insets; // Return the insets
        });

        // Get views for sign in/register buttons
        TextView signin = findViewById(R.id.signin); // Find the sign in TextView by its ID
        TextView register = findViewById(R.id.register); // Find the register TextView by its ID

        // Ensure the TextViews are not null
        if (signin == null || register == null) {
            throw new NullPointerException("signin or register TextView is null. Check the XML layout IDs."); // Throw exception if TextViews are null
        }

        // Remember login user
        if (sharedPreferences.contains("UserId")) {
            checkUserPreferences(sharedPreferences); // Check if preferences are set in Firestore
        } else {
            loadSignInFragment(); // Else, show sign in page
        }

        // Display sign in
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSignInFragment(); // Load sign in fragment
            }
        });

        // Display register
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegisterFragment(); // Load register fragment
            }
        });
    }

    private void checkUserPreferences(SharedPreferences sharedPreferences) {
        String userId = sharedPreferences.getString("UserId", null); // Get userId from SharedPreferences
        if (userId != null) {
            db.collection("Preferences").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            navigateToHomepage(); // Navigate to homepage if preferences exist
                        } else {
                            promptForPreferences(userId); // Prompt for preferences if not set
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to check preferences", Toast.LENGTH_SHORT).show(); // Show failure message
                        loadSignInFragment(); // Fallback to sign in fragment
                    });
        } else {
            loadSignInFragment(); // Load sign in fragment if userId is null
        }
    }

    private void navigateToHomepage() {
        Intent intent = new Intent(MainActivity.this, homepage.class); // Create intent for homepage
        startActivity(intent); // Start homepage activity
        finish(); // Finish MainActivity so the user can't navigate back to it
    }

    // Setting sign in fragment
    private void loadSignInFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction(); // Begin fragment transaction
        SignIn signInFragment = new SignIn(); // Create SignIn fragment instance
        signInFragment.setOnLoginSuccessListener(this); // Set login success listener
        transaction.replace(R.id.fraglayout, signInFragment); // Replace fragment layout with SignIn fragment
        transaction.commit(); // Commit the transaction
    }

    // Setting register fragment
    private void loadRegisterFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction(); // Begin fragment transaction
        Register registerFragment = new Register(); // Create Register fragment instance
        registerFragment.setOnRegistrationSuccessListener(this); // Set registration success listener
        transaction.replace(R.id.fraglayout, registerFragment); // Replace fragment layout with Register fragment
        transaction.commit(); // Commit the transaction
    }

    @Override
    public void onRegistrationSuccess() {
        loadSignInFragment(); // Load sign in fragment on registration success
    }

    @Override
    public void onLoginSuccess() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE); // Get SharedPreferences instance
        checkUserPreferences(sharedPreferences); // Check user preferences on login success
    }

    private void promptForPreferences(String userId) {
        setContentView(R.layout.activity_preferences); // Inflate the preferences layout

        Spinner genreSpinner1 = findViewById(R.id.genreSpinner1); // Find genreSpinner1 by its ID
        Spinner genreSpinner2 = findViewById(R.id.genreSpinner2); // Find genreSpinner2 by its ID
        Spinner genreSpinner3 = findViewById(R.id.genreSpinner3); // Find genreSpinner3 by its ID
        Button saveButton = findViewById(R.id.saveButton); // Find saveButton by its ID

        // Fetch event data and extract genres
        dbHandler handler = new dbHandler(); // Create dbHandler instance
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                ArrayList<String> genres = new ArrayList<>(); // Create list for genres
                for (Event event : eventList) {
                    String genre = event.getGenre(); // Get genre of the event
                    if (genre != null && !genres.contains(genre)) {
                        genres.add(genre); // Add genre to the list if not already present
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, genres); // Create ArrayAdapter for genres
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Set drop-down view resource

                genreSpinner1.setAdapter(adapter); // Set adapter for genreSpinner1
                genreSpinner2.setAdapter(adapter); // Set adapter for genreSpinner2
                genreSpinner3.setAdapter(adapter); // Set adapter for genreSpinner3
            }
        });

        saveButton.setOnClickListener(v -> {
            String choice1 = genreSpinner1.getSelectedItem().toString(); // Get selected item from genreSpinner1
            String choice2 = genreSpinner2.getSelectedItem().toString(); // Get selected item from genreSpinner2
            String choice3 = genreSpinner3.getSelectedItem().toString(); // Get selected item from genreSpinner3

            savePreferences(userId, choice1, choice2, choice3); // Save user preferences
        });
    }

    private void savePreferences(String userId, String choice1, String choice2, String choice3) {
        Map<String, Object> preferences = new HashMap<>(); // Create map for preferences
        preferences.put("userId", userId); // Put userId in the map
        preferences.put("choice1", choice1); // Put choice1 in the map
        preferences.put("choice2", choice2); // Put choice2 in the map
        preferences.put("choice3", choice3); // Put choice3 in the map

        db.collection("Preferences").document(userId)
                .set(preferences)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE); // Get SharedPreferences instance
                    sharedPreferences.edit().putBoolean("PreferencesSet", true).apply(); // Mark preferences as set in SharedPreferences
                    navigateToHomepage(); // Redirect to homepage after saving preferences
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to save preferences", Toast.LENGTH_SHORT).show()); // Show failure message
    }
}
