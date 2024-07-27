package sg.edu.np.mad.TicketFinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SignIn extends Fragment {
    // UI Components
    private EditText emailField; // EditText for entering email
    private EditText passwordField; // EditText for entering password
    private Button submitButton; // Button for submitting the sign-in form
    private TextView forgotpassword; // TextView for the "Forgot Password" link
    private ImageButton autoFillIcon; // ImageButton for autofill
    private TextView autoFilltext; // TextView for autofill text

    // Firebase and SharedPreferences
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private SharedPreferences sharedPreferences; // SharedPreferences for storing user data

    private OnLoginSuccessListener loginSuccessListener; // Listener for login success events
    private boolean autofillUsed = false; // To check if autofill was used

    public void setOnLoginSuccessListener(OnLoginSuccessListener listener) {
        this.loginSuccessListener = listener; // Set the login success listener
    }

    // Creating the view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false); // Inflate the layout for this fragment

        // Initialize UI components
        emailField = view.findViewById(R.id.Email); // Find the email EditText by its ID
        passwordField = view.findViewById(R.id.Password); // Find the password EditText by its ID
        submitButton = view.findViewById(R.id.Submit); // Find the submit button by its ID
        forgotpassword = view.findViewById(R.id.forgotPassword); // Find the forgot password TextView by its ID
        autoFillIcon = view.findViewById(R.id.autoFillIcon); // Find the autofill ImageButton by its ID
        autoFilltext = view.findViewById(R.id.autoFilltext); // Find the autofill TextView by its ID

        // Disable autofill initially
        emailField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO); // Disable autofill for the email field
        passwordField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO); // Disable autofill for the password field

        // Initialize Firebase and SharedPreferences
        db = FirebaseFirestore.getInstance(); // Get Firestore instance
        mAuth = FirebaseAuth.getInstance(); // Get FirebaseAuth instance
        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE); // Get SharedPreferences instance

        // OnClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate input fields are filled before attempting sign-in
                if (validateInput()) {
                    signInWithEmailAndPassword(); // Attempt to sign in with email and password
                } else {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show(); // Show a message if fields are empty
                }
            }
        });

        // OnClickListener for the "Forgot Password" text
        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display a dialog for password reset
                showForgotPasswordDialog(getContext()); // Show forgot password dialog
            }
        });

        // OnClickListener for the "AutoFill" icon
        View.OnClickListener autoFillListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptBiometricForRetrieve(); // Prompt biometric for retrieving autofill credentials
            }
        };
        autoFillIcon.setOnClickListener(autoFillListener); // Set the autofill listener for the icon
        autoFilltext.setOnClickListener(autoFillListener); // Set the autofill listener for the text

        return view; // Return the inflated view
    }

    // Validate input fields for email and password
    private boolean validateInput() {
        String username = emailField.getText().toString(); // Get the email from the EditText
        String password = passwordField.getText().toString(); // Get the password from the EditText

        return !username.isEmpty() && !password.isEmpty();  // Both fields must be filled
    }

    // Sign in with email and password using Firebase Authentication
    private void signInWithEmailAndPassword() {
        // Get entered email and password
        String email = emailField.getText().toString().trim(); // Trim the email input
        String password = passwordField.getText().toString().trim(); // Trim the password input

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in success, retrieve the current user
                            FirebaseUser user = mAuth.getCurrentUser(); // Get the current user
                            if (user != null) {
                                // Fetch user details from Firestore
                                fetchUserDetailsFromFirestore(user, email, password); // Fetch user details from Firestore
                            } else {
                                // User not found after successful authentication
                                Toast.makeText(getActivity(), "Authentication failed: User not found", Toast.LENGTH_SHORT).show(); // Show error message
                            }
                        } else {
                            // Sign-in failure
                            Log.w("SignIn", "signInWithEmail:failure", task.getException()); // Log the error
                            Toast.makeText(getActivity(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Show error message
                        }
                    }
                });
    }

    // Fetch user details from Firestore after successful sign-in
    private void fetchUserDetailsFromFirestore(FirebaseUser user, String email, String password) {
        // Match userId to userId in database
        db.collection("Account")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Save user details to SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("Document", document.getId()); // Save document ID
                                editor.putString("UserId", String.valueOf(document.get("userId"))); // Save user ID
                                editor.putString("Name", document.getString("Name")); // Save user name
                                editor.putString("Email", document.getString("Email")); // Save user email
                                editor.putString("PhoneNum", document.getString("PhoneNum")); // Save user phone number
                                editor.putString("Password", password); // Save user password
                                editor.apply(); // Apply changes

                                // Update password in Firestore
                                updatePasswordInFirestore(document.getId(), password); // Update password in Firestore

                                // Fetch user preferences after saving basic details
                                fetchUserPreferences(String.valueOf(document.get("userId"))); // Fetch user preferences

                                // Prompt user to save password for autofill with biometric if autofill was not used
                                if (!autofillUsed) {
                                    promptSavePasswordAutofill(email, password); // Prompt to save password for autofill
                                } else {
                                    if (loginSuccessListener != null) {
                                        loginSuccessListener.onLoginSuccess(); // Notify login success
                                    }
                                }

                                break; // Break after processing the first document
                            }
                        } else {
                            // Error handling
                            Log.w("SignIn", "Error getting documents.", task.getException()); // Log the error
                            Toast.makeText(getActivity(), "Failed to retrieve user details", Toast.LENGTH_SHORT).show(); // Show error message
                        }
                    }
                });
    }

    // Fetch user preferences from Firestore and save to SharedPreferences
    private void fetchUserPreferences(String userId) {
        db.collection("Preferences").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserPreferences preferences = documentSnapshot.toObject(UserPreferences.class); // Convert document to UserPreferences object
                        if (preferences != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("choice1", preferences.getChoice1()); // Save choice1
                            editor.putString("choice2", preferences.getChoice2()); // Save choice2
                            editor.putString("choice3", preferences.getChoice3()); // Save choice3
                            editor.apply(); // Apply changes

                            // Show a toast message indicating preferences are saved
                            Toast.makeText(getActivity(), "Preferences saved: " + preferences.getChoice1() + ", " + preferences.getChoice2() + ", " + preferences.getChoice3(), Toast.LENGTH_LONG).show(); // Show success message
                        }
                    } else {
                        Toast.makeText(getActivity(), "No preferences found for this user.", Toast.LENGTH_LONG).show(); // Show message if no preferences found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("SignIn", "Error getting preferences.", e); // Log the error
                    Toast.makeText(getActivity(), "Failed to retrieve user preferences", Toast.LENGTH_SHORT).show(); // Show error message
                });
    }

    // Update password in Firestore after sign-in
    private void updatePasswordInFirestore(String documentId, String newPassword) {
        db.collection("Account").document(documentId)
                .update("Password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignIn", "Password updated in Firestore"); // Log success message
                })
                .addOnFailureListener(e -> {
                    Log.e("SignIn", "Error updating password in Firestore", e); // Log error message
                });
    }

    // Display a dialog for password reset
    private void showForgotPasswordDialog(Context context) {
        Dialog forgotPasswordDialog = new Dialog(context); // Create a new dialog
        forgotPasswordDialog.setContentView(R.layout.forgot_password); // Set the dialog content view

        // Initialize views
        EditText emailEditText = forgotPasswordDialog.findViewById(R.id.editTextEmail); // Find the email EditText by its ID
        Button resetButton = forgotPasswordDialog.findViewById(R.id.resetButton); // Find the reset button by its ID
        ImageView cancelImage = forgotPasswordDialog.findViewById(R.id.cancelImage); // Find the cancel image by its ID

        // OnClickListener for the reset button in the password reset dialog
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim(); // Get the email from EditText and trim it
                // Validate email before sending password reset email
                if (!TextUtils.isEmpty(email)) {
                    sendPasswordResetEmail(email); // Send password reset email
                } else {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show(); // Show message if email is empty
                }
            }
        });

        // OnClickListener for the cancel button in the password reset dialog
        cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog.dismiss(); // Dismiss the dialog
            }
        });

        // Show the password reset dialog
        forgotPasswordDialog.show(); // Show the dialog
    }

    // Send a password reset email to the provided email address
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Log.d("SignIn", "Password reset email sent"); // Log success message
                            Toast.makeText(getActivity(), "Password reset email sent", Toast.LENGTH_SHORT).show(); // Show success message
                        } else {
                            // Failed to send password reset email
                            Log.e("SignIn", "sendPasswordResetEmail:failure", task.getException()); // Log error message
                            Toast.makeText(getActivity(), "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Show error message
                        }
                    }
                });
    }

    // Interface for login success listener
    public interface OnLoginSuccessListener {
        void onLoginSuccess(); // Callback for successful login
    }

    // Prompt for biometric authentication before retrieving credentials
    private void promptBiometricForRetrieve() {
        Executor executor = Executors.newSingleThreadExecutor(); // Create a single thread executor
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                getActivity().runOnUiThread(() -> {
                    autofillCredentials(); // Autofill credentials after successful authentication
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Handle error or cancel
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Handle failure
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential to retrieve saved credentials")
                .setDeviceCredentialAllowed(true) // Allow using device credentials (PIN, pattern, password)
                .build();

        biometricPrompt.authenticate(promptInfo); // Authenticate using biometric prompt
    }

    // Autofill credentials after biometric authentication succeeds
    private void autofillCredentials() {
        AutofillManager autofillManager = getActivity().getSystemService(AutofillManager.class); // Get AutofillManager instance
        if (autofillManager != null) {
            emailField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES); // Enable autofill for the email field
            passwordField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES); // Enable autofill for the password field
            autofillManager.requestAutofill(emailField); // Request autofill for the email field
            autofillManager.requestAutofill(passwordField); // Request autofill for the password field
            autofillUsed = true; // Set autofill used to true
        }
    }

    // Prompt the user to save the password for autofill with biometric
    private void promptSavePasswordAutofill(String email, String password) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogTheme)
                .setTitle("Save Email and Password")
                .setMessage("Do you want to save your email and password for autofill?")
                .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                    promptBiometricForSave(email, password); // Prompt biometric for saving credentials
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, which) -> {
                    if (loginSuccessListener != null) {
                        loginSuccessListener.onLoginSuccess(); // Notify login success
                    }
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.dialogButtonColor)); // Set positive button color
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.dialogButtonColor)); // Set negative button color
        });

        dialog.show(); // Show the dialog
    }

    // Prompt for biometric authentication before saving credentials
    private void promptBiometricForSave(String email, String password) {
        Executor executor = Executors.newSingleThreadExecutor(); // Create a single thread executor
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                getActivity().runOnUiThread(() -> {
                    saveCredentialsForAutofill(email, password); // Save credentials for autofill
                    if (loginSuccessListener != null) {
                        loginSuccessListener.onLoginSuccess(); // Notify login success
                    }
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Handle error
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Handle failure
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential to save email and password")
                .setDeviceCredentialAllowed(true) // Allow using device credentials (PIN, pattern, password)
                .build();

        biometricPrompt.authenticate(promptInfo); // Authenticate using biometric prompt
    }

    private void saveCredentialsForAutofill(String email, String password) {
        // Save credentials to shared preferences for later autofill
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedEmail", email); // Save email to SharedPreferences
        editor.putString("savedPassword", password); // Save password to SharedPreferences
        editor.apply(); // Apply changes
    }
}
