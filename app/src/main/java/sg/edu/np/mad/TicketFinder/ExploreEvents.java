package sg.edu.np.mad.TicketFinder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.util.Random;

public class ExploreEvents extends AppCompatActivity {
    private boolean searchByArtist = false;
    private EventAdapter mAdapter;
    private RecyclerView recyclerView;

    // blank recycler view
    ArrayList<Event> noEvents = new ArrayList<>();
    //temp event list
    ArrayList<Event> tempEvents = new ArrayList<>();

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

        // init temp events
        for (int i = 0; i < 10; i++) {
            Event tempEvent = new Event(R.drawable.img, "Event" + i, new Random().nextDouble(), "Description"+i, "Artist"+i, "Genre"+i, "Venue"+i, "Date"+i, "Time"+i);
            tempEvents.add(tempEvent);
        }

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

                for (Event event : tempEvents){
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
}