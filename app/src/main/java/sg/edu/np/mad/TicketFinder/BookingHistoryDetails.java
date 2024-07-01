package sg.edu.np.mad.TicketFinder;

import static sg.edu.np.mad.TicketFinder.R.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingHistoryDetails extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // Shared preferences for storing user data
    private FirebaseFirestore db;
    private RecyclerView recyclerView; // RecyclerView to display booking details
    private BookingDetailsAdapter bookingDetailsAdapter;
    private List<BookingDetails> bookingDetailsList = new ArrayList<>();

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

        // Get user ID from shared preferences and fetch booking details
        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            fetchBookingDetailsData(userId);
        }

        // Set up footer
        Footer.setUpFooter(this);
    }

    // Method to fetch booking details data from Firestore
    private void fetchBookingDetailsData(String userId) {
        db.collection("BookingDetails")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> { // Coded with the help of chatGPT
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear existing booking details list
                            bookingDetailsList.clear();
                            // Populate booking details list
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

                                // Create BookingDetails object and add to list
                                BookingDetails bookingDetails = new BookingDetails(eventTitle,purchaseTimeString,time,seatCategory, seatNumber, totalPriceString, quantityString, paymentMethod);
                                bookingDetailsList.add(bookingDetails);
                            }

                            Collections.sort(bookingDetailsList, (booking1, booking2) -> {
                                // Convert purchase time strings to Date objects for comparison
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
                                try {
                                    Date date1 = sdf.parse(booking1.getpurcasetime());
                                    Date date2 = sdf.parse(booking2.getpurcasetime());
                                    // Sort in descending order (most recent first)
                                    return date2.compareTo(date1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });
                            // Initialize and set adapter for RecyclerView
                            bookingDetailsAdapter = new BookingDetailsAdapter(bookingDetailsList);
                            recyclerView.setAdapter(bookingDetailsAdapter);
                        } else {
                            Log.d("BookingHistoryDetails", "No booking details found");
                        }
                    } else {
                        Log.d("BookingHistoryDetails", "Error getting documents: ", task.getException());
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