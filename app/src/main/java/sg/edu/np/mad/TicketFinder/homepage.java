//package sg.edu.np.mad.TicketFinder;
//
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.widget.ImageView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Random;
//
//public class homepage extends AppCompatActivity {
//
//    private RecyclerView horizontalRecyclerView;
//    private EventAdapter horizontalItemAdapter;
//    private RecyclerView gridRecyclerView;
//    private EventAdapter gridItemAdapter;
//    private dbHandler handler = new dbHandler();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_homepage);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        // Initialize horizontal RecyclerView
//        horizontalRecyclerView = findViewById(R.id.horizontalRecyclerView);
//        horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        horizontalItemAdapter = new EventAdapter(homepage.this, new ArrayList<>(), false); // Pass false for horizontal layout
//        horizontalRecyclerView.setAdapter(horizontalItemAdapter);
//
//        // Initialize grid RecyclerView
//        gridRecyclerView = findViewById(R.id.gridRecyclerView);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getSpanCount());
//        gridRecyclerView.setLayoutManager(gridLayoutManager);
//        gridItemAdapter = new EventAdapter(homepage.this, new ArrayList<>(), true); // Pass true for grid layout
//        gridRecyclerView.setAdapter(gridItemAdapter);
//
//
//
//        // Set up footer
//        Footer.setUpFooter(this);
//
//        // Load the featured image
//        ImageView featuredImage = findViewById(R.id.featuredImage);
//        loadFeaturedImage(featuredImage);
//
//        // Fetch event list
//        getEventList();
//    }
//
//    // Method to load a random featured image from the event list
//    private void loadFeaturedImage(ImageView imageView) {
//        handler.getData(new FirestoreCallback<Event>() {
//            @Override
//            public void onCallback(ArrayList<Event> eventList) {
//                if (!eventList.isEmpty()) {
//
//                    // random so that featured image changes every time it loads
//                    Random random = new Random();
//                    int randomIndex = random.nextInt(eventList.size());
//
//                    String imageUrl = eventList.get(randomIndex).getImgUrl();
//                    Glide.with(homepage.this)
//                            .load(imageUrl)          // Load the image from the URL
//                            .into(imageView);        // Set the image into the provided ImageView
//                }
//            }
//        });
//    }
//
//    // Method to fetch the event list and update the RecyclerViews
//    private void getEventList() {
//        handler.getData(new FirestoreCallback<Event>() {
//            @Override
//            public void onCallback(ArrayList<Event> eventList) {
//                if (eventList != null && !eventList.isEmpty()) {
//                    // Sort events by date
//                    Collections.sort(eventList, new Comparator<Event>() {
//                        @Override
//                        public int compare(Event event1, Event event2) {
//                            return event1.getDate().compareTo(event2.getDate());
//                        }
//                    });
//
//                    // Filter out top 3 upcoming events
//                    List<Event> topEvents = eventList.subList(0, Math.min(eventList.size(), 3));
//
//                    // Convert List to ArrayList cos need to ensure compatibility
//                    // with the setSearchList() method of the EventAdapter
//                    ArrayList<Event> topEventsArrayList = new ArrayList<>(topEvents);
//
//                    runOnUiThread(() -> {
//                        horizontalItemAdapter.setSearchList(topEventsArrayList);
//
//                        // Exclude the first 3 items from eventList
//                        List<Event> remainingEvents = eventList.subList(Math.min(3, eventList.size()), eventList.size());
//                        gridItemAdapter.setSearchList(new ArrayList<>(remainingEvents)); // add the remaining events to the grid
//
//                    });
//                }
//            }
//        });
//    }
//
//    // Method to get the number of columns for the grid layout based on orientation
//    private int getSpanCount() {
//        int orientation = getResources().getConfiguration().orientation;
//        return (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 4 : 2;
//    }
//
//    // Update the grid layout span count when the configuration changes
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        // Retrieve the current layout manager of the grid RecyclerView
//        GridLayoutManager layoutManager = (GridLayoutManager) gridRecyclerView.getLayoutManager();
//        if (layoutManager != null) {
//            // Update the span count of the grid layout manager based on the new config
//            layoutManager.setSpanCount(getSpanCount());
//        }
//    }
//}


package sg.edu.np.mad.TicketFinder;

import android.content.res.Configuration;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

        // Initialize horizontal RecyclerView for "Top 3 Upcoming Events" //changed
        horizontalRecyclerView = findViewById(R.id.horizontalRecyclerView);
        horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horizontalItemAdapter = new EventAdapter(homepage.this, new ArrayList<>(), true); // Pass true for grid layout //changed
        horizontalRecyclerView.setAdapter(horizontalItemAdapter);

        // Initialize grid RecyclerView for "Recommended for You" //changed
        gridRecyclerView = findViewById(R.id.gridRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getSpanCount());
        gridRecyclerView.setLayoutManager(gridLayoutManager);
        gridItemAdapter = new EventAdapter(homepage.this, new ArrayList<>(), false); // Pass false for list layout //changed
        gridRecyclerView.setAdapter(gridItemAdapter);



        // Set up footer
        Footer.setUpFooter(this);

        // Load the featured image
        ImageView featuredImage = findViewById(R.id.featuredImage);
        loadFeaturedImage(featuredImage);

        // Fetch event list
        getEventList();
    }

    // Method to load a random featured image from the event list
    private void loadFeaturedImage(ImageView imageView) {
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                if (!eventList.isEmpty()) {

                    // random so that featured image changes every time it loads
                    Random random = new Random();
                    int randomIndex = random.nextInt(eventList.size());

                    String imageUrl = eventList.get(randomIndex).getImgUrl();
                    Glide.with(homepage.this)
                            .load(imageUrl)          // Load the image from the URL
                            .into(imageView);        // Set the image into the provided ImageView
                }
            }
        });
    }

    // Method to fetch the event list and update the RecyclerViews
    private void getEventList() {
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                if (eventList != null && !eventList.isEmpty()) {
                    // Sort events by date
                    Collections.sort(eventList, new Comparator<Event>() {
                        @Override
                        public int compare(Event event1, Event event2) {
                            return event1.getDate().compareTo(event2.getDate());
                        }
                    });

                    // Filter out top 3 upcoming events
                    List<Event> topEvents = eventList.subList(0, Math.min(eventList.size(), 3));

                    // Convert List to ArrayList cos need to ensure compatibility
                    // with the setSearchList() method of the EventAdapter
                    ArrayList<Event> topEventsArrayList = new ArrayList<>(topEvents);

                    runOnUiThread(() -> {
                        horizontalItemAdapter.setSearchList(topEventsArrayList); //changed

                        // Exclude the first 3 items from eventList
                        List<Event> remainingEvents = eventList.subList(Math.min(3, eventList.size()), eventList.size());
                        gridItemAdapter.setSearchList(new ArrayList<>(remainingEvents)); //changed
                    });
                }
            }
        });
    }

    // Method to get the number of columns for the grid layout based on orientation
    private int getSpanCount() {
//        int orientation = getResources().getConfiguration().orientation;
//        return (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 4 : 2;
        return 1;
    }

    // Update the grid layout span count when the configuration changes
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Retrieve the current layout manager of the grid RecyclerView
        GridLayoutManager layoutManager = (GridLayoutManager) gridRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            // Update the span count of the grid layout manager based on the new config
            layoutManager.setSpanCount(getSpanCount());
        }
    }
}
