package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get event object
        Intent intent = getIntent();
        Event eventObj = (Event) getIntent().getSerializableExtra("event");
        Log.i("event", eventObj.toString()); // testing

        // initialise all views
        TextView title = findViewById(R.id.eventTitle);
        TextView caption = findViewById(R.id.eventCaption);
        TextView artist = findViewById(R.id.eventArtist);
        TextView genre = findViewById(R.id.eventGenre);
        TextView timing = findViewById(R.id.eventTiming);
        TextView venue = findViewById(R.id.eventVenue);
        TextView description = findViewById(R.id.eventDescription);
        TextView price = findViewById(R.id.eventTicketPrice);
        TextView salesTiming = findViewById(R.id.eventGeneralSales);

        // date formatter (eg. 30 October 2023)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

        // format eventdate
        String eventDateString = eventObj.getDate().format(formatter);

        // get sales period (subtract 1 month from eventDate)
        LocalDate salesDate = eventObj.getDate().minusMonths(1);
        // format sales period date to string
        String salesDateString = salesDate.format(formatter);


        // setting text
        title.setText(eventObj.getTitle());
        caption.setText(eventObj.getCaption());
        artist.setText(eventObj.getArtist());
        genre.setText(eventObj.getGenre());
        timing.setText(eventDateString + ", " + eventObj.getTime());
        venue.setText(eventObj.getVenue());
        description.setText(eventObj.getDescription());
        price.setText("$" + String.format("%.2f",eventObj.getPrice())); // set price to string with 2dp
        salesTiming.setText(salesDateString + ", " + eventObj.getTime()); // sales timing is 1 month before, same time


        // show map button
        Button showMapButton = findViewById(R.id.showMapButton);
        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create and display a dialog
                Dialog mapDialog = new Dialog(EventDetails.this);
                mapDialog.setContentView(R.layout.map_dialog);
                ImageView mapImage = mapDialog.findViewById(R.id.mapImageView);
                mapImage.setImageResource(R.drawable.seat_map_with_color_legend);
                Button closeButton = mapDialog.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mapDialog.dismiss();
                    }
                });
                mapDialog.show();
            }
        });
        Footer.setUpFooter(this);
    }
}