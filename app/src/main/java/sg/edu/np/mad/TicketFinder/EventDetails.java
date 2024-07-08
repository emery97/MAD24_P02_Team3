package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

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

        final Event[] eventForTitle = new Event[1];
        try {
            Intent intent = getIntent();
            Event eventObj = (Event) intent.getSerializableExtra("event");
            eventForTitle[0] = eventObj;

            Log.i("event", eventObj.toString());

            TextView title = findViewById(R.id.eventTitle);
            TextView caption = findViewById(R.id.eventCaption);
            TextView artist = findViewById(R.id.eventArtist);
            TextView genre = findViewById(R.id.eventGenre);
            TextView timing = findViewById(R.id.eventTiming);
            TextView venue = findViewById(R.id.eventVenue);
            TextView description = findViewById(R.id.eventDescription);
            TextView price = findViewById(R.id.eventTicketPrice);
            TextView salesTiming = findViewById(R.id.eventGeneralSales);
            ImageView eventImg = findViewById(R.id.eventImg);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

            String eventDateString = eventObj.getDate().format(formatter);

            LocalDate salesDate = eventObj.getDate().minusMonths(1);
            String salesDateString = salesDate.format(formatter);

            Glide.with(this)
                    .load(eventObj.getImgUrl())
                    .into(eventImg);

            title.setText(eventObj.getTitle());
            caption.setText(eventObj.getCaption());
            artist.setText(eventObj.getArtist());
            genre.setText(eventObj.getGenre());
            timing.setText(eventDateString + ", " + eventObj.getTime());
            venue.setText(eventObj.getVenue());
            description.setText(eventObj.getDescription());
            price.setText("$" + String.format("%.2f", eventObj.getPrice()));
            salesTiming.setText(salesDateString + ", " + eventObj.getTime());

        } catch (Exception e) {
            Log.e("EDIntent", "no intent passed in");
        }

        //show venue details
        Button showVenueMapButton = findViewById(R.id.showVenueMap);
        showVenueMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventDetails.this, maps.class);
                intent.putExtra("event", eventForTitle[0]); // sending event data
                startActivity(intent);
            }
        });

        //show seat map
        Button showMapButton = findViewById(R.id.showMapButton);

        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog mapDialog = new Dialog(EventDetails.this);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mapDialog.setContentView(R.layout.horizontal_map_layout);
                    mapDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    mapDialog.setContentView(R.layout.map_dialog);
                }

                PhotoView mapImage = mapDialog.findViewById(R.id.mapImageView);
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

        Button buyTicketsButton = findViewById(R.id.buyTicketsButton);
        buyTicketsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetails.this, BuyTicket.class);

                if (eventForTitle[0] != null) {
                    intent.putExtra("event", eventForTitle[0]); // Pass the Event object
                    intent.putExtra("eventTitle", eventForTitle[0].getTitle());
                    intent.putExtra("eventTiming", eventForTitle[0].getDate().format(DateTimeFormatter.ofPattern("dd LLLL yyyy")) + ", " + eventForTitle[0].getTime());
                } else {
                    Log.d("buyTicketsButton", "eventObj is null");
                }

                startActivity(intent);
            }
        });

        // Set up the chatbot button click listener
        findViewById(R.id.chatbotButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetails.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        Footer.setUpFooter(this);
    }
}
