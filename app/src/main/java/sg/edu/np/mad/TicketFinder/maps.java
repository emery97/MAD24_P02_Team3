package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class maps extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    FrameLayout mapContainer;
    Event eventObj;

    Address venueAddress;

    private static final String TAG = "maps";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String API_KEY = "AIzaSyAtrYJH3VUAJgo-qhxicKkjihd8pPSuEII";

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

        Button gMapButton = findViewById(R.id.gMapButton);
        gMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venueAddress != null) {
                    double latitude = venueAddress.getLatitude();
                    double longitude = venueAddress.getLongitude();
                    String uri = "google.navigation:q=" + latitude + "," + longitude;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Log.e("maps", "Google Maps app is not installed");
                    }
                } else {
                    Log.e("maps", "Venue address is null");
                }
            }
        });
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

                // Get place ID
                getPlaceIdFromCoordinates(venueAddress.getLatitude(), venueAddress.getLongitude());
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
    private void getPlaceIdFromCoordinates(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeocodingService service = retrofit.create(GeocodingService.class);
        String latlng = latitude + "," + longitude;

        Call<GeocodingResponse> call = service.getReverseGeocodingData(latlng, API_KEY);
        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    GeocodingResponse geocodingResponse = response.body();
                    if (geocodingResponse != null && !geocodingResponse.results.isEmpty()) {
                        GeocodingResponse.Result result = geocodingResponse.results.get(0);
                        String placeId = result.place_id;
                        Log.d(TAG, "Place ID: " + placeId);
                    }
                } else {
                    Log.e(TAG, "Request failed with status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage());
            }
        });
    }
}
