package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class maps extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    FrameLayout mapContainer;
    Event eventObj;

    Address venueAddress;

    private static final String TAG = "maps";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String API_KEY = "AIzaSyAtrYJH3VUAJgo-qhxicKkjihd8pPSuEII";

    private static final int PROXIMITY_RADIUS = 1500;

    private String selectedPlaceType = "restaurant"; // Default place type
    private List<Marker> placeMarkers = new ArrayList<>();

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

        Button mapTypeButton = findViewById(R.id.mapTypeButton);
        mapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeSelectorDialog();
            }
        });

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

        RadioGroup placeTypeGroup = findViewById(R.id.placeTypeGroup);
        placeTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.restaurantRadioButton) {
                    selectedPlaceType = "restaurant";
                    fetchNearbyPlaces(gMap.getCameraPosition().target);
                } else if (checkedId == R.id.parkingRadioButton) {
                    selectedPlaceType = "parking";
                    fetchNearbyPlaces(gMap.getCameraPosition().target);
                } else if (checkedId == R.id.transitRadioButton) {
                    selectedPlaceType = "transit_station";
                    fetchNearbyPlaces(gMap.getCameraPosition().target);
                } else if (checkedId == R.id.hideRadioButton) {
                    hideNearbyPlaces();
                }
            }
        });

        Footer.setUpFooter(this);
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
                getPlaceId();
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
    private void getPlaceId() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeocodingService service = retrofit.create(GeocodingService.class);

        Call<GeocodingResponse> call2 = service.getGeocodingData(eventObj.getVenue(), API_KEY);
        call2.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    GeocodingResponse geocodingResponse = response.body();
                    if (geocodingResponse != null && !geocodingResponse.results.isEmpty()) {
                        GeocodingResponse.Result result = geocodingResponse.results.get(0);
                        String placeId = result.place_id;
                        Log.d(TAG, "Place ID: " + placeId);

                        // Fetch place details using place ID
                        getPlaceDetails(placeId);
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

    private void getPlaceDetails(String placeId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PlacesService service = retrofit.create(PlacesService.class);

        Call<PlacesResponse> call = service.getPlaceDetails(placeId, API_KEY);
        Log.d(TAG, "Request URL: " + BASE_URL + "place/details/json?place_id=" + placeId + "&key=" + API_KEY);
        call.enqueue(new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                if (response.isSuccessful()) {
                    PlacesResponse placesResponse = response.body();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String jsonResponse = gson.toJson(response.body());

                    Log.d(TAG, "PlacesResponse JSON: " + jsonResponse);

                    if (placesResponse != null && placesResponse.result != null) {
                        updateUIWithPlaceDetails(placesResponse.result);
                    } else {
                        Log.e(TAG, "PlacesResponse or placesResponse.result is null");
                    }
                } else {
                    Log.e(TAG, "Request failed with status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage());
            }
        });
    }

    private void updateUIWithPlaceDetails(PlacesResponse.Result result) {

        if (result.photos != null && !result.photos.isEmpty()) {
            CardView venueImageHolder = findViewById(R.id.venueImageHolder);
            RecyclerView venueImageRecyclerView = findViewById(R.id.venueImageRecyclerView);
            venueImageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            venueImageRecyclerView.setAdapter(new VenueImageAdapter(this, result.photos));
            venueImageHolder.setVisibility(View.VISIBLE);
        }

        if (result.opening_hours != null) {
            CardView openingHoursHolder = findViewById(R.id.openingHoursHolder);
            StringBuilder openingHours = new StringBuilder();
            for (String hour : result.opening_hours.weekday_text) {
                openingHours.append(hour).append("\n");
            }
            TextView openingHoursTextView = findViewById(R.id.openingHours);
            openingHoursTextView.setText(openingHours.toString().trim());
            openingHoursHolder.setVisibility(View.VISIBLE);
        }

        if (result.international_phone_number != null) {
            CardView phoneNumberHolder = findViewById(R.id.phoneNumberHolder);
            TextView phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
            phoneNumberTextView.setText(result.international_phone_number);
            phoneNumberHolder.setVisibility(View.VISIBLE);
        }

        if (result.website != null) {
            CardView websiteHolder = findViewById(R.id.websiteHolder);
            TextView websiteTextView = findViewById(R.id.websiteTextView);
            websiteTextView.setText(result.website);
            websiteHolder.setVisibility(View.VISIBLE);
        }

        TextView wheelchairTextView = findViewById(R.id.wheelchairTextView);
        if (result.wheelchair_accessible_entrance) {
            wheelchairTextView.setText("Yes");
        } else {
            wheelchairTextView.setText("No");
        }

        TextView ratingTextView = findViewById(R.id.ratingTextView);
        ratingTextView.setText(result.rating + "/5");

        TextView totalRatingsTextView = findViewById(R.id.totalRatingsTextView);
        totalRatingsTextView.setText(String.valueOf(result.user_ratings_total));

        if (result.reviews != null && !result.reviews.isEmpty()) {
            CardView reviewsHolder = findViewById(R.id.reviewsHolder);
            RecyclerView recyclerView = findViewById(R.id.reviewRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new VenueReviewAdapter(result.reviews));
            reviewsHolder.setVisibility(View.VISIBLE);
        }
    }

    private void showMapTypeSelectorDialog() {
        final String[] mapTypes = {"Normal", "Satellite", "Terrain", "Hybrid"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Map Type")
                .setItems(mapTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case 1:
                                gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void fetchNearbyPlaces(LatLng location) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PlacesService service = retrofit.create(PlacesService.class);

        // Clear existing markers
        hideNearbyPlaces();

        Call<NearbyPlacesResponse> call = service.getNearbyPlaces(
                selectedPlaceType,
                location.latitude + "," + location.longitude,
                PROXIMITY_RADIUS,
                API_KEY);

        call.enqueue(new Callback<NearbyPlacesResponse>() {
            @Override
            public void onResponse(Call<NearbyPlacesResponse> call, Response<NearbyPlacesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (NearbyPlacesResponse.Result place : response.body().results) {
                        LatLng placeLocation = new LatLng(place.geometry.location.lat, place.geometry.location.lng);
                        Marker marker = gMap.addMarker(new MarkerOptions()
                                .position(placeLocation)
                                .title(place.name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        placeMarkers.add(marker);
                    }

                } else {
                    Log.e(TAG, "Nearby Places request failed with status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NearbyPlacesResponse> call, Throwable t) {
                Log.e(TAG, "Nearby Places request failed: " + t.getMessage());
            }
        });
    }

    private void hideNearbyPlaces() {
        for (Marker marker : placeMarkers) {
            marker.remove();
        }
        placeMarkers.clear();
    }
}
