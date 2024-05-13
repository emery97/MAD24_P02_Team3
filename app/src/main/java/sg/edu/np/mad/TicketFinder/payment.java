package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class payment extends AppCompatActivity {
    private EditText editCardNumber, editExpiry, editCVV, editName, editAddress, editPostalCode;
    private Button buyNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.payment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        editCardNumber = findViewById(R.id.editCardNumber);
        editExpiry = findViewById(R.id.editExpiry);
        editCVV = findViewById(R.id.editTextText3);
        editName = findViewById(R.id.editTextText4);
        editAddress = findViewById(R.id.editAddress);
        editPostalCode = findViewById(R.id.editPostalCode);
        buyNow = findViewById(R.id.buyNow);

        // Set OnClickListener for buyNow button
        buyNow.setOnClickListener(view -> {
            // Validate input fields
            if (validateInput()) {
                processPayment();
            } else {
                // Display error message if validation fails
                Toast.makeText(payment.this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });



    }
    // Method to validate input fields
    private boolean validateInput() {
        String cardNumber = editCardNumber.getText().toString();
        String expiry = editExpiry.getText().toString();
        String cvv = editCVV.getText().toString();
        String name = editName.getText().toString();
        String address = editAddress.getText().toString();
        String postalCode = editPostalCode.getText().toString();

        // Check if all fields are filled
        return !cardNumber.isEmpty() && !expiry.isEmpty() && !cvv.isEmpty()
                && !name.isEmpty() && !address.isEmpty() && !postalCode.isEmpty();
    }

    // Method to process payment
    private void processPayment() {
        Toast.makeText(payment.this, "Payment successful", Toast.LENGTH_SHORT).show();
    }
}