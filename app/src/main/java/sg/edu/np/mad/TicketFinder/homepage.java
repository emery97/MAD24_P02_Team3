package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class homepage extends AppCompatActivity {

    private RecyclerView horizontalRecyclerView;
    private EventAdapter horizontalItemAdapter;
    private RecyclerView gridRecyclerView;
    private EventAdapter gridItemAdapter;
    private dbHandler handler = new dbHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize horizontal RecyclerView
        horizontalRecyclerView = findViewById(R.id.horizontalRecyclerView);
        horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horizontalItemAdapter = new EventAdapter(homepage.this, new ArrayList<>(), false); // Pass false for horizontal layout
        horizontalRecyclerView.setAdapter(horizontalItemAdapter);

        // Initialize grid RecyclerView
        gridRecyclerView = findViewById(R.id.gridRecyclerView);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        gridItemAdapter = new EventAdapter(homepage.this, new ArrayList<>(), true); // Pass true for grid layout
        gridRecyclerView.setAdapter(gridItemAdapter);



        // Set up footer
        Footer.setUpFooter(this);

        // Load the featured image
        ImageView featuredImage = findViewById(R.id.featuredImage);
        loadFeaturedImage(featuredImage);

        // Fetch event list
        getEventList();
    }

    private void loadFeaturedImage(ImageView imageView) {
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                if (!eventList.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(eventList.size());
                    String imageUrl = eventList.get(randomIndex).getImgUrl();
                    Glide.with(homepage.this)
                            .load(imageUrl)
                            .into(imageView);
                }
            }
        });
    }

    private void getEventList() {
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                if (eventList != null && !eventList.isEmpty()) {
                    runOnUiThread(() -> {
                        horizontalItemAdapter.setSearchList(eventList);
                        gridItemAdapter.setSearchList(eventList);
                    });
                }
            }
        });
    }
}
