package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class UnfriendActivity extends AppCompatActivity {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private UnfriendAdapter adapter;
    private List<User> friendList;
    private static final String TAG = "unfriendActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unfriend_friend_activity);

        try {
            // Step 1: Initialize UI components
            searchView = findViewById(R.id.searchFriendsToUnfriend);
            recyclerView = findViewById(R.id.exploreView);
            ImageButton addFriendButton = findViewById(R.id.addFriendPageButton);

            // Step 2: Retrieve data from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String json = sharedPreferences.getString("friendList", null); // Note the null check
            String currentUserId = sharedPreferences.getString("UserId", null); // Retrieve current user ID
            friendList = new ArrayList<>();
            if (json != null && !json.isEmpty()) {
                Gson gson = new Gson();
                friendList = gson.fromJson(json, new TypeToken<List<User>>() {}.getType());
            } else {
                Log.e(TAG, "No friend list found in SharedPreferences.");
            }

            // Log the retrieved friend list size for debugging
            Log.d(TAG, "Retrieved friend list size: " + friendList.size());

            // Step 3: Set up the RecyclerView and Adapter
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new UnfriendAdapter(this, friendList, currentUserId);
            recyclerView.setAdapter(adapter);

            // Step 4: Set up listeners
            // Set onclick listener for friend page
            addFriendButton.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to FriendsActivity");
                Intent intent = new Intent(UnfriendActivity.this, FriendsActivity.class);
                startActivity(intent);
            });

            // Set up search listener
            setupSearchListener();

            // Step 5: Footer setup
            Footer.setUpFooter(this);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
        }
    }

    private void showUsers() {
        adapter.show(friendList);
    }

    private void setupSearchListener() {
        Log.d(TAG, "setupSearchListener: " + friendList.size());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                if (query == null || query.trim().isEmpty()) {
                    adapter.show(friendList);
                } else {
                    adapter.filter(query); // Filter adapter based on user input
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                if (newText == null || newText.trim().isEmpty()) {
                    adapter.show(friendList);
                } else {
                    adapter.filter(newText); // Filter adapter based on user input
                }
                return false;
            }
        });
    }
}
