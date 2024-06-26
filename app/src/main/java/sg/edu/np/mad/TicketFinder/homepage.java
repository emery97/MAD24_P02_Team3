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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class homepage extends AppCompatActivity {

    private RecyclerView horizontalRecyclerView;
    private EventAdapter horizontalItemAdapter;
    private RecyclerView gridRecyclerView;
    private EventAdapter gridItemAdapter;
    private dbHandler handler = new dbHandler();
    private Spinner venueSpinner;

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

        // Initialize Spinner
        venueSpinner = findViewById(R.id.venueSpinner);

        // Fetch and set venue data
        handler.getVenues(new FirestoreCallback<String>() {
            @Override
            public void onCallback(ArrayList<String> venueList) {
                // Ensure "Singapore" is the first item in the venue list
                if (!venueList.contains("Singapore")) {
                    venueList.add(0, "Singapore");
                } else {
                    venueList.remove("Singapore");
                    venueList.add(0, "Singapore");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(homepage.this, android.R.layout.simple_spinner_item, venueList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                venueSpinner.setAdapter(adapter);
            }
        });

        // Set OnItemSelectedListener to Spinner
        venueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVenue = parent.getItemAtPosition(position).toString();
                fetchEventsForVenue(selectedVenue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
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
//        ImageView featuredImage = findViewById(R.id.featuredImage);
//        loadFeaturedImage(featuredImage);

        // Fetch event list
        getEventList();
    }

    // Method to load a random featured image from the event list
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

                    // Filter out top 3 upcoming events closest to today
                    ArrayList<Event> topEventsArrayList = new ArrayList<>();
                    for (Event event : eventList) {
                        if (topEventsArrayList.size() < 3) {
                            topEventsArrayList.add(event);
                        } else {
                            break;
                        }
                    }

                    runOnUiThread(() -> {
                        // Update the "Upcoming Events" RecyclerView
                        horizontalItemAdapter.setSearchList(topEventsArrayList);

                        // Exclude the first 3 items from eventList for "Recommended for You"
                        List<Event> remainingEvents = eventList.subList(Math.min(3, eventList.size()), eventList.size());
                        gridItemAdapter.setSearchList(new ArrayList<>(remainingEvents));
                    });
                }
            }
        });
    }




    // Method to fetch events for the selected venue and update the RecyclerViews
    private void fetchEventsForVenue(String venue) {
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {
                if (eventList != null && !eventList.isEmpty()) {
                    ArrayList<Event> filteredEvents;
                    if (venue.equals("Singapore")) {
                        // Show all events if "Singapore" is selected
                        filteredEvents = new ArrayList<>(eventList);
                    } else {
                        // Filter events by selected venue
                        filteredEvents = new ArrayList<>();
                        for (Event event : eventList) {
                            if (event.getVenue().equals(venue)) {
                                filteredEvents.add(event);
                            }
                        }
                    }

                    // Filter out past events
                    ArrayList<Event> upcomingEvents = new ArrayList<>();
                    LocalDate currentDate = LocalDate.now(); // Get the current date

                    for (Event event : filteredEvents) {
                        if (!event.getDate().isBefore(currentDate)) {
                            upcomingEvents.add(event);
                        }
                    }

                    // Sort the filtered events by date
                    Collections.sort(upcomingEvents, new Comparator<Event>() {
                        @Override
                        public int compare(Event event1, Event event2) {
                            return event1.getDate().compareTo(event2.getDate());
                        }
                    });

                    runOnUiThread(() -> {
                        // Get the top 3 upcoming events
                        ArrayList<Event> topEvents = new ArrayList<>(upcomingEvents.subList(0, Math.min(3, upcomingEvents.size())));

                        // Update the "Upcoming Events" RecyclerView
                        horizontalItemAdapter.setSearchList(topEvents);

                        // Exclude the first 3 items from upcomingEvents for "Recommended for You"
                        ArrayList<Event> remainingEvents = new ArrayList<>(upcomingEvents.subList(Math.min(3, upcomingEvents.size()), upcomingEvents.size()));
                        gridItemAdapter.setSearchList(remainingEvents);
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