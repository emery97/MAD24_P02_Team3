package sg.edu.np.mad.TicketFinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class profilePage extends AppCompatActivity {
    // UI components
    private TextView username, password, email, regPassword;
    private EditText editUsername, editPassword;
    private ImageView editingIcon, profilePicture;
    private CheckBox showPassword;
    private Button saveButton, logoutButton, feedbackbutton;
    private String actualPassword;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String userId; // This should be the userId from Firestore document, not Firebase UID
    private static final String TAG = "ProfilePage";
    private boolean isEditMode = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Set up footer
        Footer.setUpFooter(this);

        // Initialize UI components
        username = findViewById(R.id.regUsername);
        password = findViewById(R.id.regPassword);
        email = findViewById(R.id.regEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        regPassword = findViewById(R.id.regPassword);
        editingIcon = findViewById(R.id.editingIcon);
        profilePicture = findViewById(R.id.profilePicture);
        showPassword = findViewById(R.id.showPassword);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        feedbackbutton = findViewById(R.id.Viewfeedbackbtn);

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Initialise database components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // Check if user is authenticated
        if (firebaseUser == null) {
            // Redirect to login page if user is not authenticated
            Log.e(TAG, "FirebaseUser is null, redirecting to login");
            Intent intent = new Intent(profilePage.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Get userId from SharedPreferences
        userId = sharedPreferences.getString("userId", "N/A");
        if (userId.equals("N/A")) {
            // Attempt to fetch the userId from Firestore using the email
            fetchUserId();
        } else {
            // Load user data
            loadUserData();
        }

        // OnClickListener for show password checkbox
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    togglePasswordVisibility();
                } else {
                    togglePasswordVisibilityNonEditMode();
                }
            }
        });

        // OnClickListener for editing icon
        editingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                AllowEditing();
                saveButton.setVisibility(View.VISIBLE); // Ensure save button is visible when editing
                // Feedback and Logout buttons are invisible
                feedbackbutton.setVisibility(View.INVISIBLE);
                logoutButton.setVisibility(View.INVISIBLE);
            }
        });

        // OnClickListener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get password from EditText
                actualPassword = editPassword.getText().toString();
                // Disable editing mode
                UnallowEditing();
                isEditMode = false;
                // Update user information
                updateUserInformation();
            }
        });

        // OnClickListener for logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout
                logout();
            }
        });

        // OnClickListener for feedback button
        feedbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to feedback activity
                Intent intent = new Intent(profilePage.this, ViewFeedback.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear SharedPreferences if "RememberMe" is false
        if (!sharedPreferences.getBoolean("RememberMe", false)) {
            sharedPreferences.edit().clear().apply();
        }
    }

    // Method to fetch userId from Firestore using email
    private void fetchUserId() {
        // Match account email to entry in database
        db.collection("Account").whereEqualTo("Email", firebaseUser.getEmail()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get userId
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        userId = documentSnapshot.getId();

                        // Save userId to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userId", userId);
                        editor.apply();

                        // Load user data
                        loadUserData();
                    } else { // No matches in database
                        Log.e(TAG, "No matching document found in Firestore");
                        Toast.makeText(this, "No user data found. Please log in again.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        Intent intent = new Intent(profilePage.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> { // Error handling
                    Log.e(TAG, "Error fetching userId from Firestore", e);
                    Toast.makeText(this, "Error fetching user data. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to load user data
    // Referenced from chatgpt
    private void loadUserData() {
        if (firebaseUser != null) {
            // Set username and email from SharedPreferences
            username.setText(sharedPreferences.getString("Name", "N/A"));
            email.setText(firebaseUser.getEmail());
            // Set actual password from SharedPreferences
            actualPassword = sharedPreferences.getString("Password", "N/A");
            password.setText(actualPassword);

            Log.d(TAG, "Loaded user data: " + firebaseUser.getDisplayName() + ", " + firebaseUser.getEmail() + ", userId: " + userId);

            // Fetch the latest user data from Firestore
            db.collection("Account").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Set username and email from Firestore
                    username.setText(documentSnapshot.getString("Name"));
                    email.setText(documentSnapshot.getString("Email"));
                    // Set actual password from Firestore
                    actualPassword = documentSnapshot.getString("Password");
                    password.setText(actualPassword);

                    // Save the latest data to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", documentSnapshot.getString("Name"));
                    editor.putString("Email", documentSnapshot.getString("Email"));
                    editor.putString("Password", documentSnapshot.getString("Password"));
                    editor.apply();

                    Log.d(TAG, "User data refreshed from Firestore");
                } else { // No user found
                    Log.e(TAG, "No such document in Firestore");
                }
                // Error handling
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user data from Firestore", e));
        } else {
            Log.e(TAG, "FirebaseUser is null in loadUserData");
        }
    }

    // Method to toggle password visibility in edit mode
    // Referenced from chatgpt
    private void togglePasswordVisibility() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibility: checked");
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editPassword.setSelection(editPassword.getText().length());
    }

    // Method to toggle password visibility in non-edit mode
    private void togglePasswordVisibilityNonEditMode() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibilityNonEditMode: checked");
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        regPassword.requestLayout();
    }

    // Method to allow editing user information
    private void AllowEditing() {
        // Regular fields not shown
        username.setVisibility(View.GONE);
        password.setVisibility(View.GONE);

        // Replaced by editing fields
        editUsername.setVisibility(View.VISIBLE);
        editPassword.setVisibility(View.VISIBLE);
        // Setting account data in editing fields
        editUsername.setText(username.getText());
        editPassword.setText(password.getText());

        // Display save button
        saveButton.setVisibility(View.VISIBLE);
    }

    // Method to disable editing user information
    private void UnallowEditing() {
        // Regular fields shown
        username.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);

        // Editing fields and save button not shown
        editUsername.setVisibility(View.GONE);
        editPassword.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
    }

    // Method to logout user
    private void logout() {
        mAuth.signOut();
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login/register page
        Intent intent = new Intent(profilePage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Method to update user information in Firestore
    private void updateFirestore(String updatedName, String updatedEmail, String updatedPassword) {
        // Post to database
        db.collection("Account").document(userId)
                .update("Name", updatedName, "Password", updatedPassword)
                .addOnSuccessListener(aVoid -> { // Upon posting
                    // Update SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", updatedName);
                    editor.putString("Password", updatedPassword);
                    editor.apply();

                    Log.d(TAG, "Profile updated successfully in Firestore.");
                    Toast.makeText(profilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // Reload user data to reflect changes
                    loadUserData();
                })
                .addOnFailureListener(e -> { // Error handling
                    Log.e(TAG, "Failed to update profile in Firestore", e);
                    Toast.makeText(profilePage.this, "Failed to update profile in Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to update user information
    // Referenced from chatgpt
    private void updateUserInformation() {
        // Get updated name and password
        String updatedName = editUsername.getText().toString();
        String updatedPassword = editPassword.getText().toString();

        Log.d(TAG, "Updating user information with updatedName: " + updatedName + ", updatedPassword: " + updatedPassword);

        if (firebaseUser != null) { // Check if user exists
            // Get email and password of user
            String currentEmail = firebaseUser.getEmail();
            String currentPassword = sharedPreferences.getString("Password", "N/A");

            if (currentEmail != null && !currentPassword.equals("N/A")) { // Check if user has both email and password
                Log.d(TAG, "Current email: " + currentEmail + ", Current password: " + currentPassword); // Log current email and password

                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    // Reauthenticate user
                    if (task.isSuccessful()) { //
                        Log.d(TAG, "User re-authenticated.");
                        Toast.makeText(this, "Re-Authentication success.", Toast.LENGTH_SHORT).show();

                        // Update password in Firebase Auth
                        firebaseUser.updatePassword(updatedPassword).addOnCompleteListener(passwordUpdateTask -> {
                            if (passwordUpdateTask.isSuccessful()) {
                                Log.d(TAG, "User password updated.");

                                // Update profile in Firestore
                                updateFirestore(updatedName, currentEmail, updatedPassword);
                            } else {
                                Log.e(TAG, "Error updating password in Firebase Auth", passwordUpdateTask.getException());
                                Toast.makeText(profilePage.this, "Failed to update password in Firebase Auth", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Log.e(TAG, "User re-authentication failed", task.getException());
                        Toast.makeText(profilePage.this, "User re-authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Current email or password is missing");
                Toast.makeText(profilePage.this, "Current email or password is missing", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "FirebaseUser is null");
            Toast.makeText(profilePage.this, "Failed to update profile: FirebaseUser not found", Toast.LENGTH_SHORT).show();
        }
    }
}
