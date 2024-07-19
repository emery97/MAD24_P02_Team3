package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.util.Log;
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

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.find_friends);

        searchView = findViewById(R.id.searchFriends);
        recyclerView = findViewById(R.id.exploreView);

        // Initialize userList with data (e.g., fetched from Firestore)
        userList = new ArrayList<>(); // Populate userList with your data

        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupSearchListener();
        fetchUsers(); // Fetch users from Firestore

        // Set the activity as listener for friend addition
        adapter.setOnFriendAddListener(this);

        // Set up footer
        Footer.setUpFooter(this);
    }

    private void fetchUsers() {
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
                    } else {
                        Log.e("FriendsActivity", "Error fetching users", task.getException());
                    }
                });
    }

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query); // Filter adapter based on user input
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText); // Filter adapter based on user input
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
