package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
    private DocumentReference qrCodeRef;

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

        ImageButton scanQRCodeButton = findViewById(R.id.scanQRCodeButton);
        scanQRCodeButton.setOnClickListener(v -> startQRCodeScanner());

        checkAndRemoveExpiredQRCodes();
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Scanned");
                Toast.makeText(this, "Scanned: ", Toast.LENGTH_LONG).show();
                handleQRCodeResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleQRCodeResult(String qrCodeData) {
        try {
            JSONObject qrData = new JSONObject(qrCodeData);
            updateQRCodeStatus(qrData);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_LONG).show();
        }
    }

    private void updateQRCodeStatus(JSONObject qrData) {
        try {
            String qrCodeString = qrData.toString();
            db.collection("QrCodes")
                    .whereEqualTo("data", qrCodeString)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    qrCodeRef = document.getReference();
                                    qrCodeRef.update("status", "waiting")
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "QR Code status updated to waiting");
                                                Intent intent = new Intent(BookingHistoryDetails.this, DisplayQRDetails.class);
                                                intent.putExtra("qrCodeData", qrCodeString);
                                                startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error updating QR Code status", e));
                                }
                            } else {
                                Log.d(TAG, "QR Code not found");
                                Toast.makeText(this, "QR Code not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.w(TAG, "Error querying QR Code", task.getException());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private void checkAndRemoveExpiredQRCodes() {
        db.collection("QrCodes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        WriteBatch batch = db.batch();
                        long currentTimeMillis = System.currentTimeMillis();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String dataString = document.getString("data");
                            if (dataString != null) {
                                try {
                                    JSONObject dataJson = new JSONObject(dataString);
                                    long expiryTimeMillis = dataJson.getLong("expiryTimeMillis");

                                    if (expiryTimeMillis <= currentTimeMillis) {
                                        batch.delete(document.getReference());
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing JSON data", e);
                                }
                            }
                        }

                        // Commit the batch write
                        batch.commit()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Expired QR codes removed successfully"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error removing expired QR codes", e));
                    } else {
                        Log.w(TAG, "Error querying QR codes", task.getException());
                    }
                });
    }
}

