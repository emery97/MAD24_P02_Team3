package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import android.view.View;


public class TransferTicketsActivity extends AppCompatActivity {

    private static final String TAG = "TransferTicketsActivity";

    private TextView friendsTextView;
    private RecyclerView recyclerView;
    private FriendsAdapter friendsAdapter;
    private List<User> friendsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_tickets_activity);

        friendsTextView = findViewById(R.id.friends);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(this, friendsList);
        recyclerView.setAdapter(friendsAdapter);

        // Initialise database components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Get shared preferences for user data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Get user ID from shared preferences and fetch friends
        String currentUserId = sharedPreferences.getString("UserId", null);

        if (currentUserId != null) {
            fetchFriends(currentUserId);
        } else {
            Log.d(TAG, "Current user ID is null.");
        }

        // Set up footer
        Footer.setUpFooter(this);

    }
    // Method to handle backButton click
    public void onBackButtonClick(View view) {
        // Navigate back to BookingHistoryDetails activity
        Intent intent = new Intent(this, BookingHistoryDetails.class);
        startActivity(intent);
        finish(); // Finish current activity (TransferTicketsActivity)
    }
    private void fetchFriends(String currentUserId) {
        Log.d(TAG, "Fetching friends for currentUserId: " + currentUserId);

        db.collection("Account")
                .whereEqualTo("userId", Integer.parseInt(currentUserId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully fetched document for currentUserId: " + currentUserId);
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                List<String> friendsIds = (List<String>) document.get("friends");
                                if (friendsIds != null && !friendsIds.isEmpty()) {
                                    Log.d(TAG, "Friends found: " + friendsIds.size());
                                    loadFriends(friendsIds);
                                } else {
                                    Log.d(TAG, "No friends found for this user or friends list is empty.");
                                }
                            } else {
                                Log.d(TAG, "Document does not exist for currentUserId: " + currentUserId);
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
            Log.d(TAG, "Fetching data for friendId: " + friendId);
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
                                for (DocumentSnapshot document : t.getResult()) {
                                    Log.d(TAG, "Document data for friendId: " + document.getData());
                                    if (document.exists()) {
                                        String name = document.getString("Name");
                                        String profileImageUrl = document.getString("ProfilePicUrl");
                                        if (name != null) {
                                            User friend = new User(name, profileImageUrl);
                                            friendsList.add(friend);
                                            Log.d(TAG, "Friend added: " + name);
                                        } else {
                                            Log.d(TAG, "Name field is null for friendId.");
                                        }
                                    } else {
                                        Log.d(TAG, "No such document for friendId.");
                                    }
                                }
                            } else {
                                Log.d(TAG, "get failed for task: " + t.getException());
                            }
                        }
                        runOnUiThread(() -> {
                            friendsAdapter.notifyDataSetChanged();
                            Log.d(TAG, "friendsList size after adding friends: " + friendsList.size());
                        });
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}

