package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class Register extends Fragment {
    private EditText Name;
    private EditText Email;
    private EditText PhoneNumber;
    private EditText Password;
    private Button Registerbutton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);  // Correct layout for this fragment

        Name = view.findViewById(R.id.RegisterName);
        Email = view.findViewById(R.id.RegisterEmail);
        PhoneNumber = view.findViewById(R.id.RegisterPhone);
        Password = view.findViewById(R.id.Registerpassword);
        Registerbutton = view.findViewById(R.id.Registerbtn);

        Registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    Toast.makeText(getActivity(), "Registered successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private boolean validateInput() {
        String username = Name.getText().toString();
        String email = Email.getText().toString();
        String phone = PhoneNumber.getText().toString();
        String password = Password.getText().toString();

        return !username.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !password.isEmpty();  // Both fields must be filled
    }
}
