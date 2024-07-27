package sg.edu.np.mad.TicketFinder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UpcomingConcertsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private UpcomingConcertsAdapter upcomingConcertsAdapter;
    private static final String TAG = "UpcomingConcertsActivity";
    private List<UpcomingConcert> upcomingConcertList = new ArrayList<>();
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.upcoming_concert);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Get shared preferences for user data
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Retrieve user ID and name from shared preferences
        currentUserId = sharedPreferences.getString("UserId", null);
        currentUserName = sharedPreferences.getString("Name", null);
        Log.d(TAG, "Current User ID: " + currentUserId + ", Current User Name: " + currentUserName);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch upcoming concerts
        fetchUpcomingConcerts();

        // Set up footer
        Footer.setUpFooter(this);
    }
    /**
     * Fetches upcoming concert data from the Firestore database, filters by current user ID, and initializes the RecyclerView adapter.
     */
    private void fetchUpcomingConcerts() {
        String currentUserId = sharedPreferences.getString("UserId", null);

        db.collection("UpcomingConcert")
                .whereEqualTo("UserID", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UpcomingConcert> concertsList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Log.d(TAG, "Document data: " + documentSnapshot.getData());

                        String concertTitle = documentSnapshot.getString("ConcertTitle");
                        String eventTime = documentSnapshot.getString("EventTime");
                        String name = documentSnapshot.getString("Name");
                        Long quantityLong = documentSnapshot.getLong("Quantity");
                        List<Long> ticketIDs = (List<Long>) documentSnapshot.get("TicketIDs");
                        String userId = documentSnapshot.getString("UserID");

                        int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                        if (concertTitle != null && eventTime != null && ticketIDs != null && userId != null) {
                            UpcomingConcert concert = new UpcomingConcert(concertTitle, eventTime, name, quantity, ticketIDs, userId);
                            concertsList.add(concert);
                            Log.d(TAG, "Concert retrieved: " + concert.getConcertTitle() + ", Ticket IDs: " + concert.getTicketIDs());
                        } else {
                            Log.w(TAG, "Missing fields in document: " + documentSnapshot.getId());
                        }
                    }
                    // Initialize the adapter with the fetched data
                    upcomingConcertsAdapter = new UpcomingConcertsAdapter(concertsList, db, currentUserId, currentUserName);
                    recyclerView.setAdapter(upcomingConcertsAdapter);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching upcoming concerts", e));
    }



}
