package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    // Creating the view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Initialize UI components
        emailField = view.findViewById(R.id.Email);
        passwordField = view.findViewById(R.id.Password);
        submitButton = view.findViewById(R.id.Submit);
        forgotpassword = view.findViewById(R.id.forgotPassword);

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
                                fetchUserDetailsFromFirestore(user, password);
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
    private void fetchUserDetailsFromFirestore(FirebaseUser user, String newPassword) {
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
                                editor.putString("UserId", String.valueOf(document.get("userId")));
                                editor.putString("Name", document.getString("Name"));
                                editor.putString("Email", document.getString("Email"));
                                editor.putString("PhoneNum", document.getString("PhoneNum"));
                                editor.putString("Password", newPassword);
                                editor.apply();

                                // Update password in Firestore
                                updatePasswordInFirestore(document.getId(), newPassword);

                                navigateToHomepage();
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

    // Navigate to Homepage after successful sign-in
    private void navigateToHomepage() {
        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), homepage.class);
        startActivity(intent);
        getActivity().finish();
    }

    // Display a dialog for password reset
    private void showForgotPasswordDialog(Context context) {
        Dialog forgotPasswordDialog = new Dialog(context);
        forgotPasswordDialog.setContentView(R.layout.forgot_password);

        // Initalize views
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
}
