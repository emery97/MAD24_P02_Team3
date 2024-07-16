package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DisplayQRDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventTitle;
    private String eventDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.displayqrdetails);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button viewMoreButton = findViewById(R.id.viewMoreButton);
        LinearLayout additionalDetailsLayout = findViewById(R.id.additionalDetailsLayout);
        db = FirebaseFirestore.getInstance();

        viewMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (additionalDetailsLayout.getVisibility() == View.GONE) {
                    additionalDetailsLayout.setVisibility(View.VISIBLE);
                    viewMoreButton.setText("View Less");
                } else {
                    additionalDetailsLayout.setVisibility(View.GONE);
                    viewMoreButton.setText("View More");
                }
            }
        });

        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        TextView qrcode = findViewById(R.id.qrdata);
        qrcode.setText(qrCodeData);

        try {
            // Parse the JSON string
            JSONObject qrData = new JSONObject(qrCodeData);
            eventTitle = qrData.getString("event_title");
            eventDate = qrData.getString("event_date");

            // Check if event is valid
            checkEventValidity(eventTitle);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_LONG).show();
            finish(); // Close activity if QR data is invalid
        }
    }

    private void checkEventValidity(String eventTitle) {
        // Reference to the events collection
        Query query = db.collection("Events")
                .whereEqualTo("Name", eventTitle);

        // Execute the query asynchronously
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean isValidEvent = false;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String firestoreName = document.getString("Name");

                    // Compare the retrieved Firestore event name with the provided event title
                    if (firestoreName != null && firestoreName.equals(eventTitle)) {
                        isValidEvent = true;
                        break;
                    }
                }
                if (isValidEvent) {
                    Toast.makeText(DisplayQRDetails.this, "Valid event", Toast.LENGTH_SHORT).show();
                    // Continue displaying event details
                    displayEventDetails();
                } else {
                    Toast.makeText(DisplayQRDetails.this, "Not a valid event", Toast.LENGTH_SHORT).show();
                    // Navigate back to BookingHistoryDetails or any other appropriate action
                    navigateBack();
                }
            } else {
                // Handle errors
                Toast.makeText(DisplayQRDetails.this, "Error checking event validity", Toast.LENGTH_SHORT).show();
                navigateBack(); // Navigate back on error
            }
        });
    }


    private void displayEventDetails() {
        // Continue displaying event details here
        try {
            JSONObject qrData = new JSONObject(getIntent().getStringExtra("qrCodeData"));
            eventTitle = qrData.getString("event_title");
            String seatCategory = qrData.getString("seat_category");
            String seatNumber = qrData.getString("seat_number");
            String totalPrice = qrData.getString("total_price");
            String quantity = qrData.getString("quantity");
            String paymentMethod = qrData.getString("payment_method");
            eventDate = qrData.getString("event_date");
            String purchaseTime = qrData.getString("date_bought");
            String userid = qrData.getString("user_id");
            String name = qrData.getString("name");
            String email = qrData.getString("email");
            String phone = qrData.getString("phoneNum");
            String expiry = qrData.getString("expiryTimeMillis");

            TextView eventTitleTextView = findViewById(R.id.eventTitleTextView);
            TextView seatCategoryTextView = findViewById(R.id.seatCategoryTextView);
            TextView seatNumberTextView = findViewById(R.id.seatNumberTextView);
            TextView totalPriceTextView = findViewById(R.id.totalPriceTextView);
            TextView quantityTextView = findViewById(R.id.quantityTextView);
            TextView paymentMethodTextView = findViewById(R.id.paymentMethodTextView);
            TextView timeTextView = findViewById(R.id.timeTextView);
            TextView purchaseTimeTextView = findViewById(R.id.purchaseTimeTextView);
            TextView nameTextView = findViewById(R.id.accountnameTextView);
            TextView useridTextView = findViewById(R.id.useridTextView);
            TextView EmailTextView = findViewById(R.id.EmailTextView);
            TextView PhoneTextView = findViewById(R.id.PhoneTextView);
            TextView expiryTextView = findViewById(R.id.expiryTextView);

            eventTitleTextView.setText(eventTitle);
            seatCategoryTextView.setText(seatCategory);
            seatNumberTextView.setText(seatNumber);
            totalPriceTextView.setText(totalPrice);
            quantityTextView.setText(quantity);
            paymentMethodTextView.setText(paymentMethod);
            timeTextView.setText(eventDate);
            purchaseTimeTextView.setText(purchaseTime);
            nameTextView.setText(name);
            useridTextView.setText(userid);
            EmailTextView.setText(email);
            PhoneTextView.setText(phone);
            expiryTextView.setText(expiry);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateBack() {
        // Navigate back to BookingHistoryDetails or any other appropriate action
        Intent intent = new Intent(DisplayQRDetails.this, BookingHistoryDetails.class);
        startActivity(intent);
        finish(); // Finish current activity to prevent going back to it
    }
}
