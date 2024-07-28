package sg.edu.np.mad.TicketFinder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.Manifest;

public class weather extends AppCompatActivity {
    private final TextView[] weatherTexts = new TextView[4];
    private Switch weatherNotificationSwitch;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String NOTIFICATION_KEY = "WeatherNotification";
    private static final String TAG = "WeatherActivity";

    private RecyclerView recyclerView;
    private WeatherAdapter weatherAdapter;
    private List<WeatherData> weatherDataList;
    private List<WeatherData24> weatherData2HourList;
    private SearchView searchViewWeather;
    private RecyclerView recyclerView24HourForecast;
    private WeatherAdapter24Hour weatherAdapter24Hour;
    private Spinner spinnerForecastType;
    private ImageButton backimagebutton;
    private static final int PAGE_SIZE = 10; // Number of items per page
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private ProgressBar progressBar;
    private RecyclerView remindersrecyclerview;
    private WeatherReminderAdapter weatherReminderAdapter;
    private ListenerRegistration reminderListener;
    private List<WeatherReminder> reminderList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);
        backimagebutton = findViewById(R.id.backimagebutton);
        backimagebutton.setOnClickListener(v -> {
            Intent intent = new Intent(weather.this, BookingHistoryDetails.class);
            startActivity(intent);
        });

        searchViewWeather = findViewById(R.id.searchViewWeather);
        spinnerForecastType = findViewById(R.id.spinnerForecastType);
        weatherData2HourList = new ArrayList<>();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewWeather);
        weatherAdapter = new WeatherAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(weatherAdapter);

        remindersrecyclerview = findViewById(R.id.remindersrecyclerview);
        weatherReminderAdapter = new WeatherReminderAdapter(new ArrayList<>());
        remindersrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        remindersrecyclerview.setAdapter(weatherReminderAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(remindersrecyclerview);

        setupFirestoreListener();

        recyclerView24HourForecast = findViewById(R.id.recyclerView24HourForecast);
        weatherAdapter24Hour = new WeatherAdapter24Hour(this,new ArrayList<>()); // Initialize with empty list
        recyclerView24HourForecast.setLayoutManager(new LinearLayoutManager(this));
        recyclerView24HourForecast.setAdapter(weatherAdapter24Hour);

        recyclerView24HourForecast.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMoreData && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                        isLoading = true;
                        currentPage++;
                        fetch2HourWeatherData(currentPage);
                    }
                }
            }
        });



        // Initialize the switch and shared preferences
        weatherNotificationSwitch = findViewById(R.id.weatherNotificationSwitch);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set the switch state based on shared preferences
        weatherNotificationSwitch.setChecked(sharedPreferences.getBoolean(NOTIFICATION_KEY, false));

        // Set the switch listener
        weatherNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(NOTIFICATION_KEY, isChecked);
            editor.apply();

            if (isChecked) {
//                scheduleDailyNotification(this);
                showTimePickerDialog();
            } else {
                cancelDailyWeatherNotification();
            }
        });
        spinnerForecastType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Show 4-day forecast
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView24HourForecast.setVisibility(View.GONE);
                } else if (position == 1) {
                    // Show 2-hour forecast
                    recyclerView.setVisibility(View.GONE);
                    recyclerView24HourForecast.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to showing 4-day forecast
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView24HourForecast.setVisibility(View.GONE);
            }
        });

        // Fetch weather data from API
        fetchWeatherData();

        setupSearchView();

        // Schedule daily notification if the switch is on
        if (weatherNotificationSwitch.isChecked()) {
            scheduleDailyNotification(this);
        }
    }

    private WeatherReminder removedItem;
    private int removedPosition;
    private String removedItemId;

    // Swipe left and drag and drop for the reminders
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            // Swap the items in the adapter
            weatherReminderAdapter.swapItems(fromPosition, toPosition);

            // Notify the adapter of the move
            weatherReminderAdapter.notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                int position = viewHolder.getAdapterPosition();
                removedItem = weatherReminderAdapter.getItem(position);
                removedPosition = position;

                // Remove the item from the adapter
                weatherReminderAdapter.removeItem(position);

                // Get the document ID from the removed item
                String documentId = removedItem.getDocumentId(); // Ensure you have a method to get the document ID

                if (documentId != null) {
                    // Delete the document from Firestore
                    db.collection("Reminders").document(documentId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error deleting document", e);
                            });
                } else {
                    Log.e(TAG, "Document ID is null, cannot delete document");
                }

                // Show Snackbar with Undo option
                Snackbar snackbar = Snackbar.make(recyclerView, "Item removed", Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Restore the item if Undo is clicked
                        weatherReminderAdapter.restoreItem(removedItem, removedPosition);
                        addReminderToFirestore(removedItem);
                    }
                });
                snackbar.show();
            }
        }



        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.parseColor("#FF0000"))
                    .addSwipeLeftActionIcon(R.drawable.baseline_delete_24)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };


    // 4 hour weather forecast fetching
    private void fetchWeatherData() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.data.gov.sg/v1/environment/4-day-weather-forecast")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage());
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Network request successful");
                    // Parse JSON and handle data
                    parseWeatherData(responseData);

                    fetch2HourWeatherData(currentPage);
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.message());
                    // Handle unsuccessful response
                }
            }
        });
    }

    // 2 hour weather forecast data
    private void fetch2HourWeatherData(int page) {
        showLoading();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "2-Hour Forecast request failed: " + e.getMessage());
                isLoading = false;
                runOnUiThread(() -> hideLoading());
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "2-Hour Forecast request successful");
                    // Parse JSON and handle data
                    runOnUiThread(() -> {
                        new Handler().postDelayed(() -> {
                            parse2HourWeatherData(responseData, page);
                            hideLoading();  // Hide ProgressBar when data is fetched
                            compareAndUpdateWeatherData();
                        }, 1000); // 3 seconds delay
                    });
                } else {
                    Log.e(TAG, "Unsuccessful 2-Hour Forecast response: " + response.message());
                    runOnUiThread(() -> hideLoading());
                    // Handle unsuccessful response
                }
                isLoading = false;
            }
        });
    }

    // Parsing 4 day weather forecast
    private void parseWeatherData(String responseData) {
        try {
            JSONObject json = new JSONObject(responseData);
            JSONArray items = json.getJSONArray("items");
            JSONObject firstItem = items.getJSONObject(0);
            JSONArray forecasts = firstItem.getJSONArray("forecasts");

            // Get today's date
            Date today = Calendar.getInstance().getTime();

            // Format to check against forecast dates
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // List to hold weather data
            weatherDataList = new ArrayList<>();

            // Loop through forecasts and find the next 4 days
            AtomicInteger count = new AtomicInteger(0);
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String forecastDateStr = forecast.getString("date");
                Date forecastDate = dateFormat.parse(forecastDateStr);

                if (forecastDate.after(today)) {
                    String forecastForecast = forecast.getString("forecast");

                    // Determine weather icon based on forecast
                    int weatherIconResId = getWeatherIconResId(forecastForecast);

                    JSONObject relativeHumidity = forecast.getJSONObject("relative_humidity");
                    int humidityLow = relativeHumidity.getInt("low");
                    int humidityHigh = relativeHumidity.getInt("high");

                    JSONObject temperature = forecast.getJSONObject("temperature");
                    int tempLow = temperature.getInt("low");
                    int tempHigh = temperature.getInt("high");

                    JSONObject wind = forecast.getJSONObject("wind");
                    JSONObject windSpeed = wind.getJSONObject("speed");
                    int windSpeedLow = windSpeed.getInt("low");
                    int windSpeedHigh = windSpeed.getInt("high");
                    String windDirection = wind.getString("direction");

                    // Create WeatherData object and add to list
                    WeatherData weatherData = new WeatherData(
                            formatDate(forecastDate),
                            forecastForecast,
                            String.format("%d%% - %d%%", humidityLow, humidityHigh),
                            String.format("%d°C - %d°C", tempLow, tempHigh),
                            String.format("%d km/h - %d km/h (%s)", windSpeedLow, windSpeedHigh, windDirection),
                            weatherIconResId
                    );
                    weatherDataList.add(weatherData);

                    // Update UI on the main thread
                    runOnUiThread(() -> weatherAdapter.setWeatherData(weatherDataList));

                    if (count.getAndIncrement() >= 3) {
                        break; // Stop after getting the next 4 days
                    }
                }
            }

        } catch (JSONException | ParseException e) {
            Log.e(TAG, "Error parsing JSON or date: " + e.getMessage());
            // Handle JSON parsing error or date parsing error
        }
    }

    // Parsing 2 hour weather forecast
    private void parse2HourWeatherData(String responseData, int page) {
        try {
            JSONObject json = new JSONObject(responseData);
            JSONArray areaMetadataArray = json.getJSONArray("area_metadata");
            JSONArray items = json.getJSONArray("items");
            JSONObject firstItem = items.getJSONObject(0);
            JSONArray forecasts = firstItem.getJSONArray("forecasts");

            List<WeatherData24> newWeatherData2HourList = new ArrayList<>();

            // Map to hold the area name to location mapping
            Map<String, double[]> areaLocationMap = new HashMap<>();

            for (int i = 0; i < areaMetadataArray.length(); i++) {
                JSONObject areaMetadata = areaMetadataArray.getJSONObject(i);
                String areaName = areaMetadata.getString("name");
                JSONObject labelLocation = areaMetadata.getJSONObject("label_location");
                double latitude = labelLocation.getDouble("latitude");
                double longitude = labelLocation.getDouble("longitude");

                areaLocationMap.put(areaName, new double[]{latitude, longitude});
            }

            // Calculate start and end indices based on page and page size
            int start = page * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, forecasts.length());

            for (int i = start; i < end; i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String area = forecast.getString("area");
                String forecastDescription = forecast.getString("forecast");

                double[] location = areaLocationMap.get(area);
                double latitude = location != null ? location[0] : 0;
                double longitude = location != null ? location[1] : 0;

                WeatherData24 weatherData = new WeatherData24(area, forecastDescription, latitude, longitude);
                newWeatherData2HourList.add(weatherData);
            }

            runOnUiThread(() -> {
                if (newWeatherData2HourList.size() < PAGE_SIZE) {
                    hasMoreData = false; // No more data to load
                }
                weatherData2HourList.addAll(newWeatherData2HourList);
                weatherAdapter24Hour.addWeatherData(newWeatherData2HourList);
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing 2-Hour Forecast JSON: " + e.getMessage());
        }
    }



    private int getWeatherIconResId(String forecast) {
        // Default icon if none of the conditions match
        int weatherIconResId = R.drawable.weathercloud;

        // Check forecast string and set appropriate icon
        if (forecast.toLowerCase().contains("thundery")) {
            weatherIconResId = R.drawable.thunderstorm;
        } else if (forecast.toLowerCase().contains("sunny")) {
            weatherIconResId = R.drawable.sunny;
        } else if (forecast.toLowerCase().contains("rainy")) {
            weatherIconResId = R.drawable.weathercloud;
        }

        return weatherIconResId;
    }

    // Utility method to format date for display
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    // Datetime picker dialog display for user to select date and time
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            // Store selected time in shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notificationHour", selectedHour);
            editor.putInt("notificationMinute", selectedMinute);
            editor.apply();

            // Schedule the notification at the selected time
            scheduleDailyNotification(this);
        }, hour, minute, true);

        timePickerDialog.setOnShowListener(dialog -> {
            Button positiveButton = timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE);
            Button negativeButton = timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE);

            // Change button colors
            int color = ContextCompat.getColor(this, R.color.black);
            positiveButton.setTextColor(color);
            negativeButton.setTextColor(color);
        });

        timePickerDialog.show();
    }

    // Schedules daily notification
    private void scheduleDailyNotification(Context context) {
        Log.d(TAG, "Scheduling daily weather notification");

        Intent intent = new Intent(context, WeatherNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Retrieve the selected time from shared preferences
        int hour = sharedPreferences.getInt("notificationHour", 10); // Default to 8 AM if not set
        int minute = sharedPreferences.getInt("notificationMinute", 0); // Default to 0 minutes if not set

        // Set the alarm to trigger at the user-selected time each day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If the time is in the past for today, set it for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Toast.makeText(context, "Enabled Daily Weather Notifications", Toast.LENGTH_SHORT).show();

        // Set an exact alarm to ensure it triggers at the precise time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    private void cancelDailyWeatherNotification() {
        Log.d(TAG, "Canceling daily weather notification");
        Intent intent = new Intent(this, WeatherNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void setupSearchView() {
        searchViewWeather.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String searchText) {
        if (weatherDataList == null || weatherDataList.isEmpty()) {
            return;
        }

        // Filter for 4-day forecast
        List<WeatherData> filteredList = new ArrayList<>();
        for (WeatherData data : weatherDataList) {
            if (data.getDate().toLowerCase().contains(searchText.toLowerCase())
                    || data.getForecast().toLowerCase().contains(searchText.toLowerCase()))                     {
                filteredList.add(data);
            }
        }

        // Update 4-day forecast RecyclerView with filtered data
        weatherAdapter.setWeatherData(filteredList);

        // Filter for 2-hour forecast
        if (weatherData2HourList == null || weatherData2HourList.isEmpty()) {
            Log.d(TAG, "2-hour forecast data is empty");
            return;
        }

        List<WeatherData24> filteredList24Hour = new ArrayList<>();
        for (WeatherData24 data : weatherData2HourList) {
            if (data.getArea().toLowerCase().contains(searchText.toLowerCase())
                    || data.getForecast().toLowerCase().contains(searchText.toLowerCase())
                    || Double.toString(data.getLatitude()).contains(searchText.toLowerCase())
                    || Double.toString(data.getLongitude()).contains(searchText.toLowerCase())) {
                filteredList24Hour.add(data);
            }
        }

        Log.d(TAG, "Filtering 2-hour data: " + filteredList24Hour.size() + " items found");
        weatherAdapter24Hour.filterList(filteredList24Hour);
    }

    private void showLoading() {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    private void hideLoading() {
        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }

    private void setupFirestoreListener() {
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserId", null);

        reminderListener = db.collection("Reminders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<WeatherReminder> reminders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String area = doc.getString("area");
                            String forecast = doc.getString("forecast");
                            String documentId = doc.getId(); // Get the document ID

                            WeatherReminder reminder = new WeatherReminder(area, forecast,documentId);
                            reminders.add(reminder);
                        }
                        weatherReminderAdapter.setReminderList(reminders);
                    }
                });
    }

    private void addReminderToFirestore(WeatherReminder reminder) {
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserId", null);
        Map<String, Object> reminderMap = new HashMap<>();
        reminderMap.put("area", reminder.getArea());
        reminderMap.put("forecast", reminder.getForecast());
        reminderMap.put("userId", userId); // Add the user ID

        db.collection("Reminders").document(reminder.getDocumentId())
                .set(reminderMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Reminder re-added"))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding document: ", e));
    }


    private void compareAndUpdateWeatherData() {
        db.collection("Reminders")
                .whereEqualTo("userId", getUserId()) // Assuming you have a method to get the user ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Map<String, WeatherReminder> currentRemindersMap = new HashMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String area = doc.getString("area");
                            String forecast = doc.getString("forecast");
                            String documentId = doc.getId();

                            WeatherReminder reminder = new WeatherReminder(area, forecast, documentId);
                            currentRemindersMap.put(area, reminder);
                        }

                        // Compare with new data
                        List<WeatherReminder> newReminders = weatherReminderAdapter.getReminderList();
                        for (WeatherReminder newReminder : newReminders) {
                            WeatherReminder existingReminder = currentRemindersMap.get(newReminder.getArea());

                            if (existingReminder != null && !existingReminder.getForecast().equals(newReminder.getForecast())) {
                                // Update Firebase
                                updateReminderInFirestore(newReminder);
                            } else if (existingReminder == null) {
                                // Add new reminder
                                addReminderToFirestore(newReminder);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching reminders: ", e));
    }

    private void updateReminderInFirestore(WeatherReminder reminder) {
        db.collection("Reminders").document(reminder.getDocumentId())
                .update("forecast", reminder.getForecast())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Reminder updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating reminder: ", e));
    }

    private String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserId", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reminderListener != null) {
            reminderListener.remove();
        }
    }
}