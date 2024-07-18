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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get views for sign up/register buttons
        TextView signin = findViewById(R.id.signin);
        TextView register = findViewById(R.id.register);

        // Ensure the TextViews are not null
        if (signin == null || register == null) {
            throw new NullPointerException("signin or register TextView is null. Check the XML layout IDs.");
        }

        // remember login user
        if (sharedPreferences.contains("UserId")) {
            checkUserPreferences(sharedPreferences); // Check if preferences are set in Firestore
        } else {
            loadSignInFragment(); // else, show sign in page
        }

        // display sign up
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSignInFragment();
            }
        });

        // display log in
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegisterFragment();
            }
        });
    }

    private void checkUserPreferences(SharedPreferences sharedPreferences) {
        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            db.collection("Preferences").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            navigateToHomepage();
                        } else {
                            promptForPreferences(userId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to check preferences", Toast.LENGTH_SHORT).show();
                        loadSignInFragment(); // Fallback to sign in fragment
                    });
        } else {
            loadSignInFragment();
        }
    }

    private void navigateToHomepage() {
        Intent intent = new Intent(MainActivity.this, homepage.class);
        startActivity(intent);
        finish();  // Finish MainActivity so the user can't navigate back to it
    }

    // setting sign in fragment
    private void loadSignInFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SignIn signInFragment = new SignIn();
        signInFragment.setOnLoginSuccessListener(this);
        transaction.replace(R.id.fraglayout, signInFragment);
        transaction.commit();
    }

    // setting login fragment
    private void loadRegisterFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Register registerFragment = new Register();
        registerFragment.setOnRegistrationSuccessListener(this);
        transaction.replace(R.id.fraglayout, registerFragment);
        transaction.commit();
    }

    @Override
    public void onRegistrationSuccess() {
        loadSignInFragment();
    }

    @Override
    public void onLoginSuccess() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        checkUserPreferences(sharedPreferences);
    }

    private void promptForPreferences(String userId) {
        setContentView(R.layout.activity_preferences); // Inflate the preferences layout

        Spinner genreSpinner1 = findViewById(R.id.genreSpinner1);
        Spinner genreSpinner2 = findViewById(R.id.genreSpinner2);
        Spinner genreSpinner3 = findViewById(R.id.genreSpinner3);
        Button saveButton = findViewById(R.id.saveButton);

        // Fetch event data and extract genres
        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                ArrayList<String> genres = new ArrayList<>();
                for (Event event : eventList) {
                    String genre = event.getGenre();
                    if (genre != null && !genres.contains(genre)) {
                        genres.add(genre);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, genres);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                genreSpinner1.setAdapter(adapter);
                genreSpinner2.setAdapter(adapter);
                genreSpinner3.setAdapter(adapter);
            }
        });

        saveButton.setOnClickListener(v -> {
            String choice1 = genreSpinner1.getSelectedItem().toString();
            String choice2 = genreSpinner2.getSelectedItem().toString();
            String choice3 = genreSpinner3.getSelectedItem().toString();

            savePreferences(userId, choice1, choice2, choice3);
        });
    }


    private void savePreferences(String userId, String choice1, String choice2, String choice3) {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("userId", userId);
        preferences.put("choice1", choice1);
        preferences.put("choice2", choice2);
        preferences.put("choice3", choice3);

        db.collection("Preferences").document(userId)
                .set(preferences)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean("PreferencesSet", true).apply();
                    navigateToHomepage(); // Redirect to homepage after saving preferences
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to save preferences", Toast.LENGTH_SHORT).show());
    }
}
