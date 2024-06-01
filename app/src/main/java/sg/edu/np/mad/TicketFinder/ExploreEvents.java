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
    private boolean searchByArtist = false;
    private EventAdapter mAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Event> eventList = new ArrayList<>();

    private String selectedDate = null;
    private String selectedEventType = null;
    private String selectedPriceRange = null;

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

        // gets restored filter options
        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("selectedDate");
            selectedEventType = savedInstanceState.getString("selectedEventType");
            selectedPriceRange = savedInstanceState.getString("selectedPriceRange");
        }

        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> retrievedEventList) {
                // get event data
                eventList.addAll(retrievedEventList);
                mAdapter.notifyDataSetChanged();
                applyFilters(); // Apply filters if they were restored
            }
        });

        setupRecyclerView(); // shows events
        setupSearchToggle(); // set search by artist/title
        setupSearchBar();
        setupFilterButton();
        //set up footer
        Footer.setUpFooter(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // saves filter data for restoration
        super.onSaveInstanceState(outState);
        outState.putString("selectedDate", selectedDate);
        outState.putString("selectedEventType", selectedEventType);
        outState.putString("selectedPriceRange", selectedPriceRange);
    }

    private void setupRecyclerView() {
        //display events in recycler view
        recyclerView = findViewById(R.id.exploreView);
        mAdapter = new EventAdapter(ExploreEvents.this, eventList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        // sets layout according to horizontal/vertical orientation
        setRecyclerViewLayoutManager(getResources().getConfiguration().orientation);
    }

    private void setRecyclerViewLayoutManager(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // if horizontal, set 2 columns
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            // if vertical, 1 column
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // sets layout according to new orientation
        setRecyclerViewLayoutManager(newConfig.orientation);
    }

    private void setupSearchToggle() {
        //get toggle button
        Button searchToggle = findViewById(R.id.searchToggle);
        searchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchByArtist) {
                    // now searching by artist
                    searchByArtist = true;
                    searchToggle.setText("Search Artist");
                    Toast.makeText(ExploreEvents.this, "Now searching by artist...", Toast.LENGTH_SHORT).show();
                } else {
                    // now searching by title
                    searchByArtist = false;
                    searchToggle.setText("Search Title");
                    Toast.makeText(ExploreEvents.this, "Now searching by title...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSearchBar() {
        // get search bar
        SearchView searchEvents = findViewById(R.id.searchEvents);

        // dont enter typing mode
        searchEvents.clearFocus();

        searchEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // if nothing is typed, show all events
                if (newText.isEmpty()) {
                    mAdapter.setSearchList(eventList);
                    return true;
                }

                ArrayList<Event> searchList = new ArrayList<>();

                for (Event event : eventList) {
                    if (searchByArtist) {
                        // if event artist contains current search text, add to search list
                        if (event.getArtist().toLowerCase().contains(newText.toLowerCase())) {
                            searchList.add(event);
                        }
                    } else {
                        // if event title contains current search text, add to search list
                        if (event.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                            searchList.add(event);
                        }
                    }
                }

                // if there are no artists/titles matching the search text, alert user
                if (searchList.isEmpty()) {
                    Toast.makeText(ExploreEvents.this, "No events found", Toast.LENGTH_SHORT).show();
                }

                // show all events in search list (if empty, shows nothing)
                mAdapter.setSearchList(searchList);

                return true;
            }
        });
    }

    private void setupFilterButton() {
        // get filter button
        ImageButton filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            } // opens filter menu
        });
    }


    // -------------------------------------------------------------------------- filtering
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_options, null);

        // get views
        Spinner priceRangeSpinner = view.findViewById(R.id.priceRangeSpinner);
        Spinner eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner);
        Button selectDateButton = view.findViewById(R.id.selectDateButton);
        Button clearDateButton = view.findViewById(R.id.clearDateButton);
        TextView selectedDateTextView = view.findViewById(R.id.selectedDateTextView);

        // Use the existing getData method to populate the filters dynamically
        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> events) {
                setupPriceRangeSpinner(priceRangeSpinner, events);
                setupEventTypeSpinner(eventTypeSpinner, events);


                // Restore filter selections
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
                showDatePickerDialog(selectedDateTextView);
            }
        });

        clearDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedDate(selectedDateTextView);
            }
        });

        // apply / cancel function
        builder.setView(view)
                .setTitle("Apply Filters")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //also any existing price range/event type filters
                        selectedPriceRange = priceRangeSpinner.getSelectedItem().toString();
                        selectedEventType = eventTypeSpinner.getSelectedItem().toString();
                        applyFilters();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // return date filtered list
    private ArrayList<Event> filterByDate(ArrayList<Event> events, String date) {
        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event event : events) {
            if (event.getDate().toString().equals(date)) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }
    private void showDatePickerDialog(TextView selectedDateTextView) {
        // display calendar picker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ExploreEvents.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // setting date formatting
                        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                        // displays selected date
                        selectedDateTextView.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void clearSelectedDate(TextView selectedDateTextView) {
        selectedDate = null; // or ""
        selectedDateTextView.setText("No date selected"); // Clear the TextView
    }
    // ------------------------------------------------------ end of filter for date

    // ---------------------------------------------------- filter for event type
    private void setupEventTypeSpinner(Spinner spinner, ArrayList<Event> events) {
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add("Any"); // Add "Any" option first
        for (Event event : events) {
            eventTypes.add(event.getGenre()); // add all genres found in eventlist
        }
        List<String> eventTypeList = new ArrayList<>(eventTypes);
        setupSpinner(spinner, eventTypeList); // display all genre options in spinner

        // Set default selection to "Any"
        int defaultPosition = eventTypeList.indexOf("Any");
        spinner.setSelection(defaultPosition);
    }

    // return genre filtered list
    private ArrayList<Event> filterByEventType(ArrayList<Event> events, String eventType) {
        ArrayList<Event> filteredList = new ArrayList<>();
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
        // go through all events and add any relevant price ranges
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
        setupSpinner(spinner, new ArrayList<>(priceRanges)); // display all price range options
    }
    private ArrayList<Event> filterByPriceRange(ArrayList<Event> events, String priceRange) {
        ArrayList<Event> filteredList = new ArrayList<>();
        String[] ranges = priceRange.split(" - ");
        int minPrice = Integer.parseInt(ranges[0].replace("$", "").trim());
        int maxPrice = ranges.length > 1 ? Integer.parseInt(ranges[1].replace("$", "").trim()) : Integer.MAX_VALUE;

        for (Event event : events) {
            if (event.getPrice() >= minPrice && event.getPrice() <= maxPrice) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }
    // ------------------------------------------------------------ end of filter for price

    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private void applyFilters() {
        ArrayList<Event> filteredList = new ArrayList<>(eventList);

        if (selectedDate != null) {
            filteredList = filterByDate(filteredList, selectedDate);
        }
        if (selectedEventType != null && !"Any".equals(selectedEventType)) {
            filteredList = filterByEventType(filteredList, selectedEventType);
        }
        if (selectedPriceRange != null && !"Any".equals(selectedPriceRange)) {
            filteredList = filterByPriceRange(filteredList, selectedPriceRange);
        }

        mAdapter.setSearchList(filteredList);
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No events found with the selected filters", Toast.LENGTH_SHORT).show();
        }
    }
}
