package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SignIn extends Fragment {
    private EditText usernameField;
    private EditText passwordField;
    private Button submitButton;
    private CheckBox Remember;
    private TextView forgotpassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);  // Correct layout for this fragment

        usernameField = view.findViewById(R.id.Username);
        passwordField = view.findViewById(R.id.Password);
        submitButton = view.findViewById(R.id.Submit);
        Remember = view.findViewById(R.id.RememberMe);
        forgotpassword = view.findViewById(R.id.forgotPassword);

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

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog(getContext());
            }
        });

        return view;

    }

    private boolean validateInput() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        return !username.isEmpty() && !password.isEmpty();  // Both fields must be filled
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
                    // Implement logic to send reset password email or SMS
                    // You can call a method here to handle the password reset process
                    // For example: sendPasswordResetEmail(email);
                } else {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
                forgotPasswordDialog.dismiss();
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
}