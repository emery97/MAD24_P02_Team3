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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class maps extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap; // Google Map instance
    private FrameLayout mapContainer; // Container for the map
    private Event eventObj; // Event object containing details about the event
    private Address venueAddress; // Address object for the venue

    private static final String TAG = "maps";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String API_KEY = "AIzaSyAtrYJH3VUAJgo-qhxicKkjihd8pPSuEII";
    private static final int PROXIMITY_RADIUS = 1500; // Radius to search for nearby places

    private String selectedPlaceType = "restaurant"; // Default place type for nearby search
    private List<Marker> placeMarkers = new ArrayList<>(); // List to hold markers for nearby places

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

        // Retrieve event object from Intent
        Intent intent = getIntent();
        eventObj = (Event) intent.getSerializableExtra("event");

        if (eventObj == null) {
            Log.e(TAG, "Event object is null");
            return;
        }

        // Initialize map container and fragment
        mapContainer = findViewById(R.id.map_container);
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map_container, mapFragment);
        fragmentTransaction.commit();

        // Set up map fragment callback
        mapFragment.getMapAsync(this);

        // Set event venue name
        TextView name = findViewById(R.id.venueName);
        name.setText(eventObj.getVenue());

        // Set up map type button listener
        Button mapTypeButton = findViewById(R.id.mapTypeButton);
        mapTypeButton.setOnClickListener(v -> showMapTypeSelectorDialog());

        // Set up Google Maps navigation button listener
        Button gMapButton = findViewById(R.id.gMapButton);
        gMapButton.setOnClickListener(v -> {
            if (venueAddress != null) { // Check if the venueAddress object is not null before proceeding
                double latitude = venueAddress.getLatitude(); // Get the latitude of the venue
                double longitude = venueAddress.getLongitude(); // Get the longitude of the venue

                // Create a URI to open Google Maps and start navigation to the venue's coordinates
                String uri = "google.navigation:q=" + latitude + "," + longitude;

                // Create an Intent to view the navigation URI
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                // Specify that the Intent should be handled by the Google Maps app
                intent1.setPackage("com.google.android.apps.maps");

                // Check if there is an application that can handle the Intent
                if (intent1.resolveActivity(getPackageManager()) != null) {
                    // Start the activity to open Google Maps and begin navigation
                    startActivity(intent1);
                } else {
                    // Log an error if Google Maps app is not installed on the device
                    Log.e(TAG, "Google Maps app is not installed");
                }
            } else {
                // Log an error if venueAddress is null
                Log.e(TAG, "Venue address is null");
            }
        });

        // Set up place type radio group listener
        RadioGroup placeTypeGroup = findViewById(R.id.placeTypeGroup);
        placeTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Selecting restaurant/parking/transit will call fetchNearbyPlaces
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
                // hide will remove all markers
                hideNearbyPlaces();
            }
        });

        Footer.setUpFooter(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
        LatLng eventLocation = getLocationFromAddress(this, eventObj.getVenue());

        if (eventLocation != null) {
            // Add a marker for the event location and move the camera to it
            this.gMap.addMarker(new MarkerOptions().position(eventLocation).title("Event is Here"));
            float zoomLevel = 17.0f; // Set the zoom level (1.0 - 21.0)
            this.gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, zoomLevel));

            if (venueAddress != null) {
                // Set venue address text
                TextView address = findViewById(R.id.venueAddress);
                // merge all address texts into 1 string
                StringBuilder fullAddress = new StringBuilder();
                for (int i = 0; i <= venueAddress.getMaxAddressLineIndex(); i++) {
                    fullAddress.append(venueAddress.getAddressLine(i));
                    if (i != venueAddress.getMaxAddressLineIndex()) {
                        fullAddress.append("\n");
                    }
                }
                address.setText(fullAddress.toString());

                // Get place ID and set place details
                getPlaceId();
            }
        } else {
            Log.e(TAG, "Event location is null");
        }
    }

    // Convert address string to LatLng object
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        // Create a Geocoder instance to perform geocoding operations
        Geocoder coder = new Geocoder(context);
        List<Address> address; // List to hold the result from the geocoding operation
        LatLng p1 = null; // Initialize LatLng object to null

        try {
            // Perform the geocoding operation to convert the address to geographic coordinates
            address = coder.getFromLocationName(strAddress, 5); // Convert location name to a list of addresses
            if (address != null && !address.isEmpty()) {
                // If the list of addresses is not null and not empty, use the first result
                venueAddress = address.get(0); // Get the first address in the list
                Log.i(TAG, venueAddress.toString()); // Log the address details for debugging
                p1 = new LatLng(venueAddress.getLatitude(), venueAddress.getLongitude()); // Create a LatLng object with the coordinates
                Log.i(TAG, p1.toString()); // Log the LatLng object for debugging
            } else {
                // If no addresses are found, log an error message
                Log.e(TAG, "No address found");
            }
        } catch (IOException ex) {
            // Catch and log any IOExceptions that occur during the geocoding operation
            Log.e(TAG, "Geocoder exception", ex);
        }

        // Return the LatLng object, which contains the latitude and longitude of the address
        return p1;
    }

    // Retrieve place ID for the given venue
    private void getPlaceId() {
        // Create a Retrofit instance using the Builder pattern
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // Set the base URL for the API
                .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter for JSON parsing
                .build(); // Build the Retrofit instance

        // Create an implementation of the GeocodingService interface
        GeocodingService service = retrofit.create(GeocodingService.class);

        // Create a call object to request geocoding data from the API
        Call<GeocodingResponse> call2 = service.getGeocodingData(eventObj.getVenue(), API_KEY);

        // Asynchronously execute the request
        call2.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                // Handle the API response
                if (response.isSuccessful()) {
                    // Check if the response was successful
                    GeocodingResponse geocodingResponse = response.body(); // Get the response body
                    if (geocodingResponse != null && !geocodingResponse.results.isEmpty()) {
                        // Check if the response body and results list are not null or empty
                        GeocodingResponse.Result result = geocodingResponse.results.get(0); // Get the first result
                        String placeId = result.place_id; // Extract the place ID from the result
                        Log.d(TAG, "Place ID: " + placeId); // Log the place ID for debugging

                        // Fetch place details using the extracted place ID
                        getPlaceDetails(placeId);
                    }
                } else {
                    // Log an error if the request was not successful
                    Log.e(TAG, "Request failed with status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                // Handle request failure
                Log.e(TAG, "Request failed: " + t.getMessage()); // Log the error message
            }
        });
    }

    // Retrieve place details for the given place ID
    private void getPlaceDetails(String placeId) {
        // Create a Retrofit instance using the Builder pattern
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // Set the base URL for the API
                .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter for JSON parsing
                .build(); // Build the Retrofit instance

        // Create an implementation of the PlacesService interface
        PlacesService service = retrofit.create(PlacesService.class);

        // Create a call object to request place details from the API
        Call<PlacesResponse> call = service.getPlaceDetails(placeId, API_KEY);

        // Log the URL of the request for debugging purposes
        Log.d(TAG, "Request URL: " + BASE_URL + "place/details/json?place_id=" + placeId + "&key=" + API_KEY);

        // Asynchronously execute the request
        call.enqueue(new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                // Handle the API response
                if (response.isSuccessful()) {
                    // Check if the response was successful
                    PlacesResponse placesResponse = response.body(); // Get the response body

                    // Create a Gson instance for pretty-printing JSON responses
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    // Convert the response body to a pretty-printed JSON string
                    String jsonResponse = gson.toJson(response.body());

                    // Log the JSON response for debugging
                    Log.d(TAG, "PlacesResponse JSON: " + jsonResponse);

                    // Check if the response body and result are valid
                    if (placesResponse != null && placesResponse.result != null) {
                        // Update the UI with the place details
                        updateUIWithPlaceDetails(placesResponse.result);
                    } else {
                        // Log an error if the response or result is null
                        Log.e(TAG, "PlacesResponse or placesResponse.result is null");
                    }
                } else {
                    // Log an error if the request was not successful
                    Log.e(TAG, "Request failed with status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                // Handle request failure
                // Log the error message for debugging
                Log.e(TAG, "Request failed: " + t.getMessage());
            }
        });
    }

    // Update UI with place details
    private void updateUIWithPlaceDetails(PlacesResponse.Result result) {

        // Check if there are photos available for the venue
        if (result.photos != null && !result.photos.isEmpty()) {
            // Find the CardView and RecyclerView for displaying venue images
            CardView venueImageHolder = findViewById(R.id.venueImageHolder);
            RecyclerView venueImageRecyclerView = findViewById(R.id.venueImageRecyclerView);

            // Set up the RecyclerView to display images horizontally
            venueImageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            // Set the adapter to display the list of photos using VenueImageAdapter
            venueImageRecyclerView.setAdapter(new VenueImageAdapter(this, result.photos));

            // Make the CardView visible to show the images
            venueImageHolder.setVisibility(View.VISIBLE);
        }

        // Check if opening hours information is available
        if (result.opening_hours != null) {
            // Find the CardView and TextView for displaying opening hours
            CardView openingHoursHolder = findViewById(R.id.openingHoursHolder);
            TextView openingHoursTextView = findViewById(R.id.openingHours);

            // Build a string representation of the opening hours
            StringBuilder openingHours = new StringBuilder();
            for (String hour : result.opening_hours.weekday_text) {
                openingHours.append(hour).append("\n");
            }
            // Set the opening hours text and make the CardView visible
            openingHoursTextView.setText(openingHours.toString().trim());
            openingHoursHolder.setVisibility(View.VISIBLE);
        }

        // Check if an international phone number is available
        if (result.international_phone_number != null) {
            // Find the CardView and TextView for displaying the phone number
            CardView phoneNumberHolder = findViewById(R.id.phoneNumberHolder);
            TextView phoneNumberTextView = findViewById(R.id.phoneNumberTextView);

            // Set the phone number text and make the CardView visible
            phoneNumberTextView.setText(result.international_phone_number);
            phoneNumberHolder.setVisibility(View.VISIBLE);
        }

        // Check if a website URL is available
        if (result.website != null) {
            // Find the CardView and TextView for displaying the website URL
            CardView websiteHolder = findViewById(R.id.websiteHolder);
            TextView websiteTextView = findViewById(R.id.websiteTextView);

            // Set the website URL text and make the CardView visible
            websiteTextView.setText(result.website);
            websiteHolder.setVisibility(View.VISIBLE);
        }

        // Find the TextView for displaying wheelchair accessibility information
        TextView wheelchairTextView = findViewById(R.id.wheelchairTextView);
        // Set the wheelchair accessibility text based on the boolean value
        if (result.wheelchair_accessible_entrance) {
            wheelchairTextView.setText("Yes");
        } else {
            wheelchairTextView.setText("No");
        }

        // Find the TextView for displaying the rating
        TextView ratingTextView = findViewById(R.id.ratingTextView);
        // Set the rating text with a "/5" suffix
        ratingTextView.setText(result.rating + "/5");

        // Find the TextView for displaying the total number of ratings
        TextView totalRatingsTextView = findViewById(R.id.totalRatingsTextView);
        // Set the total number of user ratings
        totalRatingsTextView.setText(String.valueOf(result.user_ratings_total));

        // Check if there are reviews available
        if (result.reviews != null && !result.reviews.isEmpty()) {
            // Find the CardView and RecyclerView for displaying reviews
            CardView reviewsHolder = findViewById(R.id.reviewsHolder);
            RecyclerView recyclerView = findViewById(R.id.reviewRecyclerView);

            // Set up the RecyclerView to display reviews in a vertical list
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Set the adapter to display the list of reviews using VenueReviewAdapter
            recyclerView.setAdapter(new VenueReviewAdapter(result.reviews));

            // Make the CardView visible to show the reviews
            reviewsHolder.setVisibility(View.VISIBLE);
        }
    }

    // Show a dialog to select the map type
    private void showMapTypeSelectorDialog() {
        // Define the options for map types
        final String[] mapTypes = {"Normal", "Satellite", "Terrain", "Hybrid"};

        // Create an AlertDialog.Builder to build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Map Type")  // Set the title of the dialog
                .setItems(mapTypes, (dialog, which) -> {  // Set the list of map types and handle item selection
                    switch (which) {
                        case 0:
                            // Set the map type to Normal (default view)
                            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1:
                            // Set the map type to Satellite (satellite imagery)
                            gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2:
                            // Set the map type to Terrain (terrain and elevation view)
                            gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            break;
                        case 3:
                            // Set the map type to Hybrid (satellite imagery with roads and labels)
                            gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            break;
                    }
                });

        // Show the dialog to the user
        builder.show();
    }


    // Fetch nearby places based on the selected type and location
    private void fetchNearbyPlaces(LatLng location) {
        // Create a Retrofit instance with base URL and Gson converter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create a service for the Places API
        PlacesService service = retrofit.create(PlacesService.class);

        // Clear existing markers on the map
        hideNearbyPlaces();

        // Prepare and execute the request to fetch nearby places
        Call<NearbyPlacesResponse> call = service.getNearbyPlaces(
                selectedPlaceType,  // Type of places to search for (e.g., restaurants, parks)
                location.latitude + "," + location.longitude,  // Latitude and longitude of the current location
                PROXIMITY_RADIUS,  // Radius within which to search for places
                API_KEY);  // API key for authentication

        // Enqueue the request to be executed asynchronously
        call.enqueue(new Callback<NearbyPlacesResponse>() {
            @Override
            public void onResponse(Call<NearbyPlacesResponse> call, Response<NearbyPlacesResponse> response) {
                // Handle the response from the API
                if (response.isSuccessful() && response.body() != null) {
                    // Iterate through the list of results and add markers for each place
                    for (NearbyPlacesResponse.Result place : response.body().results) {
                        // Create a LatLng object for the place's location
                        LatLng placeLocation = new LatLng(place.geometry.location.lat, place.geometry.location.lng);
                        // Add a marker on the map at the place's location
                        Marker marker = gMap.addMarker(new MarkerOptions()
                                .position(placeLocation)  // Set marker position
                                .title(place.name)  // Set marker title
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));  // Set marker color
                        // Add the marker to the list of place markers
                        placeMarkers.add(marker);
                    }

                } else {
                    // Log an error if the request was not successful
                    Log.e(TAG, "Nearby Places request failed with status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NearbyPlacesResponse> call, Throwable t) {
                // Log an error if the request failed
                Log.e(TAG, "Nearby Places request failed: " + t.getMessage());
            }
        });
    }


    // Hide all markers for nearby places
    private void hideNearbyPlaces() {
        for (Marker marker : placeMarkers) {
            marker.remove();
        }
        placeMarkers.clear();
    }
}
