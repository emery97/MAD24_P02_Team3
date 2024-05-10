package sg.edu.np.mad.ticketFinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SignIn extends Fragment {
    private EditText usernameField;
    private EditText passwordField;
    private Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);  // Correct layout for this fragment

        usernameField = view.findViewById(R.id.Username);
        passwordField = view.findViewById(R.id.Password);
        submitButton = view.findViewById(R.id.Submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private boolean validateInput() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        return !username.isEmpty() && !password.isEmpty();  // Both fields must be filled
    }
}