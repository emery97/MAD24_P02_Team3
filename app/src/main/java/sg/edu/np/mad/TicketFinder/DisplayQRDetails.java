package sg.edu.np.mad.TicketFinder;

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

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayQRDetails extends AppCompatActivity {

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
            String eventTitle = qrData.getString("event_title");
            String seatCategory = qrData.getString("seat_category");
            String seatNumber = qrData.getString("seat_number");
            String totalPrice = qrData.getString("total_price");
            String quantity = qrData.getString("quantity");
            String paymentMethod = qrData.getString("payment_method");
            String time = qrData.getString("event_date");
            String purchaseTime = qrData.getString("date_bought");
            String userid = qrData.getString("user_id");
            String name = qrData.getString("name");
            String email = qrData.getString("email");
            String phone = qrData.getString("phoneNum");
            String expiry = qrData.getString("expiryTimeMillis");

            // Find the TextView elements by their ID
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


            // Set the text for each TextView
            eventTitleTextView.setText(eventTitle);
            seatCategoryTextView.setText(seatCategory);
            seatNumberTextView.setText(seatNumber);
            totalPriceTextView.setText(totalPrice);
            quantityTextView.setText(quantity);
            paymentMethodTextView.setText(paymentMethod);
            timeTextView.setText(time);
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
}