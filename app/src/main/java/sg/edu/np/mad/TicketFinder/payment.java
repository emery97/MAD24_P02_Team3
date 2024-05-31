package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class payment extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private Spinner paymentmethod;
    private EditText editCardNumber, editExpiry, editCVV, editName, editAddress, editPostalCode;
    private Button buyNow;
    private Button bookingdetails;
    private TextView totalPricetext;
    private Button cancel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Check orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape mode
            setContentView(R.layout.horizontal_payment);
            // Set the dialog to match parent size
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            // Portrait mode
            setContentView(R.layout.activity_payment);
        }

        //setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.testpaymentbtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserId", null);
        String name = sharedPreferences.getString("Name", null);

        // Initialize UI components
        editCardNumber = findViewById(R.id.editCardNumber);
        editExpiry = findViewById(R.id.editExpiry);
        editCVV = findViewById(R.id.editTextText3);
        editName = findViewById(R.id.editTextText4);
        editAddress = findViewById(R.id.editAddress);
        editPostalCode = findViewById(R.id.editPostalCode);
        buyNow = findViewById(R.id.buyNow);
        totalPricetext = findViewById(R.id.totalpricedisplay);
        bookingdetails = findViewById(R.id.bookingdetails);
        cancel = findViewById(R.id.backbtn);

        paymentmethod = findViewById(R.id.paymentMethodSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentmethod.setAdapter(adapter);

        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        bookingdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBookingDetailsDialog();
            }
        });

        totalPricetext.setText("Total Price: $" + totalPrice);

        buyNow.setOnClickListener(view -> {
            // Validate input fields
            if (validateInput()) {
                processPayment();
            } else {
                //Error Message
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(payment.this, BuyTicket.class);
                startActivity(intent);
            }
        });
    }

    // Method to validate input fields
    private boolean validateInput() {
        String cardNumber = editCardNumber.getText().toString();
        if (cardNumber.length() != 16) {
            Toast.makeText(payment.this, "Invalid Card Number", Toast.LENGTH_SHORT).show();
            return false;
        }
        String expiry = editExpiry.getText().toString();
        if (expiry.length() != 5){
            Toast.makeText(payment.this, "Input valid date", Toast.LENGTH_SHORT).show();
            return false;
        }

        String cvv = editCVV.getText().toString();
        if (cvv.length() != 3){
            Toast.makeText(payment.this, "Invalid Cvv Input", Toast.LENGTH_SHORT).show();
            return false;
        }

        String name = editName.getText().toString();
        String address = editAddress.getText().toString();
        String postalCode = editPostalCode.getText().toString();


        if (name.isEmpty() || address.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(payment.this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void processPayment() {
        Toast.makeText(payment.this, "Payment successful", Toast.LENGTH_SHORT).show();
        postBookingDetailsToFirestore();
        new Handler().postDelayed(this::showConfirmationDialog, 1000); // Wait for 1 second before alert message
    }

    private void postBookingDetailsToFirestore() {
        String userId = sharedPreferences.getString("UserId", null);
        String name = sharedPreferences.getString("Name", null);
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        String seatCategory = getIntent().getStringExtra("seatCategory");
        String seatNumber = getIntent().getStringExtra("seatNumber");
        int quantity = getIntent().getIntExtra("quantity", 1);
        String paymentMethod = paymentmethod.getSelectedItem().toString();

        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("userId", userId);
        bookingDetails.put("Name", name);
        bookingDetails.put("SeatCategory", seatCategory);
        bookingDetails.put("SeatNumber", seatNumber);
        bookingDetails.put("TotalPrice", totalPrice);
        bookingDetails.put("Quantity", quantity);
        bookingDetails.put("PaymentMethod", paymentMethod);

        db.collection("BookingDetails").add(bookingDetails)
                .addOnSuccessListener(documentReference -> {
                    editCardNumber.setText("");
                    editAddress.setText("");
                    editCVV.setText("");
                    editExpiry.setText("");
                    editName.setText("");
                    editPostalCode.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(payment.this, "Error saving booking details", Toast.LENGTH_SHORT).show();
                });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Contribution Matter!");
        builder.setMessage("Provide your feedback on what to improve on our app!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        navigateToFeedback();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        navigateToHomepage();
                    }
                });
        builder.create().show();
    }

    private void navigateToHomepage() {
        Toast.makeText(payment.this, "Going back to homepage", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(payment.this, homepage.class);
        startActivity(intent);
        finish();
    }

    private void navigateToFeedback(){
        Intent intent = new Intent(payment.this, Feedback.class);
        startActivity(intent);
        finish();
    }

    private void showBookingDetailsDialog() {
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        String seatCategory = getIntent().getStringExtra("seatCategory");
        String seatNumber = getIntent().getStringExtra("seatNumber");
        int quantity = getIntent().getIntExtra("quantity", 1);

        Dialog dialog = new Dialog(payment.this);


        dialog.setContentView(R.layout.bookingdetails);

        TextView categoryText = dialog.findViewById(R.id.categoryText);
        TextView numberText = dialog.findViewById(R.id.numberText);
        TextView priceText = dialog.findViewById(R.id.priceText);
        TextView quantityText = dialog.findViewById(R.id.quantityText);

        categoryText.setText(seatCategory);
        numberText.setText(seatNumber);
        priceText.setText("Price: $" + totalPrice);
        quantityText.setText("Quantity: " + quantity);

        dialog.show();
    }
}