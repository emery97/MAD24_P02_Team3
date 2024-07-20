package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements UserAdapter.OnFriendAddListener {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private static final String TAG = "friendsActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.find_friends);

        searchView = findViewById(R.id.searchFriends);
        if (searchView == null) {
            Log.e(TAG, "SearchView is null");
        } else {
            Log.d(TAG, "SearchView initialized successfully");
        }

        recyclerView = findViewById(R.id.exploreView);

        // Initialize userList with data (e.g., fetched from Firestore)
        userList = new ArrayList<>(); // Populate userList with your data

        // Get shared preferences for user data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("UserId", null);
        adapter = new UserAdapter(this, userList, currentUserId);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch users from Firestore
        fetchUsers(new FetchUsersCallback() {
            @Override
            public void onFetchCompleted(List<User> userList) {
                showUsers();
                setupSearchListener(); // Set up search listener after fetching users
            }
        });
        // Set up footer
        Footer.setUpFooter(this);
    }

    // define a callback interface
    public interface FetchUsersCallback {
        void onFetchCompleted(List<User> userList);
    }

    private void fetchUsers(FetchUsersCallback callback) {
        db.collection("Account")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = new User();
                            user.setName(document.getString("Name"));
                            user.setUserId(document.getLong("userId").toString());
                            String profilePicURL = document.getString("ProfilePicUrl");
                            if (profilePicURL != null) {
                                user.setProfileImageUrl(profilePicURL);
                            } else {
                                user.setProfileImageUrl(""); // set empty url
                            }
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                        // notify callback
                        callback.onFetchCompleted(userList);
                    } else {
                        Log.e("FriendsActivity", "Error fetching users", task.getException());
                    }
                });
    }
    private void showUsers(){
        adapter.show(userList);
    }

    private void setupSearchListener() {
        Log.d(TAG, "setupSearchListener: " + userList.size());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                if (query == null || query.trim().isEmpty()) {
                    adapter.show(userList);
                } else {
                    adapter.filter(query); // Filter adapter based on user input
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                if (newText == null || newText.trim().isEmpty()) {
                    adapter.show(userList);
                } else {
                    adapter.filter(newText); // Filter adapter based on user input
                }
                return false;
            }
        });
    }

    @Override
    public void onFriendAdded(User user) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Account").document(currentUserId)
                .update("friends", FieldValue.arrayUnion(user.getUserId()))
                .addOnSuccessListener(aVoid -> {
                    // Successfully added friend
                    Log.d("FriendsActivity", "Friend added successfully");
                })
                .addOnFailureListener(e -> {
                    // Failed to add friend
                    Log.e("FriendsActivity", "Failed to add friend", e);
                });
    }
}
