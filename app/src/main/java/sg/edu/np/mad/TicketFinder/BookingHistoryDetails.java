package sg.edu.np.mad.TicketFinder;

import static sg.edu.np.mad.TicketFinder.R.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class BookingHistoryDetails extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookingDetailsAdapter bookingDetailsAdapter;
    private List<BookingDetails> bookingDetailsList = new ArrayList<>();
    private static final String SIGN_IN_STATUS_KEY = "SignInStatus";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_history_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Get shared preferences for user data
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize connect to google button
        View connectToGoogleAccount = findViewById(R.id.connectToGoogleCalendar);
        connectToGoogleAccount.setOnClickListener(v -> connectToGoogleAccount());

        // Get user ID from shared preferences and fetch booking details
        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            fetchBookingDetailsData(userId, null); // No callback needed here
        }

        // Set up footer
        Footer.setUpFooter(this);
    }

    // Method to fetch booking details data from Firestore
    private void fetchBookingDetailsData(String userId, Runnable callback) {
        db.collection("BookingDetails")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            bookingDetailsList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String eventTitle = document.getString("ConcertTitle");
                                String seatCategory = document.getString("SeatCategory");
                                String seatNumber = document.getString("SeatNumber");
                                Double totalPrice = document.getDouble("TotalPrice");
                                String totalPriceString = totalPrice != null ? String.valueOf(totalPrice) : null;
                                Long quantityLong = document.getLong("Quantity");
                                String quantityString = quantityLong != null ? String.valueOf(quantityLong) : null;
                                String paymentMethod = document.getString("PaymentMethod");
                                BookingDetails bookingDetails = new BookingDetails(eventTitle, seatCategory, seatNumber, totalPriceString, quantityString, paymentMethod);
                                bookingDetailsList.add(bookingDetails);
                            }
                            bookingDetailsAdapter = new BookingDetailsAdapter(bookingDetailsList);
                            recyclerView.setAdapter(bookingDetailsAdapter);

                        } else {
                            Log.d("BookingHistoryDetails", "No booking details found");
                        }
                    } else {
                        Log.d("BookingHistoryDetails", "Error getting documents: ", task.getException());
                    }
                    if (callback != null) {
                        callback.run();
                    }
                });
        // Log the last item in the list if not empty
        if (!bookingDetailsList.isEmpty()) {
            Log.d("INSIDE FUNCTION", bookingDetailsList.get(bookingDetailsList.size() - 1).getConcertName());
        }
    }

    private void connectToGoogleAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Toast.makeText(this, "Already signed in", Toast.LENGTH_SHORT).show();
            String userId = sharedPreferences.getString("UserId", null);
            if (userId != null) {
                fetchBookingDetailsData(userId, this::fillCalendarEventWithLatestBooking);
            } else {
                fillCalendarEventWithLatestBooking();
            }
        } else {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            String userId = sharedPreferences.getString("UserId", null);
            Log.d("epic fail", "FAILED TO FILL CALENDAR");
            if (userId != null) {
                fetchBookingDetailsData(userId, this::fillCalendarEventWithLatestBooking);
            } else {
                fillCalendarEventWithLatestBooking();
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            Log.d("LOGIN FAILED", "handleSignInResult: " + e);
        }
    }

    private void fillCalendarEventWithLatestBooking() {
        if (bookingDetailsList.isEmpty()) {
            Toast.makeText(this, "No booking done", Toast.LENGTH_SHORT).show();
            return;
        }

        BookingDetails latestBooking = bookingDetailsList.get(bookingDetailsList.size() - 1);
        Log.d("LATEST_BOOKING", String.valueOf(bookingDetailsList.size()));

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(android.provider.CalendarContract.Events.TITLE, latestBooking.getConcertName());
        intent.putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, "Concert Location");
        intent.putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Seat: " + latestBooking.getSeatCategory() + ", " + latestBooking.getSeatNumber());

        GregorianCalendar startDate = new GregorianCalendar(2024, 5, 14, 19, 0);
        GregorianCalendar endDate = new GregorianCalendar(2024, 5, 14, 22, 0);

        intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTimeInMillis());
        intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTimeInMillis());
        intent.putExtra(android.provider.CalendarContract.Events.ACCESS_LEVEL, android.provider.CalendarContract.Events.ACCESS_PRIVATE);
        intent.putExtra(android.provider.CalendarContract.Events.AVAILABILITY, android.provider.CalendarContract.Events.AVAILABILITY_BUSY);

        startActivity(intent);
    }
}
