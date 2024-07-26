package sg.edu.np.mad.TicketFinder;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.HashMap;
import java.util.Map;

// Class definition
public class UpcomingConcertsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // Shared preferences for storing user data
    private FirebaseFirestore db;
    private RecyclerView recyclerView; // RecyclerView to display booking details
    private UpcomingConcertsAdapter upcomingConcertsAdapter;
    private static final String TAG = "UpcomingConcertsActivity";
    private List<BookingDetailsII> bookingDetailsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.upcoming_concert);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Get shared preferences for user data
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get user ID from shared preferences and fetch upcoming concerts
        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            fetchUpcomingConcerts(userId);
        }

        // Set up footer
        Footer.setUpFooter(this);
    }

    private void fetchUpcomingConcerts(String userId) {
        db.collection("BookingDetailsII")
                .whereEqualTo("UserID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Date currentDate = new Date();
                            Log.d(TAG, "fetchUpcomingConcerts: COMES HERE");

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String documentId = document.getId();
                                String concertTitle = document.getString("ConcertTitle");
                                String eventTime = document.getString("EventTime");
                                String name = document.getString("Name");
                                String paymentMethod = document.getString("PaymentMethod");
                                Timestamp purchaseTime = document.getTimestamp("PurchaseTime");
                                int quantity = document.getLong("Quantity").intValue();
                                List<Long> ticketIDs = (List<Long>) document.get("TicketIDs");
                                double totalPrice = document.getDouble("TotalPrice");
                                Log.d(TAG, "fetchUpcomingConcerts: EVENT TIME FROM DOCUMENT"+ eventTime);
                                Log.d(TAG, "fetchUpcomingConcerts: CURRENT TIME"+currentDate);
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
                                    Date eventDate = sdf.parse(eventTime);
                                    if (eventDate != null && eventDate.after(currentDate)) {
                                        BookingDetailsII bookingDetails = new BookingDetailsII(concertTitle, eventTime, name, paymentMethod, purchaseTime, quantity, ticketIDs, totalPrice);
                                        bookingDetailsList.add(bookingDetails);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            upcomingConcertsAdapter = new UpcomingConcertsAdapter(bookingDetailsList, db);
                            recyclerView.setAdapter(upcomingConcertsAdapter);
                        } else {
                            Log.d("UpcomingConcertsActivity", "No upcoming concerts found");
                        }
                    } else {
                        Log.d("UpcomingConcertsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }


    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
            return sdf.format(date);
        }
        return "";
    }
}

