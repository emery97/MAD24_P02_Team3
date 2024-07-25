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
import android.view.autofill.AutofillManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class payment extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // SharedPreferences for storing buy ticket data
    private SharedPreferences userSharedPreferences; // SharedPreferences for storing user data
    private Spinner paymentmethod;

    private EditText editCardNumber, editExpiry, editCVV, editName, editAddress, editPostalCode; // EditText fields for payment details
    private Button buyNow;
    private Button bookingdetails;
    private TextView totalPricetext;
    private Button cancel;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private SeatSelectionViewModel viewModel;
    private static String TAG = "payment";
    private HashMap<String, ArrayList<String>> latestSeatMap = new HashMap<>();
    private boolean autofillUsed = false;
    List<Integer>ticketIDs;

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
        promptRetrieveCredentials();

        // Retrieve the seatMap from the intent
        Intent intent = getIntent();
        latestSeatMap = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra("seatMap");

        // Log the seatMap for debugging
        for (Map.Entry<String, ArrayList<String>> entry : latestSeatMap.entrySet()) {
            String seatCategoryKey = entry.getKey();
            ArrayList<String> seatNumbers = entry.getValue();
            Log.d(TAG, "onCreate: SEAT CAT " + seatCategoryKey);
            Log.d(TAG, "onCreate: SEAT NO. " + seatNumbers);
        }
    }

    private void promptRetrieveCredentials() {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle("Retrieve Saved Credentials")
                .setMessage("Do you want to retrieve your saved card details?")
                .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                    promptBiometricForRetrieve();
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, which) -> {
                    // User chose not to retrieve saved credentials
                    editCardNumber.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                    editExpiry.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                    editCVV.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.dialogButtonColor));
            dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.dialogButtonColor));
        });

        dialog.show();
    }

    // Prompt for biometric authentication before retrieving credentials
    private void promptBiometricForRetrieve() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Use payment.this.runOnUiThread() to access runOnUiThread
                payment.this.runOnUiThread(() -> {
                    autofillCredentials();
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Handle error or cancel
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Handle failure
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential to retrieve saved credentials")
                .setDeviceCredentialAllowed(true) // Allow using device credentials (PIN, pattern, password)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    // Autofill credentials after biometric authentication succeeds
    private void autofillCredentials() {
        AutofillManager autofillManager = this.getSystemService(AutofillManager.class);
        if (autofillManager != null) {
            editCardNumber.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            editExpiry.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            editCVV.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            autofillManager.requestAutofill(editCardNumber);
            autofillManager.requestAutofill(editExpiry);
            autofillManager.requestAutofill(editCVV);
            autofillUsed = true; // Set autofill used to true
        }
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
        // Post tickets to Firestore
        postTicketToFirestore();
        // Show asking to connect to google calendar dialog
        new Handler().postDelayed(this::connectToGoogleCalendar, 1000); // Wait for 1 second before displaying the alert message

        // Show confirmation dialog
        //new Handler().postDelayed(this::showConfirmationDialog, 1000); // Wait for 1 second before displaying the alert message
    }

//    private void postBookingDetailsToFirestore() {
//        // Get user data from SharedPreferences
//        String userId = sharedPreferences.getString("UserId", null);
//        String name = sharedPreferences.getString("Name", null);
//        // Get booking details from intent
//        String concertName = getIntent().getStringExtra("concertName");
//        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
//        String seatCategory = getIntent().getStringExtra("seatCategory");
//        String seatNumber = getIntent().getStringExtra("seatNumber");
//        int quantity = getIntent().getIntExtra("quantity", 1);
//        String paymentMethod = paymentmethod.getSelectedItem().toString();
//        String Time = getIntent().getStringExtra("eventTiming");
//
//        // Create booking details map
//        Map<String, Object> bookingDetails = new HashMap<>();
//        bookingDetails.put("userId", userId);
//        bookingDetails.put("Name", name);
//        bookingDetails.put("SeatCategory", seatCategory);
//        bookingDetails.put("SeatNumber", seatNumber);
//        bookingDetails.put("TotalPrice", totalPrice);
//        bookingDetails.put("Quantity", quantity);
//        bookingDetails.put("PaymentMethod", paymentMethod);
//        bookingDetails.put("ConcertTitle", concertName);
//        bookingDetails.put("EventTime", Time);
//        bookingDetails.put("PurchaseTime", FieldValue.serverTimestamp());
//
//        // Add booking details to Firestore
//        db.collection("BookingDetails").add(bookingDetails)
//                .addOnSuccessListener(documentReference -> {
//                    // Clear input fields on success
//                    editCardNumber.setText("");
//                    editAddress.setText("");
//                    editCVV.setText("");
//                    editExpiry.setText("");
//                    editName.setText("");
//                    editPostalCode.setText("");
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(payment.this, "Error saving booking details", Toast.LENGTH_SHORT).show();
//                });
//    }


    // post ticket to firestore

    public void postTicketToFirestore() {
        // Get user data from SharedPreferences
        sharedPreferences = getSharedPreferences("TicketFinderPrefs", Context.MODE_PRIVATE);
        userSharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = userSharedPreferences.getString("UserId", null);
        String name = userSharedPreferences.getString("Name", null);

        String concertName = getIntent().getStringExtra("concertName");
        String time = getIntent().getStringExtra("eventTiming");

        // Fetch the latest ticket ID from Firestore
        db.collection("Ticket")
                .orderBy("TicketID", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestTicket = task.getResult().getDocuments().get(0);
                        int latestTicketID = latestTicket.getLong("TicketID").intValue();
                        postTickets(latestTicketID + 1, userId, name, concertName, time);
                    } else {
                        // No tickets in the collection, start with ID 1
                        postTickets(1, userId, name, concertName, time);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(payment.this, "Error fetching latest ticket ID", Toast.LENGTH_SHORT).show();
                });
    }

    private void postTickets(int startingTicketID, String userId, String name, String concertName, String time) {
        List<Map<String, Object>> ticketList = new ArrayList<>();
        List<Integer> ticketIDs = new ArrayList<>();  // Initialize ticketIDs list
        int currentTicketID = startingTicketID;

        // Initialize bookingDetails map
        Map<String, Object> bookingDetails = new HashMap<>();
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        int quantity = getIntent().getIntExtra("quantity", 1);
        String paymentMethod = paymentmethod.getSelectedItem().toString();

        bookingDetails.put("ConcertTitle", concertName);
        bookingDetails.put("EventTime", time);
        bookingDetails.put("Name", name);
        bookingDetails.put("PaymentMethod", paymentMethod);
        bookingDetails.put("PurchaseTime", FieldValue.serverTimestamp());
        bookingDetails.put("Quantity", quantity);
        bookingDetails.put("TotalPrice", totalPrice);
        bookingDetails.put("userId", userId);

        boolean firstSeatAdded = false;

        for (Map.Entry<String, ArrayList<String>> entry : latestSeatMap.entrySet()) {
            String seatCategoryKey = entry.getKey();
            ArrayList<String> seatNumbers = entry.getValue();
            Log.d(TAG, "postTicketToFirestore: SEAT CAT " + seatCategoryKey);
            Log.d(TAG, "postTicketToFirestore: SEAT NO. " + seatNumbers);

            for (String seatNumber : seatNumbers) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("ConcertTitle", concertName);
                ticket.put("EventTime", time);
                ticket.put("Name", name);
                ticket.put("SeatCategory", seatCategoryKey);
                ticket.put("SeatNumber", seatNumber);
                ticket.put("TicketID", currentTicketID); // Use incremental ID
                ticket.put("userId", userId);
                ticketList.add(ticket);
                ticketIDs.add(currentTicketID++); // Add ticketID to list and increment

                // Add seat details to bookingDetails once
                if (!firstSeatAdded) {
                    bookingDetails.put("SeatCategory", seatCategoryKey);
                    bookingDetails.put("SeatNumber", seatNumber);
                    firstSeatAdded = true;
                }
            }
        }

        // Add tickets to Firestore
        for (Map<String, Object> ticket : ticketList) {
            db.collection("Ticket").add(ticket)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Ticket added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(payment.this, "Error saving tickets ", Toast.LENGTH_SHORT).show();
                    });
        }

        // Add booking details to Firestore once
        db.collection("BookingDetails").add(bookingDetails)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Booking details added with ID: " + documentReference.getId());
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

        // After tickets are added, you can now post the booking details
        postBookingDetailsToFirestore(ticketIDs, userId, name, concertName, time);
    }


    private void postBookingDetailsToFirestore(List<Integer> ticketIDs, String userId, String name, String concertName, String time) {
        // Get booking details from intent
        sharedPreferences = getSharedPreferences("TicketFinderPrefs", Context.MODE_PRIVATE);
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        int quantity = getIntent().getIntExtra("quantity", 1);
        String paymentMethod = paymentmethod.getSelectedItem().toString();

        // Create booking details map
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("ConcertTitle", concertName);
        bookingDetails.put("EventTime", time);
        bookingDetails.put("Name", name);
        bookingDetails.put("PaymentMethod", paymentMethod);
        bookingDetails.put("PurchaseTime", FieldValue.serverTimestamp());
        bookingDetails.put("Quantity", quantity);
        bookingDetails.put("TotalPrice", totalPrice);
        bookingDetails.put("TicketIDs", ticketIDs);
        bookingDetails.put("UserID", userId);

        // Add booking details to Firestore
        db.collection("BookingDetailsII").add(bookingDetails)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Booking details added with ID: " + documentReference.getId());
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
                        if (checkAndRequestPermissions()) {
                            connectToGoogleAccount();
                            showConfirmationDialog();
                        }
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

    private boolean checkAndRequestPermissions() {
        String[] permissions = {
                android.Manifest.permission.WRITE_CALENDAR,
                android.Manifest.permission.READ_CALENDAR
        };
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();
            // Initialize the map with permissions
            perms.put(android.Manifest.permission.WRITE_CALENDAR, PackageManager.PERMISSION_GRANTED);
            perms.put(android.Manifest.permission.READ_CALENDAR, PackageManager.PERMISSION_GRANTED);
            // Fill with actual results from user
            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i], grantResults[i]);
            }
            // Check for both permissions
            if (perms.get(android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                    && perms.get(android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                // All permissions are granted
                connectToGoogleAccount();
            } else {
                // Permission is denied (this is the first time, when "never ask again" is not checked)
                Log.d("permission", "Some permissions are not granted. Ask again.");
            }
        }
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
        Log.d("EVENT VENUE", eventVenue);
        Log.d("EVENT TIME", eventTiming);

        // Parse the eventTiming to set the start and end times
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(eventTiming, formatter);
        LocalDateTime endDateTime = startDateTime.plusHours(3); // Assuming the event lasts for 3 hours

        // Convert the start and end times to GregorianCalendar
        GregorianCalendar startDate = GregorianCalendar.from(startDateTime.atZone(ZoneId.of("Asia/Singapore")));
        GregorianCalendar endDate = GregorianCalendar.from(endDateTime.atZone(ZoneId.of("Asia/Singapore")));

        // Create the calendar event intent with the concert details
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
        calendarIntent.setType("vnd.android.cursor.item/event");
        calendarIntent.putExtra(CalendarContract.Events.TITLE, concertName);
        calendarIntent.putExtra(CalendarContract.Events.DESCRIPTION, "Seat: " + seatCategory + ", " + seatNumber);
        calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventVenue);

        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Singapore");
        calendarIntent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
        calendarIntent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        // Start the calendar intent
        startActivity(calendarIntent);

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
