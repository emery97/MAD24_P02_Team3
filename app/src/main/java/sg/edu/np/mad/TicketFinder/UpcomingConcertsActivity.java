package sg.edu.np.mad.TicketFinder;

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

public class UpcomingConcertsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // Shared preferences for storing user data
    private FirebaseFirestore db;
    private RecyclerView recyclerView; // RecyclerView to display booking details
    private UpcomingConcertsAdapter upcomingConcertsAdapter;
    private List<BookingDetails> upcomingConcertsList = new ArrayList<>();


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
            Log.d("UPCOMING CONCERTS", "onCreate: "+userId);
            fetchUpcomingConcerts(userId);
        }

        // Set up footer
        Footer.setUpFooter(this);

        // setonclicklistener for transfer tickets
//        Button transferTicketButton = findViewById(R.id.transferTickets);
//        transferTicketButton.setOnClickListener(v -> {
//            Intent intent = new Intent(UpcomingConcertsActivity.this, TransferTicketsActivity.class);
//            startActivity(intent);
//        });
    }

    // Method to fetch upcoming concerts
    private void fetchUpcomingConcerts(String userId) {
        db.collection("BookingDetails")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear existing booking details list
                            upcomingConcertsList.clear();
                            // Current date to compare with concert dates
                            Date currentDate = new Date();

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String eventTitle = document.getString("ConcertTitle");
                                String seatCategory = document.getString("SeatCategory");
                                String seatNumber = document.getString("SeatNumber");

                                // Retrieve TotalPrice as double and convert to String
                                Double totalPrice = document.getDouble("TotalPrice");
                                String totalPriceString = totalPrice != null ? String.valueOf(totalPrice) : null;

                                // Retrieve Quantity as long and convert to String
                                Long quantityLong = document.getLong("Quantity");
                                String quantityString = quantityLong != null ? String.valueOf(quantityLong) : null;

                                String paymentMethod = document.getString("PaymentMethod");

                                String time = document.getString("EventTime");

                                Timestamp purchaseTimeTimestamp = document.getTimestamp("PurchaseTime");
                                String purchaseTimeString = formatTimestamp(purchaseTimeTimestamp);

                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
                                    Date concertDate = sdf.parse(time);
                                    if (concertDate != null && concertDate.after(currentDate)) {
                                        // Create BookingDetails object and add to list
                                        BookingDetails bookingDetails = new BookingDetails(eventTitle, purchaseTimeString, time, seatCategory, seatNumber, totalPriceString, quantityString, paymentMethod);
                                        upcomingConcertsList.add(bookingDetails);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Initialize and set adapter for RecyclerView
                            upcomingConcertsAdapter = new UpcomingConcertsAdapter(upcomingConcertsList);
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
