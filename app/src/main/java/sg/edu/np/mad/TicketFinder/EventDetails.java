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

        // test
        String inputDate = eventObj.getDate(); // example input

        // Define the date formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse the input date string to LocalDate
        LocalDate date = LocalDate.parse(inputDate, formatter);

        // Subtract one month from the date
        LocalDate newDate = date.minusMonths(1);

        // Format the new date back to a string
        String outputDate = newDate.format(formatter);


        // setting text
        title.setText(eventObj.getTitle());
        caption.setText(eventObj.getCaption());
        artist.setText(eventObj.getArtist());
        genre.setText(eventObj.getGenre());
        timing.setText(eventObj.getDate() + ", " + eventObj.getTime());
        venue.setText(eventObj.getVenue());
        description.setText(eventObj.getDescription());
        price.setText("$" + String.format("%.2f",eventObj.getPrice())); // set price to string with 2dp
        salesTiming.setText(outputDate);


        // show map button
        Button showMapButton = findViewById(R.id.showMapButton);
        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create and display a dialog
                Dialog mapDialog = new Dialog(EventDetails.this);
                mapDialog.setContentView(R.layout.map_dialog);
                ImageView mapImage = mapDialog.findViewById(R.id.mapImageView);
                mapImage.setImageResource(R.drawable.seat_map);
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