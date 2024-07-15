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
    private EditText emailField;
    private EditText passwordField;
    private Button submitButton;
    private TextView forgotpassword;

    // Firebase and SharedPreferences
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    private OnLoginSuccessListener loginSuccessListener;
    private boolean autofillUsed = false; // To check if autofill was used

    public void setOnLoginSuccessListener(OnLoginSuccessListener listener) {
        this.loginSuccessListener = listener;
    }

    // Creating the view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Initialize UI components
        emailField = view.findViewById(R.id.Email);
        passwordField = view.findViewById(R.id.Password);
        submitButton = view.findViewById(R.id.Submit);
        forgotpassword = view.findViewById(R.id.forgotPassword);

        // Disable autofill initially
        emailField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        passwordField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);

        // Initialize Firebase and SharedPreferences
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // OnClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate input fields are filled before attempting sign-in
                if (validateInput()) {
                    signInWithEmailAndPassword();
                } else {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // OnClickListener for the "Forgot Password" text
        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display a dialog for password reset
                showForgotPasswordDialog(getContext());
            }
        });

        // Prompt the user if they want to use autofill
        promptRetrieveCredentials();

        return view;
    }

    // Validate input fields for email and password
    private boolean validateInput() {
        String username = emailField.getText().toString();
        String password = passwordField.getText().toString();

        return !username.isEmpty() && !password.isEmpty();  // Both fields must be filled
    }

    // Sign in with email and password using Firebase Authentication
    private void signInWithEmailAndPassword() {
        // Get entered email and password
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in success, retrieve the current user
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Fetch user details from Firestore
                                fetchUserDetailsFromFirestore(user, email, password);
                            } else {
                                // User not found after successful authentication
                                Toast.makeText(getActivity(), "Authentication failed: User not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Sign-in failure
                            Log.w("SignIn", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                editor.putString("Document", document.getId());
                                editor.putString("UserId", String.valueOf(document.get("userId"))); // Ensure key is "userId"
                                editor.putString("Name", document.getString("Name"));
                                editor.putString("Email", document.getString("Email"));
                                editor.putString("PhoneNum", document.getString("PhoneNum"));
                                editor.putString("Password", password);
                                editor.apply();

                                // Update password in Firestore
                                updatePasswordInFirestore(document.getId(), password);

                                // Fetch user preferences after saving basic details
                                fetchUserPreferences(String.valueOf(document.get("userId")));

                                // Prompt user to save password for autofill with biometric if autofill was not used
                                if (!autofillUsed) {
                                    promptSavePasswordAutofill(email, password);
                                } else {
                                    if (loginSuccessListener != null) {
                                        loginSuccessListener.onLoginSuccess();
                                    }
                                }

                                break;
                            }
                        } else {
                            // Error handling
                            Log.w("SignIn", "Error getting documents.", task.getException());
                            Toast.makeText(getActivity(), "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Fetch user preferences from Firestore and save to SharedPreferences
    private void fetchUserPreferences(String userId) {
        db.collection("Preferences").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserPreferences preferences = documentSnapshot.toObject(UserPreferences.class);
                        if (preferences != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("choice1", preferences.getChoice1());
                            editor.putString("choice2", preferences.getChoice2());
                            editor.putString("choice3", preferences.getChoice3());
                            editor.apply();

                            // Show a toast message indicating preferences are saved
                            Toast.makeText(getActivity(), "Preferences saved: " + preferences.getChoice1() + ", " + preferences.getChoice2() + ", " + preferences.getChoice3(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "No preferences found for this user.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("SignIn", "Error getting preferences.", e);
                    Toast.makeText(getActivity(), "Failed to retrieve user preferences", Toast.LENGTH_SHORT).show();
                });
    }

    // Update password in Firestore after sign-in
    private void updatePasswordInFirestore(String documentId, String newPassword) {
        db.collection("Account").document(documentId)
                .update("Password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignIn", "Password updated in Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("SignIn", "Error updating password in Firestore", e);
                });
    }

    // Display a dialog for password reset
    private void showForgotPasswordDialog(Context context) {
        Dialog forgotPasswordDialog = new Dialog(context);
        forgotPasswordDialog.setContentView(R.layout.forgot_password);

        // Initialize views
        EditText emailEditText = forgotPasswordDialog.findViewById(R.id.editTextEmail);
        Button resetButton = forgotPasswordDialog.findViewById(R.id.resetButton);
        ImageView cancelImage = forgotPasswordDialog.findViewById(R.id.cancelImage);

        // OnClickListener for the reset button in the password reset dialog
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                // Validate email before sending password reset email
                if (!TextUtils.isEmpty(email)) {
                    sendPasswordResetEmail(email);
                } else {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // OnClickListener for the cancel button in the password reset dialog
        cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog.dismiss();
            }
        });

        // Show the password reset dialog
        forgotPasswordDialog.show();
    }

    // Send a password reset email to the provided email address
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Log.d("SignIn", "Password reset email sent");
                            Toast.makeText(getActivity(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to send password reset email
                            Log.e("SignIn", "sendPasswordResetEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Interface for login success listener
    public interface OnLoginSuccessListener {
        void onLoginSuccess();
    }

    // Prompt the user if they want to retrieve saved credentials
    private void promptRetrieveCredentials() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Retrieve Saved Credentials")
                .setMessage("Do you want to retrieve your saved email and password?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    promptBiometricForRetrieve();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // User chose not to retrieve saved credentials
                    emailField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                    passwordField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                })
                .show();
    }

    // Prompt for biometric authentication before retrieving credentials
    private void promptBiometricForRetrieve() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                getActivity().runOnUiThread(() -> {
                    autofillCredentials();
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

        biometricPrompt.authenticate(promptInfo);
    }

    // Autofill credentials after biometric authentication succeeds
    private void autofillCredentials() {
        AutofillManager autofillManager = getActivity().getSystemService(AutofillManager.class);
        if (autofillManager != null) {
            emailField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            passwordField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            autofillManager.requestAutofill(emailField);
            autofillManager.requestAutofill(passwordField);
            autofillUsed = true; // Set autofill used to true
        }
    }

    // Prompt the user to save the password for autofill with biometric
    private void promptSavePasswordAutofill(String email, String password) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Save Email and Password")
                .setMessage("Do you want to save your email and password for autofill?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    promptBiometricForSave(email, password);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    if (loginSuccessListener != null) {
                        loginSuccessListener.onLoginSuccess();
                    }
                })
                .show();
    }

    // Prompt for biometric authentication before saving credentials
    private void promptBiometricForSave(String email, String password) {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                getActivity().runOnUiThread(() -> {
                    saveCredentialsForAutofill(email, password);
                    if (loginSuccessListener != null) {
                        loginSuccessListener.onLoginSuccess();
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

        biometricPrompt.authenticate(promptInfo);
    }

    private void saveCredentialsForAutofill(String email, String password) {
        // Save credentials to shared preferences for later autofill
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedEmail", email);
        editor.putString("savedPassword", password);
        editor.apply();
    }
}
