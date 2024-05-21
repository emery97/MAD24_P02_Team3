package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
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
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends Fragment {
    private static final String TAG = "RegisterFragment";
    private EditText Name;
    private EditText Email;
    private EditText PhoneNumber;
    private EditText Password;
    private Button Registerbutton;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        db = FirebaseFirestore.getInstance();
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

                Map<String, Object> user = new HashMap<>();
                user.put("Name", username);
                user.put("Email", email);
                user.put("PhoneNum", phone);
                user.put("Password", password);

                Log.d(TAG, "Attempting to add user to Firestore");

                db.collection("Account")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "User added successfully: " + documentReference.getId());
                                Toast.makeText(getActivity(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                                Name.setText("");
                                Email.setText("");
                                PhoneNumber.setText("");
                                Password.setText("");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error adding user", e);
                                Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        return view;
    }
}