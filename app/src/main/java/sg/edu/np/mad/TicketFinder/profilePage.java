package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class profilePage extends AppCompatActivity {
    // attributes
    private TextView username, password, email,regUsername, regPassword, regEmail;
    private ImageView editingIcon, profilePicture;
    private CheckBox showPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_page);

        // initialize UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        regUsername = findViewById(R.id.regUsername);
        regPassword = findViewById(R.id.regPassword);
        regEmail = findViewById(R.id.regEmail);
        editingIcon = findViewById(R.id.editingIcon);
        profilePicture = findViewById(R.id.profilePicture);
        showPassword = findViewById(R.id.showPassword);

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowEditing();
                togglePasswordVisibility();
            }

        });

    }

    // Toggle password visibility based on checkbox
    private boolean togglePasswordVisibility() {
        if (showPassword.isChecked()) {
            // Show password
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            // Hide password
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        regPassword.getSelectionEnd(); // Ensure cursor is at the end of the text
        return showPassword.isChecked();
    }


    // method to edit
    private void AllowEditing(){
        regEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        regUsername.setInputType(InputType.TYPE_CLASS_TEXT);
        if (togglePasswordVisibility()){
            regPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        else{
            regPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }




}
