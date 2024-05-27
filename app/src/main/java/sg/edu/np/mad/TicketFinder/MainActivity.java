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

public class MainActivity extends AppCompatActivity implements Register.OnRegistrationSuccessListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sharedPreferences.contains("UserId")) {
            navigateToHomepage();
        } else {
            loadSignInFragment();
        }

        TextView signup = findViewById(R.id.signin);
        TextView register = findViewById(R.id.register);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSignInFragment();
            }
        });

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

    private void loadSignInFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fraglayout, new SignIn());
        transaction.commit();
    }

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
