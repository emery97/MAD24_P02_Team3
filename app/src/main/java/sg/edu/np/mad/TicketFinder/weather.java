package sg.edu.np.mad.TicketFinder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize TextViews for each forecast day
        weatherTexts[0] = findViewById(R.id.weather1);
        weatherTexts[1] = findViewById(R.id.weather2);
        weatherTexts[2] = findViewById(R.id.weather3);
        weatherTexts[3] = findViewById(R.id.weather4);

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
                scheduleImmediateWeatherNotification();
            } else {
                cancelDailyWeatherNotification();
            }
        });

        // Fetch weather data from API
        fetchWeatherData();
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
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.message());
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

            // Loop through forecasts and find the next 4 days
            AtomicInteger count = new AtomicInteger(0);
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String forecastDateStr = forecast.getString("date");
                Date forecastDate = dateFormat.parse(forecastDateStr);

                if (forecastDate.after(today)) {
                    String forecastForecast = forecast.getString("forecast");

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        int currentCount = count.getAndIncrement();
                        if (currentCount < weatherTexts.length) {
                            weatherTexts[currentCount].setText(String.format("Forecast for %s: %s",
                                    formatDate(forecastDate), forecastForecast));
                        }
                    });

                    if (count.get() >= 4) {
                        break; // Stop after getting the next 4 days
                    }
                }
            }

        } catch (JSONException | ParseException e) {
            Log.e(TAG, "Error parsing JSON or date: " + e.getMessage());
            // Handle JSON parsing error or date parsing error
        }
    }

    // Utility method to format date for display
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    private void scheduleImmediateWeatherNotification() {
        Log.d(TAG, "Scheduling immediate weather notification");

        Intent intent = new Intent(this, WeatherNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Set the alarm to trigger immediately (you can adjust this time as needed)
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);

        scheduleDailyNotification(this);
    }

    private void scheduleDailyNotification(Context context) {
        Intent dailyIntent = new Intent(context, WeatherNotificationReceiver.class);
        dailyIntent.putExtra("dailyNotification", true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, dailyIntent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Set the alarm to trigger once, at 2:22 PM today (for testing)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 17); // Set to 2 PM (24-hour format)
        calendar.set(Calendar.MINUTE, 22); // Set to 22 minutes
        calendar.set(Calendar.SECOND, 0);

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
}
