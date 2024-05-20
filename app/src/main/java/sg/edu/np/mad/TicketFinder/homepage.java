package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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

        // for navbar
        Footer.setUpFooter(this);
    }

    private ArrayList<Event> getRecoList() {
        ArrayList<Event> recoList = new ArrayList<>();
        handler.getData(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Event> eventList) {

                // add the first 5 events from list
                if (eventList.size() > 5) {
                    for (int i = 0; i < 5; i++) {
                        recoList.add(eventList.get(i));
                    }
                } else {
                    recoList.addAll(eventList);
                }

                verticalItemAdapter.notifyDataSetChanged();
            }
        });
        return recoList;
    }

    private ArrayList<Event> getEventList() {
        ArrayList<Event> eventList = new ArrayList<>();
        handler.getData(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Event> retrievedEventList) {
                eventList.addAll(retrievedEventList);

                eventAdapter.notifyDataSetChanged();
            }
        });
        return eventList;
    }
}
