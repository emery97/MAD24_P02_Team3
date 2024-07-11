package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookingHistoryDetails extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // Shared preferences for storing user data
    private FirebaseFirestore db;
    private RecyclerView recyclerView; // RecyclerView to display booking details
    private BookingDetailsAdapter bookingDetailsAdapter;
    private List<BookingDetails> bookingDetailsList = new ArrayList<>();
    private RecyclerView weatherRecyclerView;
    private bookingweatheradapter weatherAdapter;
    private List<bkweather> weatherItemList = new ArrayList<>();
    private final TextView[] weatherTexts = new TextView[4];
    private static final String TAG = "BookingHistoryDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_history_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Get shared preferences for user data
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        weatherRecyclerView = findViewById(R.id.weatherRecyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        weatherRecyclerView.setLayoutManager(horizontalLayoutManager);
        weatherAdapter = new bookingweatheradapter(weatherItemList);
        weatherRecyclerView.setAdapter(weatherAdapter);

        // Get user ID from shared preferences and fetch booking details
        String userId = sharedPreferences.getString("UserId", null);
        if (userId != null) {
            fetchBookingDetailsData(userId);
        }

        fetchWeatherData();

        // Set up footer
        Footer.setUpFooter(this);

        // Set OnClickListener for Upcoming Concerts button
        Button upcomingConcertButton = findViewById(R.id.upcomingConcertButton);
        upcomingConcertButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookingHistoryDetails.this, UpcomingConcertsActivity.class);
            startActivity(intent);
        });
    }

    // Method to fetch booking details data from Firestore
    private void fetchBookingDetailsData(String userId) {
        db.collection("BookingDetails")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear existing booking details list
                            bookingDetailsList.clear();
                            // Populate booking details list
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String eventTitle = document.getString("ConcertTitle");
                                String seatCategory = document.getString("SeatCategory");
                                String seatNumber = document.getString("SeatNumber");

                                // Retrieve TotalPrice as double and convert to String
                                Double totalPrice = document.getDouble("TotalPrice");
                                String totalPriceString = totalPrice != null ? String.valueOf(totalPrice) : null;

                                // Retrieve Quantity as long and convert to String
                                Long quantityLong = document.getLong("Quantity");
                                String quantityString = quantityLong != null ? String.valueOf(quantityLong) : null;

                                String paymentMethod = document.getString("PaymentMethod");

                                String time = document.getString("EventTime");

                                Timestamp purchaseTimeTimestamp = document.getTimestamp("PurchaseTime");
                                String purchaseTimeString = formatTimestamp(purchaseTimeTimestamp);

                                // Create BookingDetails object and add to list
                                BookingDetails bookingDetails = new BookingDetails(eventTitle, purchaseTimeString, time, seatCategory, seatNumber, totalPriceString, quantityString, paymentMethod);
                                bookingDetailsList.add(bookingDetails);
                            }

                            Collections.sort(bookingDetailsList, (booking1, booking2) -> {
                                // Convert purchase time strings to Date objects for comparison
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
                                try {
                                    Date date1 = sdf.parse(booking1.getpurcasetime());
                                    Date date2 = sdf.parse(booking2.getpurcasetime());
                                    // Sort in descending order (most recent first)
                                    return date2.compareTo(date1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });

                            // Initialize and set adapter for RecyclerView
                            bookingDetailsAdapter = new BookingDetailsAdapter(bookingDetailsList);
                            recyclerView.setAdapter(bookingDetailsAdapter);
                        } else {
                            Log.d("BookingHistoryDetails", "No booking details found");
                        }
                    } else {
                        Log.d("BookingHistoryDetails", "Error getting documents: ", task.getException());
                    }
                });
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
            return sdf.format(date);
        }
        return "";
    }

    private void fetchWeatherData() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.data.gov.sg/v1/environment/4-day-weather-forecast";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching weather data", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray itemsArray = jsonObject.getJSONArray("items");
                        if (itemsArray.length() > 0) {
                            JSONObject itemObject = itemsArray.getJSONObject(0);
                            JSONArray forecastsArray = itemObject.getJSONArray("forecasts");
                            for (int i = 0; i < forecastsArray.length(); i++) {
                                JSONObject forecastObject = forecastsArray.getJSONObject(i);
                                String date = forecastObject.getString("date");
                                String forecast = forecastObject.getString("forecast");
                                JSONObject temperatureObject = forecastObject.getJSONObject("temperature");
                                int lowTemp = temperatureObject.getInt("low");
                                int highTemp = temperatureObject.getInt("high");

                                int iconResId = getWeatherIconResId(forecast);
                                weatherItemList.add(new bkweather(date, iconResId, forecast, lowTemp, highTemp));
                            }
                            runOnUiThread(() -> weatherAdapter.notifyDataSetChanged());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing weather data", e);
                    }
                }
            }
        });
    }

    private int getWeatherIconResId(String forecast) {
        if (forecast.toLowerCase().contains("thundery")) {
            return R.drawable.thunderstorm;
        } else if (forecast.toLowerCase().contains("sunny")) {
            return R.drawable.sunny;
        } else if (forecast.toLowerCase().contains("rain") || forecast.toLowerCase().contains("cloud")) {
            return R.drawable.weathercloud;
        } else {
            return R.drawable.weathercloud; // Default icon
        }
    }
}

