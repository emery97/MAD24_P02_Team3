package sg.edu.np.mad.TicketFinder;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExploreEvents extends AppCompatActivity {
    private boolean searchByArtist = false; // Flag to toggle between searching by artist or title
    private EventAdapter mAdapter;
    private RecyclerView recyclerView; // To display events
    private ArrayList<Event> eventList = new ArrayList<>();

    private String searchText = ""; // To store the current search query

    private String selectedDate = null; // To store the selected date filter
    private String selectedEventType = null; // To store the selected event type filter
    private String selectedPriceRange = null; // To store the selected price range filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Restore filter options if available
        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("selectedDate");
            selectedEventType = savedInstanceState.getString("selectedEventType");
            selectedPriceRange = savedInstanceState.getString("selectedPriceRange");
        }

        // Retrieve event data from database
        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> retrievedEventList) {
                eventList.addAll(retrievedEventList);
                mAdapter.notifyDataSetChanged();
                applyFilters(); // Apply filters if they were restored
            }
        });

        setupRecyclerView(); // Display events
        setupSearchToggle(); // Setup search toggle button for artist/title
        setupSearchBar(); // Setup search bar
        setupFilterButton(); // Setup filter button
        Footer.setUpFooter(this); // Set up footer
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save filter data for restoration
        super.onSaveInstanceState(outState);
        outState.putString("selectedDate", selectedDate);
        outState.putString("selectedEventType", selectedEventType);
        outState.putString("selectedPriceRange", selectedPriceRange);
    }

    private void setupRecyclerView() {
        // Display events in RecyclerView
        recyclerView = findViewById(R.id.exploreView);
        mAdapter = new EventAdapter(ExploreEvents.this, eventList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        // Set layout according to horizontal/vertical orientation
        setRecyclerViewLayoutManager(getResources().getConfiguration().orientation);
    }

    private void setRecyclerViewLayoutManager(int orientation) {
        // Set layout manager based on screen orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Two columns for horizontal
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this)); // One column for vertical
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Adjust layout manager on configuration change
        setRecyclerViewLayoutManager(newConfig.orientation);
    }

    private void setupSearchToggle() {
        // Initialize search toggle button
        Button searchToggle = findViewById(R.id.searchToggle);
        searchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle between searching by artist or title
                if (!searchByArtist) {
                    searchByArtist = true;
                    searchToggle.setText("Search Artist");
                    Toast.makeText(ExploreEvents.this, "Now searching by artist...", Toast.LENGTH_SHORT).show();
                } else {
                    searchByArtist = false;
                    searchToggle.setText("Search Title");
                    Toast.makeText(ExploreEvents.this, "Now searching by title...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSearchBar() {
        // Initialize search bar
        SearchView searchEvents = findViewById(R.id.searchEvents);
        searchEvents.clearFocus(); // Don't enter typing mode by default

        searchEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Do nothing on text submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update search query and apply filters
                searchText = newText;
                applyFilters();
                return true;
            }
        });
    }

    private void setupFilterButton() {
        // Initialize filter button
        ImageButton filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog(); // Open filter dialog
            }
        });
    }

    // -------------------------------------------------------------------------- filtering
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_options, null);

        // Initialize filter dialog views
        Spinner priceRangeSpinner = view.findViewById(R.id.priceRangeSpinner);
        Spinner eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner);
        Button selectDateButton = view.findViewById(R.id.selectDateButton);
        Button clearDateButton = view.findViewById(R.id.clearDateButton);
        TextView selectedDateTextView = view.findViewById(R.id.selectedDateTextView);

        // Populate filter options dynamically
        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> events) {
                setupPriceRangeSpinner(priceRangeSpinner, events);
                setupEventTypeSpinner(eventTypeSpinner, events);

                // Set any filter selections
                if (selectedPriceRange != null) {
                    int position = ((ArrayAdapter<String>) priceRangeSpinner.getAdapter()).getPosition(selectedPriceRange);
                    priceRangeSpinner.setSelection(position);
                }
                if (selectedEventType != null) {
                    int position = ((ArrayAdapter<String>) eventTypeSpinner.getAdapter()).getPosition(selectedEventType);
                    eventTypeSpinner.setSelection(position);
                }
                if (selectedDate != null) {
                    selectedDateTextView.setText(selectedDate);
                }
            }
        });

        // ---------------------------------------------- filter for date
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(selectedDateTextView); // Show date picker dialog
            }
        });

        clearDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedDate(selectedDateTextView); // Clear selected date
            }
        });

        // Apply/Cancel for filter dialog
        builder.setView(view)
                .setTitle("Apply Filters")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Store selected filter options and apply filters
                        selectedPriceRange = priceRangeSpinner.getSelectedItem().toString();
                        selectedEventType = eventTypeSpinner.getSelectedItem().toString();
                        applyFilters();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Return date filtered list
    private ArrayList<Event> filterByDate(ArrayList<Event> events, String date) {
        ArrayList<Event> filteredList = new ArrayList<>();

        // Filter for matching events
        for (Event event : events) {
            if (event.getDate().toString().equals(date)) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }

    private void showDatePickerDialog(TextView selectedDateTextView) {
        // Display calendar picker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ExploreEvents.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Format selected date and display it
                        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02 d",
                                year, month + 1, dayOfMonth);
                                selectedDateTextView.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void clearSelectedDate(TextView selectedDateTextView) {
        selectedDate = null; // Clear selected date
        selectedDateTextView.setText("No date selected"); // Update TextView
    }
    // ------------------------------------------------------ end of filter for date

    // ---------------------------------------------------- filter for event type
    private void setupEventTypeSpinner(Spinner spinner, ArrayList<Event> events) {
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add("Any"); // Add "Any" option first
        for (Event event : events) {
            eventTypes.add(event.getGenre()); // Add all genres found in event list
        }
        List<String> eventTypeList = new ArrayList<>(eventTypes);
        setupSpinner(spinner, eventTypeList); // Display all genre options in spinner

        // Set default selection to "Any"
        int defaultPosition = eventTypeList.indexOf("Any");
        spinner.setSelection(defaultPosition);
    }

    // Return genre filtered list
    private ArrayList<Event> filterByEventType(ArrayList<Event> events, String eventType) {
        ArrayList<Event> filteredList = new ArrayList<>();

        // Filter for matching events
        for (Event event : events) {
            if (event.getGenre().equalsIgnoreCase(eventType)) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }
    // ----------------------------------------------------  end of filter for event type

    // ------------------------------------------------------------ filter for price
    private void setupPriceRangeSpinner(Spinner spinner, ArrayList<Event> events) {
        Set<String> priceRanges = new HashSet<>();
        priceRanges.add("Any"); // Add "Any" option
        // Go through all events and add any relevant price ranges
        for (Event event : events) {
            double price = event.getPrice();
            if (price <= 20) {
                priceRanges.add("$0 - $20");
            } else if (price <= 50) {
                priceRanges.add("$21 - $50");
            } else if (price <= 100) {
                priceRanges.add("$51 - $100");
            } else if (price <= 200) {
                priceRanges.add("$101 - $200");
            }
        }
        setupSpinner(spinner, new ArrayList<>(priceRanges)); // Display all price range options
    }

    // Return price range filtered list
    private ArrayList<Event> filterByPriceRange(ArrayList<Event> events, String priceRange) {
        ArrayList<Event> filteredList = new ArrayList<>();
        // Get the max and min values from price range, and store in array
        String[] ranges = priceRange.split(" - ");

        // Get values from the array, and parse to int
        int minPrice = Integer.parseInt(ranges[0].replace("$", "").trim());
        int maxPrice = ranges.length > 1 ? Integer.parseInt(ranges[1].replace("$", "").trim()) : Integer.MAX_VALUE;

        // Filter for matching events
        for (Event event : events) {
            if (event.getPrice() >= minPrice && event.getPrice() <= maxPrice) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }
    // ------------------------------------------------------------ end of filter for price

    private void setupSpinner(Spinner spinner, List<String> items) {
        // Initialize spinner with given items
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void applyFilters() {
        // Apply selected filters to the event list
        ArrayList<Event> filteredList = new ArrayList<>(eventList);

        // Filters
        if (selectedDate != null) {
            filteredList = filterByDate(filteredList, selectedDate);
        }
        if (selectedEventType != null && !"Any".equals(selectedEventType)) {
            filteredList = filterByEventType(filteredList, selectedEventType);
        }
        if (selectedPriceRange != null && !"Any".equals(selectedPriceRange)) {
            filteredList = filterByPriceRange(filteredList, selectedPriceRange);
        }

        // Search bar
        if (!searchText.isEmpty()) {
            filteredList = applySearch(filteredList, searchText);
        }

        // Display filtered events
        mAdapter.setSearchList(filteredList);
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No events found with the selected filters", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<Event> applySearch(ArrayList<Event> events, String query) {
        // Apply search query to the event list
        ArrayList<Event> searchList = new ArrayList<>();
        for (Event event : events) {
            if (searchByArtist) {
                // If event artist contains current search text, add to search list
                if (event.getArtist().toLowerCase().contains(query.toLowerCase())) {
                    searchList.add(event);
                }
            } else {
                // If event title contains current search text, add to search list
                if (event.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    searchList.add(event);
                }
            }
        }
        return searchList;
    }
}