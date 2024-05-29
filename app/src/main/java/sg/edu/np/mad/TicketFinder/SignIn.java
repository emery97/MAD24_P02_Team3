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
import android.widget.CheckBox;
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
    private EditText emailField;
    private EditText passwordField;
    private Button submitButton;
    private CheckBox Remember;
    private TextView forgotpassword;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);  // Correct layout for this fragment

        emailField = view.findViewById(R.id.Email);
        passwordField = view.findViewById(R.id.Password);
        submitButton = view.findViewById(R.id.Submit);
        Remember = view.findViewById(R.id.RememberMe);
        forgotpassword = view.findViewById(R.id.forgotPassword);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Check if "Remember Me" was previously selected
        if (sharedPreferences.getBoolean("RememberMe", false)) {
            navigateToHomepage();
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    signInWithEmailAndPassword();
                } else {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog(getContext());
            }
        });

        return view;
    }

    private boolean validateInput() {
        String username = emailField.getText().toString();
        String password = passwordField.getText().toString();

        return !username.isEmpty() && !password.isEmpty();  // Both fields must be filled
    }

    private void signInWithEmailAndPassword() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("SignIn", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                fetchUserDetailsFromFirestore(user, password);
                            } else {
                                Toast.makeText(getActivity(), "Authentication failed: User not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("SignIn", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchUserDetailsFromFirestore(FirebaseUser user, String newPassword) {
        db.collection("Account")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Fetch user details
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("Document", document.getId());
                                editor.putString("UserId", String.valueOf(document.get("userId")));
                                editor.putString("Name", document.getString("Name"));
                                editor.putString("Email", document.getString("Email"));
                                editor.putString("PhoneNum", document.getString("PhoneNum"));
                                editor.putString("Password", newPassword);
                                editor.putBoolean("RememberMe", Remember.isChecked());
                                editor.apply();

                                // Update password in Firestore
                                updatePasswordInFirestore(document.getId(), newPassword);

                                // Navigate to Homepage
                                navigateToHomepage();
                                break;
                            }
                        } else {
                            Log.w("SignIn", "Error getting documents.", task.getException());
                            Toast.makeText(getActivity(), "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

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

    private void navigateToHomepage() {
        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), homepage.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void showForgotPasswordDialog(Context context) {
        Dialog forgotPasswordDialog = new Dialog(context);
        forgotPasswordDialog.setContentView(R.layout.forgot_password);

        EditText emailEditText = forgotPasswordDialog.findViewById(R.id.editTextEmail);
        Button resetButton = forgotPasswordDialog.findViewById(R.id.resetButton);
        ImageView cancelImage = forgotPasswordDialog.findViewById(R.id.cancelImage);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                // Validate email
                if (!TextUtils.isEmpty(email)) {
                    sendPasswordResetEmail(email);
                } else {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog.dismiss();
            }
        });

        forgotPasswordDialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("SignIn", "Password reset email sent");
                            Toast.makeText(getActivity(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("SignIn", "sendPasswordResetEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
