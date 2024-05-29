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
    private TextView username, password, email,regPassword;
    private EditText editUsername, editPassword;
    private ImageView editingIcon, profilePicture;
    private CheckBox showPassword;
    private Button saveButton, logoutButton;
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

        // initialize UI components
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
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            // Redirect to login page if user is not authenticated
            Log.e(TAG, "FirebaseUser is null, redirecting to login");
            Intent intent = new Intent(profilePage.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        userId = sharedPreferences.getString("userId", "N/A");
        if (userId.equals("N/A")) {
            // Attempt to fetch the userId from Firestore using the email
            fetchUserId();
        } else {
            loadUserData();
        }

        // onclicklistener for showing password
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

        // onclicklistener for editing
        editingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                AllowEditing();
                saveButton.setVisibility(View.VISIBLE); // Ensure save button is visible when editing
            }
        });

        // onclicklistener for saving
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualPassword = editPassword.getText().toString();
                UnallowEditing();
                isEditMode = false;
                updateUserInformation();
            }
        });

        // onclicklistener for logging out
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!sharedPreferences.getBoolean("RememberMe", false)) {
            sharedPreferences.edit().clear().apply();
        }
    }

    private void fetchUserId() {
        db.collection("Account").whereEqualTo("Email", firebaseUser.getEmail()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        userId = documentSnapshot.getId();

                        // Save userId to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userId", userId);
                        editor.apply();

                        loadUserData();
                    } else {
                        Log.e(TAG, "No matching document found in Firestore");
                        Toast.makeText(this, "No user data found. Please log in again.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        Intent intent = new Intent(profilePage.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching userId from Firestore", e);
                    Toast.makeText(this, "Error fetching user data. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserData() {
        if (firebaseUser != null) {
            username.setText(sharedPreferences.getString("Name", "N/A"));
            email.setText(firebaseUser.getEmail());
            actualPassword = sharedPreferences.getString("Password", "N/A");
            password.setText(actualPassword);

            Log.d(TAG, "Loaded user data: " + firebaseUser.getDisplayName() + ", " + firebaseUser.getEmail() + ", userId: " + userId);

            // Fetch the latest user data from Firestore
            db.collection("Account").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    username.setText(documentSnapshot.getString("Name"));
                    email.setText(documentSnapshot.getString("Email"));
                    actualPassword = documentSnapshot.getString("Password");
                    password.setText(actualPassword);

                    // Save the latest data to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", documentSnapshot.getString("Name"));
                    editor.putString("Email", documentSnapshot.getString("Email"));
                    editor.putString("Password", documentSnapshot.getString("Password"));
                    editor.apply();

                    Log.d(TAG, "User data refreshed from Firestore");
                } else {
                    Log.e(TAG, "No such document in Firestore");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user data from Firestore", e));
        } else {
            Log.e(TAG, "FirebaseUser is null in loadUserData");
        }
    }

    private void togglePasswordVisibility() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibility: checked");
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editPassword.setSelection(editPassword.getText().length());
    }

    private void togglePasswordVisibilityNonEditMode() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibilityNonEditMode: checked");
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        regPassword.requestLayout();
    }


    private void AllowEditing() {
        username.setVisibility(View.GONE);
        password.setVisibility(View.GONE);

        editUsername.setVisibility(View.VISIBLE);
        editPassword.setVisibility(View.VISIBLE);

        editUsername.setText(username.getText());
        editPassword.setText(password.getText());

        saveButton.setVisibility(View.VISIBLE);
    }

    private void UnallowEditing() {
        username.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);

        editUsername.setVisibility(View.GONE);
        editPassword.setVisibility(View.GONE);

        saveButton.setVisibility(View.GONE);
    }

    private void logout() {
        mAuth.signOut();
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(profilePage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateFirestore(String updatedName, String updatedEmail, String updatedPassword) {
        db.collection("Account").document(userId)
                .update("Name", updatedName, "Password", updatedPassword)
                .addOnSuccessListener(aVoid -> {
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
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update profile in Firestore", e);
                    Toast.makeText(profilePage.this, "Failed to update profile in Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInformation() {
        String updatedName = editUsername.getText().toString();
        String updatedPassword = editPassword.getText().toString();

        Log.d(TAG, "Updating user information with updatedName: " + updatedName + ", updatedPassword: " + updatedPassword);

        if (firebaseUser != null) {
            // Reauthenticate the user with current credentials
            String currentEmail = firebaseUser.getEmail();
            String currentPassword = sharedPreferences.getString("Password", "N/A");

            if (currentEmail != null && !currentPassword.equals("N/A")) {
                Log.d(TAG, "Current email: " + currentEmail + ", Current password: " + currentPassword); // Log current email and password

                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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
