package sg.edu.np.mad.TicketFinder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        searchViewWeather = findViewById(R.id.searchViewWeather);
        spinnerForecastType = findViewById(R.id.spinnerForecastType);
        weatherData2HourList = new ArrayList<>();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewWeather);
        weatherAdapter = new WeatherAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(weatherAdapter);

        recyclerView24HourForecast = findViewById(R.id.recyclerView24HourForecast);
        weatherAdapter24Hour = new WeatherAdapter24Hour(new ArrayList<>()); // Initialize with empty list
        recyclerView24HourForecast.setLayoutManager(new LinearLayoutManager(this));
        recyclerView24HourForecast.setAdapter(weatherAdapter24Hour);

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
                scheduleDailyNotification(this);
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

                    fetch2HourWeatherData();
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.message());
                    // Handle unsuccessful response
                }
            }
        });
    }

    private void fetch2HourWeatherData() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "2-Hour Forecast request failed: " + e.getMessage());
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "2-Hour Forecast request successful");
                    // Parse JSON and handle data
                    parse2HourWeatherData(responseData);
                } else {
                    Log.e(TAG, "Unsuccessful 2-Hour Forecast response: " + response.message());
                    // Handle unsuccessful response
                }
            }
        });
    }

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

    private void parse2HourWeatherData(String responseData) {
        try {
            JSONObject json = new JSONObject(responseData);
            JSONArray areaMetadataArray = json.getJSONArray("area_metadata");
            JSONArray items = json.getJSONArray("items");
            JSONObject firstItem = items.getJSONObject(0);
            JSONArray forecasts = firstItem.getJSONArray("forecasts");

            // List to hold 2-hour weather data
            weatherData2HourList  = new ArrayList<>();

            // Map to hold the area name to location mapping
            Map<String, double[]> areaLocationMap = new HashMap<>();

            // Loop through area metadata to get the name, latitude, and longitude
            for (int i = 0; i < areaMetadataArray.length(); i++) {
                JSONObject areaMetadata = areaMetadataArray.getJSONObject(i);
                String areaName = areaMetadata.getString("name");
                JSONObject labelLocation = areaMetadata.getJSONObject("label_location");
                double latitude = labelLocation.getDouble("latitude");
                double longitude = labelLocation.getDouble("longitude");

                areaLocationMap.put(areaName, new double[]{latitude, longitude});
            }

            // Loop through forecasts to get the area and forecast description
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String area = forecast.getString("area");
                String forecastDescription = forecast.getString("forecast");

                double[] location = areaLocationMap.get(area);
                double latitude = location != null ? location[0] : 0;
                double longitude = location != null ? location[1] : 0;

                // Create WeatherData24 object with area, forecast, latitude, and longitude
                WeatherData24 weatherData = new WeatherData24(area, forecastDescription, latitude, longitude);

                // Add to the list for RecyclerView
                weatherData2HourList.add(weatherData);
            }

            // Update UI with 2-hour weather data on the main thread
            runOnUiThread(() -> {
                weatherAdapter24Hour = new WeatherAdapter24Hour(weatherData2HourList);
                recyclerView24HourForecast.setAdapter(weatherAdapter24Hour);
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing 2-Hour Forecast JSON: " + e.getMessage());
            // Handle JSON parsing error
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

    private void scheduleDailyNotification(Context context) {
        Log.d(TAG, "Scheduling daily weather notification");

        Intent intent = new Intent(context, WeatherNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Set the alarm to trigger at a specific time each day (e.g., 8:49 PM)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 13); // Set to 8 PM (24-hour format)
        calendar.set(Calendar.MINUTE, 2); // Set to 49 minutes
        calendar.set(Calendar.SECOND, 0); // Set to 0 seconds

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

        // Update 2-hour forecast RecyclerView with filtered data
        weatherAdapter24Hour.filterList(filteredList24Hour);
    }
}
