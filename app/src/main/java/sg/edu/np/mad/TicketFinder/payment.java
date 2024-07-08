package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class payment extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // SharedPreferences for storing user data
    private Spinner paymentmethod;

    private EditText editCardNumber, editExpiry, editCVV, editName, editAddress, editPostalCode; // EditText fields for payment details
    private Button buyNow;
    private Button bookingdetails;
    private TextView totalPricetext;
    private Button cancel;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // List applications that can handle calendar intents
        listCalendarIntentHandlers();

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Retrieve Event object from intent
        Intent eventIntent = getIntent();
        Event eventObj = (Event) eventIntent.getSerializableExtra("event");

        if (eventObj == null) {
            Log.e("EVENT_NULL", "Event object is null");
            Toast.makeText(this, "Event details are missing", Toast.LENGTH_SHORT).show();
            return; // Early return to avoid further processing
        }

        // Check orientation and set layout accordingly
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape mode
            setContentView(R.layout.horizontal_payment);
            // Set the dialog to match parent size
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            // Portrait mode
            setContentView(R.layout.activity_payment);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.testpaymentbtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Get user data from SharedPreferences
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

        // Initialize payment method spinner (Referred to developer.android.com)
        paymentmethod = findViewById(R.id.paymentMethodSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentmethod.setAdapter(adapter);

        // Get total price from intent
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        // Set total price text
        totalPricetext.setText("$" + totalPrice);

        // Handle click on booking details button
        bookingdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBookingDetailsDialog();
            }
        });

        // Handle click on buy now button
        buyNow.setOnClickListener(view -> {
            // Validate input fields
            if (validateInput()) {
                processPayment();
            } else {
                // Display error message
            }
        });

        // Handle click on cancel button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to BuyTicket activity
                Intent intent = new Intent(payment.this, BuyTicket.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateInput() {
        // Get input values
        String cardNumber = editCardNumber.getText().toString();
        String expiry = editExpiry.getText().toString();
        String cvv = editCVV.getText().toString();
        String name = editName.getText().toString();
        String address = editAddress.getText().toString();
        String postalCode = editPostalCode.getText().toString();

        // Validate card number
        if (cardNumber.length() != 16) {
            Toast.makeText(payment.this, "Invalid Card Number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate expiry date
        if (expiry.length() != 5){
            Toast.makeText(payment.this, "Input valid date", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate CVV
        if (cvv.length() != 3){
            Toast.makeText(payment.this, "Invalid CVV Input", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate name, address, and postal code
        if (name.isEmpty() || address.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(payment.this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void processPayment() {
        // Display payment success message
        Toast.makeText(payment.this, "Payment successful", Toast.LENGTH_SHORT).show();
        // Post booking details to Firestore
        postBookingDetailsToFirestore();
        // Show asking to connect to google calendar dialog
        new Handler().postDelayed(this::connectToGoogleCalendar, 1000); // Wait for 1 second before displaying the alert message

        // Show confirmation dialog
        //new Handler().postDelayed(this::showConfirmationDialog, 1000); // Wait for 1 second before displaying the alert message
    }

    private void postBookingDetailsToFirestore() {
        // Get user data from SharedPreferences
        String userId = sharedPreferences.getString("UserId", null);
        String name = sharedPreferences.getString("Name", null);
        // Get booking details from intent
        String concertName = getIntent().getStringExtra("concertName");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        String seatCategory = getIntent().getStringExtra("seatCategory");
        String seatNumber = getIntent().getStringExtra("seatNumber");
        int quantity = getIntent().getIntExtra("quantity", 1);
        String paymentMethod = paymentmethod.getSelectedItem().toString();
        String Time = getIntent().getStringExtra("eventTiming");

        // Create booking details map
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("userId", userId);
        bookingDetails.put("Name", name);
        bookingDetails.put("SeatCategory", seatCategory);
        bookingDetails.put("SeatNumber", seatNumber);
        bookingDetails.put("TotalPrice", totalPrice);
        bookingDetails.put("Quantity", quantity);
        bookingDetails.put("PaymentMethod", paymentMethod);
        bookingDetails.put("ConcertTitle", concertName);
        bookingDetails.put("EventTime", Time);
        bookingDetails.put("PurchaseTime", FieldValue.serverTimestamp());

        // Add booking details to Firestore
        db.collection("BookingDetails").add(bookingDetails)
                .addOnSuccessListener(documentReference -> {
                    // Clear input fields on success
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

    // ASK IF THEY WANT TO CONNECT TO GOOGLE CALENDAR
    private void connectToGoogleCalendar() {
        // Building dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to save your event to your Google Calendar?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        connectToGoogleAccount();
                        showConfirmationDialog();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        showConfirmationDialog();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Get the buttons and set their custom styles
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        // Apply the custom styles
        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

    private void showConfirmationDialog() {
        // Build the confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Contribution Matters!");
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
        // Create the dialog
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Get the buttons and set their custom styles
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        // Apply the custom styles
        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

    private void connectToGoogleAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d("GOOGLE_SIGN_IN", "Already signed in");
            navigateToGoogleCalendar();
        } else {
            Log.d("GOOGLE_SIGN_IN", "Starting sign in intent");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d("GOOGLE_SIGN_IN", "Sign-in successful");
                    navigateToGoogleCalendar();
                }
            } catch (ApiException e) {
                Log.e("GOOGLE_SIGN_IN", "Sign-in failed: " + e.getMessage());
                Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToGoogleCalendar() {
        Intent eventIntent = getIntent();
        Event eventObj = (Event) eventIntent.getSerializableExtra("event");
        Log.d("SIGNED IN", "navigateToGoogleCalendar: ");

        if (eventObj == null) {
            Log.e("EVENT_NULL", "Event object is null");
            Toast.makeText(this, "Event details are missing", Toast.LENGTH_SHORT).show();
            return; // Early return to avoid further processing
        }

        // Get booking details from intent
        String concertName = getIntent().getStringExtra("concertName");
        String seatCategory = getIntent().getStringExtra("seatCategory");
        String seatNumber = getIntent().getStringExtra("seatNumber");
        String eventTiming = getIntent().getStringExtra("eventTiming");
        String eventVenue = eventObj.getVenue();

        // Log the event details for debugging
        Log.d("EVENT DETAILS", "Title: " + concertName);
        Log.d("EVENT VENUE",  eventVenue);

        // Create the calendar event intent with the concert details
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.TITLE, concertName);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Seat: " + seatCategory + ", " + seatNumber);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventVenue);

        // Set the event start and end time in Singapore time
        GregorianCalendar startDate = new GregorianCalendar(2024, 5, 14, 19, 0);
        GregorianCalendar endDate = new GregorianCalendar(2024, 5, 14, 22, 0);

        // Convert the start and end times to Singapore time zone
        TimeZone singaporeTimeZone = TimeZone.getTimeZone("Asia/Singapore");
        startDate.setTimeZone(singaporeTimeZone);
        endDate.setTimeZone(singaporeTimeZone);

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, singaporeTimeZone.getID());
        intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
        intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        startActivity(intent);
    }


    private void listCalendarIntentHandlers() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(CalendarContract.CONTENT_URI);
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfoList.isEmpty()) {
            Log.e("INTENT HANDLING", "No applications can handle calendar intent.");
        } else {
            for (ResolveInfo resolveInfo : resolveInfoList) {
                Log.d("INTENT HANDLING", "Package: " + resolveInfo.activityInfo.packageName);
            }
        }
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
        // Get booking details from intent
        String concertName = getIntent().getStringExtra("concertName");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        String seatCategory = getIntent().getStringExtra("seatCategory");
        String seatNumber = getIntent().getStringExtra("seatNumber");
        int quantity = getIntent().getIntExtra("quantity", 1);
        String eventTiming = getIntent().getStringExtra("eventTiming");

        // Create and show booking details dialog
        Dialog dialog = new Dialog(payment.this);
        dialog.setContentView(R.layout.bookingdetails);

        // Initialize UI components in the dialog
        TextView concertText = dialog.findViewById(R.id.concertText);
        TextView categoryText = dialog.findViewById(R.id.categoryText);
        TextView numberText = dialog.findViewById(R.id.numberText);
        TextView priceText = dialog.findViewById(R.id.priceText);
        TextView quantityText = dialog.findViewById(R.id.quantityText);
        TextView timingText = dialog.findViewById(R.id.timingText);

        // Set booking details in the dialog
        concertText.setText(concertName);
        categoryText.setText(seatCategory);
        numberText.setText(seatNumber);
        priceText.setText("Price: $" + totalPrice);
        quantityText.setText("Quantity: " + quantity);
        timingText.setText(eventTiming);
        dialog.show();
    }
}
