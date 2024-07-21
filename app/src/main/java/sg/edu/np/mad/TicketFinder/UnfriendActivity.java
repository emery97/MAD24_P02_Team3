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
            searchView = findViewById(R.id.searchFriends);
            recyclerView = findViewById(R.id.exploreView);
            ImageButton addFriendButton = findViewById(R.id.addFriendPageButton);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            friendList = new ArrayList<>();

            // Retrieve friend list from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String json = sharedPreferences.getString("friendList", null); // Note the null check
            if (json != null && !json.isEmpty()) {
                Gson gson = new Gson();
                friendList = gson.fromJson(json, new TypeToken<List<User>>() {}.getType());
            } else {
                Log.e(TAG, "No friend list found in SharedPreferences.");
            }

            // Log the retrieved friend list size for debugging
            Log.d(TAG, "Retrieved friend list size: " + friendList.size());

            // Initialize the adapter and set it to the RecyclerView
            adapter = new UnfriendAdapter(this, friendList);
            recyclerView.setAdapter(adapter);

            // Set onclick listener for friend page
            addFriendButton.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to FriendsActivity");
                Intent intent = new Intent(UnfriendActivity.this, FriendsActivity.class);
                startActivity(intent);
            });

            Footer.setUpFooter(this);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
        }
    }
}
