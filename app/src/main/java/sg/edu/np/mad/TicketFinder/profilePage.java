package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class profilePage extends AppCompatActivity {
    // attributes
    private TextView username, password, email, regUsername, regPassword, regEmail;
    private ImageView editingIcon, profilePicture;
    private CheckBox showPassword;
    private Button saveButton, logoutButton;
    private String actualPassword;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userId;
    private static final String TAG = "ProfilePage";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_page);

        // initialize UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        regUsername = findViewById(R.id.regUsername);
        regPassword = findViewById(R.id.regPassword);
        regEmail = findViewById(R.id.regEmail);
        editingIcon = findViewById(R.id.editingIcon);
        profilePicture = findViewById(R.id.profilePicture);
        showPassword = findViewById(R.id.showPassword);
        saveButton = findViewById(R.id.saveButton); // initialize saveButton
        logoutButton = findViewById(R.id.logoutButton); // initialize logoutButton
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        loadUserData();

        // Set up footer buttons
        Footer.setUpFooter(this);

        // onclicklistener for showing password
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        // onclicklistener for editing
        editingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowEditing();
                saveButton.setVisibility(View.VISIBLE);
            }
        });

        // onclicklistener for saving
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save updated password
                actualPassword = ((EditText) regPassword).getText().toString(); // Ensure it's from EditText

                // Revert EditText fields back to TextView fields
                UnallowEditing();
                saveButton.setVisibility(View.INVISIBLE);

                // Update user data in Firestore
                updateUserInformation();

                // Refresh password visibility state
                updatePasswordVisibilityState();
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

    private void loadUserData() {
        String name = sharedPreferences.getString("Name", "N/A");
        String email = sharedPreferences.getString("Email", "N/A");
        String password = sharedPreferences.getString("Password", "N/A");
        userId = sharedPreferences.getString("UserId", "N/A");

        regUsername.setText(name);
        regEmail.setText(email);
        regPassword.setText(password);
        actualPassword = password;  // Ensure actualPassword is updated with the actual password
        Log.d(TAG, "Loaded user data: " + name + ", " + email + ", " + userId);
    }

    // Toggle password visibility based on checkbox
    private void togglePasswordVisibility() {
        if (showPassword.isChecked()) {
            // Show password
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            // Hide password
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (regPassword instanceof EditText) {
            ((EditText) regPassword).setSelection(regPassword.getText().length()); // Ensure cursor is at the end of the text
        } else {
            updatePasswordVisibilityState();
        }
    }

    // Update password visibility state after saving
    private void updatePasswordVisibilityState() {
        if (showPassword.isChecked()) {
            regPassword.setText(actualPassword);
        } else {
            regPassword.setText(hidingText(actualPassword));
        }
    }

    // method to edit - creates EditText fields and copies existing properties from TextView
    private void AllowEditing() {
        // Replace TextView with EditText for username
        // Removing TextView regUsername
        ViewGroup parentRegUsername = (ViewGroup) regUsername.getParent();
        int usernameIndex = parentRegUsername.indexOfChild(regUsername);
        String usernameText = regUsername.getText().toString();
        parentRegUsername.removeView(regUsername);

        // Adding new EditText to replace regUsername
        EditText editUsername = new EditText(this);
        editUsername.setLayoutParams(regUsername.getLayoutParams()); // Use regUsername's layout params
        editUsername.setText(usernameText);

        // Changing marginTop of editUsername
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) editUsername.getLayoutParams();
        layoutParams.topMargin = 12;

        parentRegUsername.addView(editUsername, usernameIndex);

        // Replace TextView with EditText for password
        ViewGroup parentPassword = (ViewGroup) regPassword.getParent();
        int passwordIndex = parentPassword.indexOfChild(regPassword);
        parentPassword.removeView(regPassword);

        // Adding new EditText to replace regPassword
        EditText editPassword = new EditText(this);
        editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPassword.setText(actualPassword);  // Set the actual password
        editPassword.setLayoutParams(regPassword.getLayoutParams());

        parentPassword.addView(editPassword, passwordIndex);

        // Replace TextView with EditText for email
        ViewGroup parentEmail = (ViewGroup) regEmail.getParent();
        int emailIndex = parentEmail.indexOfChild(regEmail);
        parentEmail.removeView(regEmail);

        EditText editEmail = new EditText(this);
        editEmail.setLayoutParams(regEmail.getLayoutParams());
        editEmail.setText(regEmail.getText());
        parentEmail.addView(editEmail, emailIndex);

        // Save references to the EditText fields
        regUsername = editUsername;
        regEmail = editEmail;
        regPassword = editPassword;
    }

    // Method to revert EditText fields back to TextView fields
    private void UnallowEditing() {
        ViewGroup parentRegUsername = (ViewGroup) regUsername.getParent();
        int usernameIndex = parentRegUsername.indexOfChild(regUsername);
        String usernameText = regUsername.getText().toString();
        parentRegUsername.removeView(regUsername);

        // Adding new TextView to replace EditText for username
        TextView textUsername = new TextView(this);
        textUsername.setLayoutParams(regUsername.getLayoutParams()); // Use regUsername's layout params
        textUsername.setText(usernameText);

        // Changing marginTop of textUsername
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textUsername.getLayoutParams();
        layoutParams.topMargin = 12;
        textUsername.setTextSize(20);
        parentRegUsername.addView(textUsername, usernameIndex);

        // Remove reference to EditText field
        regUsername = textUsername;

        // Replace EditText with TextView for email
        ViewGroup parentEmail = (ViewGroup) regEmail.getParent();
        int emailIndex = parentEmail.indexOfChild(regEmail);
        String emailText = regEmail.getText().toString();
        parentEmail.removeView(regEmail);

        // Adding new TextView to replace EditText for email
        TextView textEmail = new TextView(this);
        textEmail.setLayoutParams(regEmail.getLayoutParams());
        textEmail.setText(emailText);
        textEmail.setTextSize(20);
        parentEmail.addView(textEmail, emailIndex);

        // Remove reference to EditText field
        regEmail = textEmail;

        // Replace EditText with TextView for password
        ViewGroup parentPassword = (ViewGroup) regPassword.getParent();
        int passwordIndex = parentPassword.indexOfChild(regPassword);
        String passwordText = regPassword.getText().toString();
        parentPassword.removeView(regPassword);

        // Adding new TextView to replace EditText for password
        TextView textPassword = new TextView(this);
        textPassword.setLayoutParams(regPassword.getLayoutParams());
        textPassword.setTextSize(20); // Set the text size on the TextView object
        if (showPassword.isChecked()) {
            textPassword.setText(actualPassword); // Show the actual password if checkbox is checked
        } else {
            textPassword.setText(hidingText(actualPassword)); // Otherwise, show bullets
        }
        parentPassword.addView(textPassword, passwordIndex);

        // Increasing margintop
        ViewGroup.MarginLayoutParams passwordLayout = (ViewGroup.MarginLayoutParams) password.getLayoutParams();
        passwordLayout.topMargin = 100;
        ViewGroup.MarginLayoutParams regPasswordLayout = (ViewGroup.MarginLayoutParams) textPassword.getLayoutParams();
        regPasswordLayout.topMargin = 50;

        // Remove reference to EditText field
        regPassword = textPassword;
    }

    // hide text after making password back to textview
    private String hidingText(String originalText) {
        StringBuilder hiddenText = new StringBuilder();
        for (int i = 0; i < originalText.length(); i++) {
            hiddenText.append("\u2022");
        }
        return hiddenText.toString();
    }

    // Logout method to clear shared preferences and navigate to login screen
    private void logout() {
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(profilePage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Update user information in Firestore
    private void updateUserInformation() {
        String updatedName = regUsername.getText().toString();
        String updatedEmail = regEmail.getText().toString();

        if (!userId.equals("N/A")) {
            db.collection("Account").document(userId)
                    .update("Name", updatedName, "Email", updatedEmail, "Password", actualPassword)
                    .addOnSuccessListener(aVoid -> {
                        // Update SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Name", updatedName);
                        editor.putString("Email", updatedEmail);
                        editor.putString("Password", actualPassword);
                        editor.apply();

                        Toast.makeText(profilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update profile", e);
                        Toast.makeText(profilePage.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User ID not found in SharedPreferences");
            Toast.makeText(this, "Failed to update profile: User ID not found", Toast.LENGTH_SHORT).show();
        }
    }
}
