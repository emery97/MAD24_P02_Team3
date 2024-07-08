package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class maps extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    FrameLayout mapContainer;
    Event eventObj;

    Address venueAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        eventObj = (Event) intent.getSerializableExtra("event");

        if (eventObj == null) {
            Log.e("maps", "Event object is null");
            return;
        }

        mapContainer = findViewById(R.id.map_container);

        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map_container, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);

        TextView name = findViewById(R.id.venueName);
        name.setText(eventObj.getVenue());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
        LatLng eventLocation = getLocationFromAddress(this, eventObj.getVenue());

        if (eventLocation != null) {
            this.gMap.addMarker(new MarkerOptions().position(eventLocation).title("Event is Here"));
            float zoomLevel = 17.0f; // Set the zoom level (1.0 - 21.0)
            this.gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, zoomLevel));

            if (venueAddress != null) {
                TextView address = findViewById(R.id.venueAddress);

                StringBuilder fullAddress = new StringBuilder();
                for (int i = 0; i <= venueAddress.getMaxAddressLineIndex(); i++) {
                    fullAddress.append(venueAddress.getAddressLine(i));
                    if (i != venueAddress.getMaxAddressLineIndex()) {
                        fullAddress.append("\n");
                    }
                }
                address.setText(fullAddress.toString());
            }
        } else {
            Log.e("maps", "Event location is null");
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null && !address.isEmpty()) {
                venueAddress = address.get(0);
                Log.i("maps", venueAddress.toString());
                p1 = new LatLng(venueAddress.getLatitude(), venueAddress.getLongitude());
                Log.i("maps", p1.toString());
            } else {
                Log.e("maps", "No address found");
            }
        } catch (IOException ex) {
            Log.e("maps", "Geocoder exception", ex);
        }

        return p1;
    }
}
