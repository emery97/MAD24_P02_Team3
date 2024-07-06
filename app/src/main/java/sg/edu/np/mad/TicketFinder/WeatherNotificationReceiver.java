package sg.edu.np.mad.TicketFinder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "WeatherNotification";
    private static final String CHANNEL_ID = "WeatherChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        fetchWeatherDataAndNotify(context);
    }

    private void fetchWeatherDataAndNotify(final Context context) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.data.gov.sg/v1/environment/4-day-weather-forecast")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray items = json.getJSONArray("items");
                        JSONObject firstItem = items.getJSONObject(0);
                        JSONArray forecasts = firstItem.getJSONArray("forecasts");

                        if (forecasts.length() > 0) {
                            JSONObject firstForecast = forecasts.getJSONObject(0);
                            String forecastDate = firstForecast.getString("date");
                            String weatherSummary = firstForecast.getString("forecast");

                            // Format date for display
                            String formattedDate = formatDate(forecastDate);

                            // Send notification with date and weather summary
                            sendNotification(context, formattedDate, weatherSummary);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.message());
                }
            }
        });
    }

    private void sendNotification(Context context, String forecastDate, String weatherSummary) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Daily Weather Forecast")
                .setContentText(forecastDate + ": " + weatherSummary) // Include date and weather summary
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.BLUE)
                .setAutoCancel(true);

        Intent notificationIntent = new Intent(context, weather.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Notifications";
            String description = "Channel for daily weather notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private String formatDate(String dateString) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            SimpleDateFormat formattedDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return formattedDateFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return "";
        }
    }
}
