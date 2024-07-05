package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class maps extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    FrameLayout map;

    Event eventObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        eventObj = (Event) intent.getSerializableExtra("event");

        if (eventObj == null) {
            Log.e("maps", "Event object is null");
            return;
        }

        map = findViewById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("maps", "MapFragment is null");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
        LatLng eventLocation = getLocationFromAddress(this, eventObj.getVenue());

        if (eventLocation != null) {
            this.gMap.addMarker(new MarkerOptions().position(eventLocation).title("Event is Here"));
            this.gMap.moveCamera(CameraUpdateFactory.newLatLng(eventLocation));
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
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i("eventLocation", p1.toString());
            } else {
                Log.e("maps", "No address found");
            }
        } catch (IOException ex) {
            Log.e("maps", "Geocoder exception", ex);
        }

        return p1;
    }
}