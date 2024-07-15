    package sg.edu.np.mad.TicketFinder;

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
        // TAG for logging
        private static final String TAG = "RegisterFragment";

        // UI components
        private EditText Name;
        private EditText Email;
        private EditText PhoneNumber;
        private EditText Password;
        private Button Registerbutton;

        // Firebase
        private FirebaseFirestore db;
        private FirebaseAuth mAuth;

        // Listener for registration success event
        public interface OnRegistrationSuccessListener {
            void onRegistrationSuccess();
        }

        // Instance of the listener
        private OnRegistrationSuccessListener registrationSuccessListener;

        // Method to set the listener
        public void setOnRegistrationSuccessListener(OnRegistrationSuccessListener listener) {
            this.registrationSuccessListener = listener;
        }

        // Creating the view
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_register, container, false);

            // Initializing Firebase
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Initializing UI components
            Name = view.findViewById(R.id.RegisterName);
            Email = view.findViewById(R.id.RegisterEmail);
            PhoneNumber = view.findViewById(R.id.RegisterPhone);
            Password = view.findViewById(R.id.Registerpassword);
            Registerbutton = view.findViewById(R.id.Registerbtn);

            // OnClickListener for register button
            Registerbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get user inputs
                    String username = Name.getText().toString().trim();
                    String email = Email.getText().toString().trim();
                    String phone = PhoneNumber.getText().toString().trim();
                    String password = Password.getText().toString().trim();

                    // Validation
                    // All fields must be filled
                    if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                        Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Incomplete form submission");
                        return;
                    } else if (!phone.matches("\\d{8}")) { // Phone number must be 8 digits
                        Toast.makeText(getActivity(), "Please enter an 8-digit, Singaporean phone number", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Invalid phone number");
                        return;
                    }

                    // Create user
                    createUserWithEmail(username, email, phone, password);
                }
            });

            return view;
        }

        // Method to create user
        // Reference from chatgpt and Firebase code
        private void createUserWithEmail(String username, String email, String phone, String password) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Registration successful
                                Log.d(TAG, "User registered with Firebase Auth");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Register user
                                    getLastUserIdAndRegister(username, email, phone, password, user.getUid());
                                } else {
                                    Log.e(TAG, "User is null after registration");
                                    Toast.makeText(getActivity(), "Registration failed: User is null", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Registration failed
                                Log.e(TAG, "Failed to register user with Firebase Auth", task.getException());
                                Toast.makeText(getActivity(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        // Method to get last user ID and register user
        private void getLastUserIdAndRegister(String username, String email, String phone, String password, String uid) {
            // Get all users from database and ordering by userId
            db.collection("Account")
                    .orderBy("userId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                // Get last user ID
                                int lastUserId = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Long userId = document.getLong("userId");
                                    if (userId != null) {
                                        lastUserId = userId.intValue();
                                    }
                                    break;
                                }
                                // Register user as lastUserId + 1
                                registerUser(username, email, phone, password, lastUserId + 1, uid);
                            } else {
                                // If no user exists, start with userId 1
                                Log.d(TAG, "No users found, starting with userId 1");
                                registerUser(username, email, phone, password, 1, uid);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() { // Error handling
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to get max userId", e);
                            Toast.makeText(getActivity(), "Failed to get user ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Method to register user
        private void registerUser(String username, String email, String phone, String password, int userId, String uid) {
            // Create user data
            Map<String, Object> user = new HashMap<>();
            user.put("userId", userId);
            user.put("Name", username);
            user.put("Email", email);
            user.put("PhoneNum", phone);
            user.put("Password", password);
            user.put("uid", uid);

            Log.d(TAG, "Attempting to add user to Firestore");

            // Add user data to Firestore
            db.collection("Account")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            // User added successfully
                            Log.d(TAG, "User added successfully: " + documentReference.getId());
                            Toast.makeText(getActivity(), "Registered Successfully!", Toast.LENGTH_SHORT).show();
                            // Clear input fields
                            Name.setText("");
                            Email.setText("");
                            PhoneNumber.setText("");
                            Password.setText("");

                            // Notify registration success
                            if (registrationSuccessListener != null) {
                                registrationSuccessListener.onRegistrationSuccess();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() { // Error handling
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to register user
                            Log.e(TAG, "Error adding user", e);
                            Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
