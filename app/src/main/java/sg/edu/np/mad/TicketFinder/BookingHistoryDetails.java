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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear existing booking details list
                            bookingDetailsList.clear();
                            // Populate booking details list
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String seatCategory = document.getString("SeatCategory");
                                String seatNumber = document.getString("SeatNumber");

                                // Retrieve TotalPrice as double and convert to String
                                Double totalPrice = document.getDouble("TotalPrice");
                                String totalPriceString = totalPrice != null ? String.valueOf(totalPrice) : null;

                                // Retrieve Quantity as long and convert to String
                                Long quantityLong = document.getLong("Quantity");
                                String quantityString = quantityLong != null ? String.valueOf(quantityLong) : null;

                                String paymentMethod = document.getString("PaymentMethod");

                                // Create BookingDetails object and add to list
                                BookingDetails bookingDetails = new BookingDetails(seatCategory, seatNumber, totalPriceString, quantityString, paymentMethod);
                                bookingDetailsList.add(bookingDetails);
                            }
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
}