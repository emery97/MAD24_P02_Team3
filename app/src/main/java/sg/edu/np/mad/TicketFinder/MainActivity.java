package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity implements Register.OnRegistrationSuccessListener {
    //login/register page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // remember login user
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sharedPreferences.contains("UserId")) {
            navigateToHomepage(); // if user has previously loggged in, go straight to homepage
        } else {
            loadSignInFragment(); // else, show sign in page
        }

        // get views for sign up/register buttons
        TextView signup = findViewById(R.id.signin);
        TextView register = findViewById(R.id.register);

        // display sign up
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSignInFragment();
            }
        });

        // display log in
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegisterFragment();
            }
        });
    }

    private void navigateToHomepage() {
        Intent intent = new Intent(MainActivity.this, homepage.class);
        startActivity(intent);
        finish();  // Finish MainActivity so the user can't navigate back to it
    }

    // setting sign in fragment
    private void loadSignInFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fraglayout, new SignIn());
        transaction.commit();
    }

    //setting login fragment
    private void loadRegisterFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fraglayout, new Register());
        transaction.commit();
    }

    @Override
    public void onRegistrationSuccess() {
        loadSignInFragment();
    }
}
