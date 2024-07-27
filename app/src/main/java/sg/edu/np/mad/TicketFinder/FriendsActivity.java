package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements UserAdapter.OnFriendAddListener {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private List<User> friendList;
    private List<String> friendUserId;
    private String currentUserId; // Ensure this is declared at the class level
    private static final String TAG = "friendsActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.find_friends);

        searchView = findViewById(R.id.searchFriends);
        recyclerView = findViewById(R.id.exploreView);

        userList = new ArrayList<>();
        friendList = new ArrayList<>();
        friendUserId = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("UserId", null);

//        Log.d(TAG, "Current User ID: " + currentUserId);

        adapter = new UserAdapter(this, userList, currentUserId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchCurrentUserFriends(new FetchUsersCallback() {
            @Override
            public void onFetchCompleted(List<User> userList, List<User> friendList, List<String> friendUserId) {
                fetchUsers(new FetchUsersCallback() {
                    @Override
                    public void onFetchCompleted(List<User> userList, List<User> friendList, List<String> friendUserId) {
                        for (User user : userList) {
                            Log.d(TAG, "onFetchCompleted: " + user.getName());
                        }
                        showUsers();
                        setupSearchListener();
                        storeFriendListInSharedPreferences(friendList);
                    }
                });
            }
        });

        // Set onclicklistener for unfriend page
        ImageButton unfriendButton = findViewById(R.id.friendsNavigation);
        unfriendButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: UNFRIEND BUTTON");
            Intent intent = new Intent(FriendsActivity.this, UnfriendActivity.class);
            startActivity(intent);
        });

        Footer.setUpFooter(this);
    }

    // Define a callback interface
    public interface FetchUsersCallback {
        void onFetchCompleted(List<User> userList, List<User> friendList, List<String> friendUserId);
    }

    private void storeFriendListInSharedPreferences(List<User> friendList) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(friendList);
        editor.putString("friendList", json);
        editor.apply();
    }

    /**
     * Fetches the current user's friends from Firestore.
     */
    private void fetchUsers(FetchUsersCallback callback) {
        Log.d(TAG, "fetchUsers: " + currentUserId);
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
                                user.setProfileImageUrl(""); // Set empty URL
                            }
                            if (!document.getLong("userId").toString().equals(currentUserId)) {
                                userList.add(user); // Only add users that are not the current user
                            }
                        }
                        adapter.notifyDataSetChanged();

                        // removing friend from user list
                        userList.removeIf(user -> friendUserId.contains(user.getUserId()));

                        // Notify callback
                        callback.onFetchCompleted(userList, friendList, friendUserId);

                    } else {
                        Log.e("FriendsActivity", "Error fetching users", task.getException());
                    }
                });
    }
    /**
     * Handles the extraction and processing of friend data from a Firestore task.
     */
    private void fetchCurrentUserFriends(FetchUsersCallback callback) {
        Log.d(TAG, "fetchCurrentUserFriends: " + currentUserId);
        db.collection("Account")
                .whereEqualTo("userId", Long.parseLong(currentUserId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendList.clear(); // Clear friendList before populating
                        friendUserId.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get friends list
                            List<String> friends = (List<String>) document.get("friends");
                            if (friends != null) {
                                for (String friendId : friends) {
                                    fetchFriendDetails(friendId, callback); // Pass the callback
                                    friendUserId.add(friendId); // add friend's user id to friendUserId list
                                }
                            } else {
                                Log.d(TAG, "No friends found for current user: " + currentUserId);
                                // Call the callback even if no friends are found
                                callback.onFetchCompleted(userList, friendList, friendUserId);
                            }
                        }
                    } else {
                        Log.e("FriendsActivity", "Error fetching current user friends", task.getException());
                    }
                    Log.d(TAG, "fetchCurrentUserFriends: friend user id " + friendUserId.size());
                });
    }
    /**
     * Fetches detailed user information for a  friend.
     * @param friendId specific friend id
     */

    private void fetchFriendDetails(String friendId, FetchUsersCallback callback) {
        db.collection("Account")
                .whereEqualTo("userId", Long.parseLong(friendId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User friend = new User();
                            friend.setName(document.getString("Name"));
                            friend.setUserId(document.getLong("userId").toString());
                            String profilePicURL = document.getString("ProfilePicUrl");
                            if (profilePicURL != null) {
                                friend.setProfileImageUrl(profilePicURL);
                            } else {
                                friend.setProfileImageUrl(""); // Set empty URL
                            }
                            friendList.add(friend); // Add friend to friendList
                        }
                        // Notify callback after fetching all friends
                        callback.onFetchCompleted(userList, friendList, friendUserId);
                    } else {
                        Log.e("FriendsActivity", "Error fetching friend details", task.getException());
                    }
                });
    }

    private void showUsers() {
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
