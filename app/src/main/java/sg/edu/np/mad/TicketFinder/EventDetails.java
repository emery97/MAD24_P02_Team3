package sg.edu.np.mad.TicketFinder;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        Intent intent = getIntent();
        Event eventObj = (Event) getIntent().getSerializableExtra("event");
        Log.i("event", eventObj.getTitle()); // testing

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