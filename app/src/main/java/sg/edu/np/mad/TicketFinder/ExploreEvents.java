package sg.edu.np.mad.TicketFinder;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.SearchView;
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
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class ExploreEvents extends AppCompatActivity {
    private boolean searchByArtist = false;
    private EventAdapter mAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Event> eventList = new ArrayList<>();

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

        dbHandler handler = new dbHandler();
        handler.getData(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Event> retrievedEventList) {
                eventList.addAll(retrievedEventList);
                mAdapter.notifyDataSetChanged();
            }
        });

        //set recycler view, show blank
        recyclerView = findViewById(R.id.exploreView);
        mAdapter = new EventAdapter(ExploreEvents.this, noEvents);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        setLayoutManager(getResources().getConfiguration().orientation); // change based on phone orientation

        // search
        // toggle search
        Button searchToggle = findViewById(R.id.searchToggle);
        searchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchByArtist){
                    searchByArtist = true;
                    searchToggle.setText("Search Artist");
                    Toast.makeText(ExploreEvents.this, "Now searching by artist...", Toast.LENGTH_SHORT).show();
                } else{
                    searchByArtist = false;
                    searchToggle.setText("Search Title");
                    Toast.makeText(ExploreEvents.this, "Now searching by title...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // searchbar function
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

                for (Event event : eventList){
                    if (searchByArtist) {
                        if (event.getArtist().toLowerCase().contains(newText.toLowerCase())){
                            searchList.add(event);
                        }
                    } else {
                        if (event.getTitle().toLowerCase().contains(newText.toLowerCase())){
                            searchList.add(event);
                        }
                    }
                }

                if (searchList.isEmpty()){
                    Toast.makeText(ExploreEvents.this, "No events found", Toast.LENGTH_SHORT).show();
                }
                mAdapter.setSearchList(searchList);

                return true;
            }
        });

        // ---------------------------------------------- filtering

        Button filterPriceRange = findViewById(R.id.filterPriceRange);
        Button filterDateTime = findViewById(R.id.filterDateTime);
        Button filterEventType = findViewById(R.id.filterEventType);

        // ---------------------------------------------------------
        filterPriceRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPriceRange();
            }
        });

        filterDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        filterEventType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEventTypeDialog();
            }
        });

        // for navbar
        Footer.setUpFooter(this);
    }

    private void setLayoutManager(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns for landscape mode
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 1 column for portrait mode
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutManager(newConfig.orientation);
    }



    // ---------------------------------------------- Date and Time

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ExploreEvents.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                        filterByDateTime(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
    private void filterByDateTime(String date) {
        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event event : eventList) {
            if (event.getDate().toString().equals(date)) {
                filteredList.add(event);
            }
        }
        mAdapter.setSearchList(filteredList);
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No events found on this date", Toast.LENGTH_SHORT).show();
        }
    }
    // ---------------------------------------------- End Of Date and Time



    // ------------------------------------------------ Event Type
    private void showEventTypeDialog() {
        final String[] eventTypes = getUniqueEventTypes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Event Type");
        builder.setItems(eventTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedEventType = eventTypes[which];
                filterByEventType(selectedEventType);
            }
        });

        builder.create().show();
    }

    private String[] getUniqueEventTypes() {
        Set<String> eventTypeSet = new HashSet<>();
        for (Event event : eventList) {
            eventTypeSet.add(event.getGenre());
        }
        return eventTypeSet.toArray(new String[0]);
    }

    private void filterByEventType(String eventType) {
        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event event : eventList) {
            if (event.getGenre().equalsIgnoreCase(eventType)) {
                filteredList.add(event);
            }
        }
        mAdapter.setSearchList(filteredList);
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No events found of this type", Toast.LENGTH_SHORT).show();
        }
    }
    // ------------------------------------------------------- End of Event Type



    // ----------------------------------------------- price range
    private void showPriceRange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Price Range");

        final String[] priceRange = {"$0 - $20", "$21 - $50", "$51 - $100", "$101 - $200", "$201+"};
        builder.setItems(priceRange, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedPriceRange = priceRange[which];
                filterByPriceRange(selectedPriceRange);
            }
        });
        builder.create().show();
    }

    private void filterByPriceRange(String priceRange) {
        ArrayList<Event> filteredList = new ArrayList<>();

        if (priceRange.equals("$201+")) {
            for (Event event : eventList) {
                if (event.getPrice() > 200) {
                    filteredList.add(event);
                }
            }
        } else {
            String[] ranges = priceRange.split(" - ");
            int minPrice = Integer.parseInt(ranges[0].replace("$", "").trim());
            int maxPrice = ranges.length > 1 ? Integer.parseInt(ranges[1].replace("$", "").trim()) : Integer.MAX_VALUE;

            for (Event event : eventList) {
                if (event.getPrice() >= minPrice && event.getPrice() <= maxPrice) {
                    filteredList.add(event);
                }
            }
        }

        mAdapter.setSearchList(filteredList);
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No events found in this price range", Toast.LENGTH_SHORT).show();
        }
    }
    // -------------------------------------------------- End Of Price Range
}