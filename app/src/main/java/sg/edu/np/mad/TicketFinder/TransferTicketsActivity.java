package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransferTicketsActivity extends AppCompatActivity {

    private static final String TAG = "TransferTicketsActivity";

    private TextView friendsTextView;
    private RecyclerView recyclerView;
    private FriendsAdapter friendsAdapter;
    private List<User> friendsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String concertName;
    private String purchaseTime;

    private Timestamp convertStringToFirestoreTimestamp(String timestampString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()); // Match the default Date.toString() format
            format.setTimeZone(TimeZone.getTimeZone("UTC")); // Ensure the timezone matches Firestore storage

            Date date = format.parse(timestampString);

            // Log the parsed date to see the exact time in milliseconds
            Log.d("TransferTicketsActivity", "Parsed date from string (UTC): " + date.toString());

            // Convert to Firestore Timestamp
            Timestamp timestamp = new Timestamp(date);
            Log.d("TransferTicketsActivity", "Converted Timestamp from string: " + timestamp.toDate().toString());

            return timestamp;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_tickets_activity);

        friendsTextView = findViewById(R.id.friends);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsList = new ArrayList<>();

        // Get shared preferences for user data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("UserId", null);

        // Get the concertName and purchaseTime from intent
        concertName = getIntent().getStringExtra("CONCERT_NAME");
        String purchaseTimeString = getIntent().getStringExtra("PURCHASE_TIME");
        long purchaseTimeSeconds = getIntent().getLongExtra("PURCHASE_TIME_DB_SECONDS", 0);
        int purchaseTimeNanoseconds = getIntent().getIntExtra("PURCHASE_TIME_DB_NANOSECONDS", 0);

        Timestamp purchaseTimeDb = new Timestamp(purchaseTimeSeconds, purchaseTimeNanoseconds);

        Log.d("TransferTicketsActivity", "Received concertName: " + concertName + ", purchaseTime: " + purchaseTimeString + ", purchaseTimeDb: " + purchaseTimeDb.toDate().toString());

        friendsAdapter = new FriendsAdapter(this, friendsList, currentUserId, concertName, purchaseTimeDb);
        recyclerView.setAdapter(friendsAdapter);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (currentUserId != null) {
            fetchFriends(currentUserId);
        } else {
            Log.d(TAG, "Current user ID is null.");
        }

        // Set up footer
        Footer.setUpFooter(this);
    }

    private void fetchFriends(String currentUserId) {
        db.collection("Account")
                .whereEqualTo("userId", Integer.parseInt(currentUserId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                List<String> friendsIds = (List<String>) document.get("friends");
                                if (friendsIds != null && !friendsIds.isEmpty()) {
                                    loadFriends(friendsIds);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void loadFriends(List<String> friendsIds) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (String friendId : friendsIds) {
            Task<QuerySnapshot> task = db.collection("Account")
                    .whereEqualTo("userId", Integer.parseInt(friendId))
                    .get();
            tasks.add(task);
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (Task<QuerySnapshot> t : tasks) {
                            if (t.isSuccessful()) {
                                for (QueryDocumentSnapshot document : t.getResult()) {
                                    if (document.exists()) {
                                        String name = document.getString("Name");
                                        String profileImageUrl = document.getString("ProfilePicUrl");
                                        Long userIdLong = document.getLong("userId");
                                        String userId = (userIdLong != null) ? String.valueOf(userIdLong) : null;

                                        if (name != null && userId != null) {
                                            User friend = new User(name, profileImageUrl, userId);
                                            friendsList.add(friend);
                                        }
                                    }
                                }
                            }
                        }
                        friendsAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}
