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

    // blank recycler view
    ArrayList<Event> noEvents = new ArrayList<>();

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

        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("selectedDate");
            selectedEventType = savedInstanceState.getString("selectedEventType");
            selectedPriceRange = savedInstanceState.getString("selectedPriceRange");
        }

        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> retrievedEventList) {
                eventList.addAll(retrievedEventList);
                mAdapter.notifyDataSetChanged();
                applyFilters(); // Apply filters if they were restored
            }
        });

        setupRecyclerView();
        setupSearchToggle();
        setupSearchBar();
        setupFilterButton();
        Footer.setUpFooter(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selectedDate", selectedDate);
        outState.putString("selectedEventType", selectedEventType);
        outState.putString("selectedPriceRange", selectedPriceRange);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.exploreView);
        mAdapter = new EventAdapter(ExploreEvents.this, noEvents);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        setRecyclerViewLayoutManager(getResources().getConfiguration().orientation);
    }

    private void setRecyclerViewLayoutManager(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns for landscape mode
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 1 column for portrait mode
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRecyclerViewLayoutManager(newConfig.orientation);
    }

    private void setupSearchToggle() {
        Button searchToggle = findViewById(R.id.searchToggle);
        searchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        SearchView searchEvents = findViewById(R.id.searchEvents);
        searchEvents.clearFocus();
        searchEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mAdapter.clear();
                    return true;
                }

                ArrayList<Event> searchList = new ArrayList<>();

                for (Event event : eventList) {
                    if (searchByArtist) {
                        if (event.getArtist().toLowerCase().contains(newText.toLowerCase())) {
                            searchList.add(event);
                        }
                    } else {
                        if (event.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                            searchList.add(event);
                        }
                    }
                }

                if (searchList.isEmpty()) {
                    Toast.makeText(ExploreEvents.this, "No events found", Toast.LENGTH_SHORT).show();
                }
                mAdapter.setSearchList(searchList);

                return true;
            }
        });
    }

    private void setupFilterButton() {
        ImageButton filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }


    // -------------------------------------------------------------------------- filtering
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_options, null);

        Spinner priceRangeSpinner = view.findViewById(R.id.priceRangeSpinner);
        Spinner eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner);
        Button selectDateButton = view.findViewById(R.id.selectDateButton);
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

        builder.setView(view)
                .setTitle("Apply Filters")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPriceRange = priceRangeSpinner.getSelectedItem().toString();
                        selectedEventType = eventTypeSpinner.getSelectedItem().toString();
                        applyFilters();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
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
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ExploreEvents.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                        selectedDateTextView.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
    // ------------------------------------------------------ end of filter for date

    // ---------------------------------------------------- filter for event type
    private void setupEventTypeSpinner(Spinner spinner, ArrayList<Event> events) {
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add("Any"); // Add "Any" option first
        for (Event event : events) {
            eventTypes.add(event.getGenre());
        }
        List<String> eventTypeList = new ArrayList<>(eventTypes);
        setupSpinner(spinner, eventTypeList);

        // Set default selection to "Any"
        int defaultPosition = eventTypeList.indexOf("Any");
        spinner.setSelection(defaultPosition);
    }
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
            } else {
                priceRanges.add("$201+");
            }
        }
        setupSpinner(spinner, new ArrayList<>(priceRanges));
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
