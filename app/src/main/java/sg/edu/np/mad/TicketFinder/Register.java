package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Register extends Fragment {
    private static final String TAG = "RegisterFragment";
    private EditText Name;
    private EditText Email;
    private EditText PhoneNumber;
    private EditText Password;
    private Button Registerbutton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public interface OnRegistrationSuccessListener {
        void onRegistrationSuccess();
    }

    private OnRegistrationSuccessListener registrationSuccessListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegistrationSuccessListener) {
            registrationSuccessListener = (OnRegistrationSuccessListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegistrationSuccessListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        registrationSuccessListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Name = view.findViewById(R.id.RegisterName);
        Email = view.findViewById(R.id.RegisterEmail);
        PhoneNumber = view.findViewById(R.id.RegisterPhone);
        Password = view.findViewById(R.id.Registerpassword);
        Registerbutton = view.findViewById(R.id.Registerbtn);

        Registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = Name.getText().toString().trim();
                String email = Email.getText().toString().trim();
                String phone = PhoneNumber.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Incomplete form submission");
                    return;
                }
                else if (!phone.matches("\\d{8}")) {
                    Toast.makeText(getActivity(), "Please enter an 8-digit, Singaporean phone number", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Invalid phone number");
                    return;
                }

                createUserWithEmail(username, email, phone, password);
            }
        });

        return view;
    }

    private void createUserWithEmail(String username, String email, String phone, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User registered with Firebase Auth");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                getMaxUserIdAndRegister(username, email, phone, password, user.getUid());
                            } else {
                                Log.e(TAG, "User is null after registration");
                                Toast.makeText(getActivity(), "Registration failed: User is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Failed to register user with Firebase Auth", task.getException());
                            Toast.makeText(getActivity(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getMaxUserIdAndRegister(String username, String email, String phone, String password, String uid) {
        db.collection("Account")
                .orderBy("userId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int maxUserId = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Long userId = document.getLong("userId");
                                if (userId != null) {
                                    maxUserId = userId.intValue();
                                }
                                break;
                            }
                            registerUser(username, email, phone, password, maxUserId + 1, uid);
                        } else {
                            // If no user exists, start with userId 1
                            Log.d(TAG, "No users found, starting with userId 1");
                            registerUser(username, email, phone, password, 1, uid);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to get max userId", e);
                        Toast.makeText(getActivity(), "Failed to get user ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String username, String email, String phone, String password, int userId, String uid) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("Name", username);
        user.put("Email", email);
        user.put("PhoneNum", phone);
        user.put("Password", password);  // It is not recommended to store passwords in Firestore. Consider storing only hashed passwords or omitting this field.
        user.put("uid", uid);

        Log.d(TAG, "Attempting to add user to Firestore");

        db.collection("Account")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "User added successfully: " + documentReference.getId());
                        Toast.makeText(getActivity(), "Registered Successfully!", Toast.LENGTH_SHORT).show();
                        Name.setText("");
                        Email.setText("");
                        PhoneNumber.setText("");
                        Password.setText("");

                        if (registrationSuccessListener != null) {
                            registrationSuccessListener.onRegistrationSuccess();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding user", e);
                        Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
