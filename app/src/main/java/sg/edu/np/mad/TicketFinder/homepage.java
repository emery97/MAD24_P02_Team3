package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class homepage extends AppCompatActivity {

    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private RecyclerView verticalRecyclerView;
    private EventAdapter verticalItemAdapter;

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

        // Horizontal RecyclerView
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eventAdapter = new EventAdapter(homepage.this, getEventList());
        eventRecyclerView.setAdapter(eventAdapter);

        // Vertical RecyclerView
        verticalRecyclerView = findViewById(R.id.verticalRecyclerView);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        verticalItemAdapter = new EventAdapter(homepage.this, getRecoList());
        verticalRecyclerView.setAdapter(verticalItemAdapter);
    }

    private ArrayList<Event> getRecoList() {
        ArrayList<Event> recoList = new ArrayList<>();
        recoList.add(new Event(R.drawable.img, "Event 1", "Artist 1", "Date 1"));
        recoList.add(new Event(R.drawable.img, "Event 2", "Artist 2", "Date 2"));
        recoList.add(new Event(R.drawable.img, "Event 3", "Artist 3", "Date 3"));
        return recoList;
    }

    private ArrayList<Event> getEventList() {
        ArrayList<Event> eventList = new ArrayList<>();
        eventList.add(new Event(R.drawable.featured_img, "Event 1", "Artist 1", "Date 1"));
        eventList.add(new Event(R.drawable.featured_img, "Event 2", "Artist 2", "Date 2"));
        eventList.add(new Event(R.drawable.featured_img, "Event 3", "Artist 3", "Date 3"));
        return eventList;
    }
}
