package sg.edu.np.mad.TicketFinder;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Bookingdetailsmore extends AppCompatActivity {
    private static final String CHANNEL_ID = "event_notifications";
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "TicketFinderPrefs";
    private static final String SWITCH_STATE_KEY = "SwitchState";
    private TextView textViewEventDate;
    private TextView textViewTemperature;
    private TextView textViewForecast, textViewWeatherDate,textViewWeatherDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.bookingdetailsmore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNotificationChannel();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String eventTitle = extras.getString("event_title");
            String eventDate = extras.getString("event_date");
            String dateBought = extras.getString("date_bought");
            String seatCategory = extras.getString("seat_category");
            String seatNumber = extras.getString("seat_number");
            String totalPrice = extras.getString("total_price");
            String quantity = extras.getString("quantity");
            String paymentMethod = extras.getString("payment_method");

            // Display data in TextViews or other views in your layout
            TextView textViewEventTitle = findViewById(R.id.textViewEventTitle);
            textViewEventDate = findViewById(R.id.textViewEventDate);
            TextView textViewDateBought = findViewById(R.id.textViewDateBought);
            TextView textViewSeatCategory = findViewById(R.id.textViewSeatCategory);
            TextView textViewSeatNumber = findViewById(R.id.textViewSeatNumber);
            TextView textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
            TextView textViewQuantity = findViewById(R.id.textViewQuantity);
            TextView textViewPaymentMethod = findViewById(R.id.textViewPaymentMethod);

            textViewEventTitle.setText(eventTitle);
            textViewEventDate.setText(eventDate);
            textViewDateBought.setText(dateBought);
            textViewSeatCategory.setText(seatCategory);
            textViewSeatNumber.setText(seatNumber);
            textViewTotalPrice.setText(totalPrice);
            textViewQuantity.setText(quantity);
            textViewPaymentMethod.setText(paymentMethod);

            Switch switchNotification = findViewById(R.id.switchNotification);
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // Use a combination of event details to create a unique key
            String eventIdentifier = eventTitle + "_" + dateBought; // You can adjust this based on your specific requirements

            boolean switchState = sharedPreferences.getBoolean(eventIdentifier + "_SwitchState", false);
            switchNotification.setChecked(switchState);

            switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(eventIdentifier + "_SwitchState", isChecked);
                editor.apply();

                if (isChecked) {
                    sendNotification();
                    showDateTimePickerDialog(eventIdentifier);
                    Toast.makeText(this, "Notifications enabled for this event", Toast.LENGTH_SHORT).show();
                } else {
                    cancelScheduledNotification(eventIdentifier);
                }
            });

            // Check if event date is within 4 days of the current date
            if (isEventDateWithinNextFourDays(eventDate)) {
                fetchWeatherData(eventDate);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Notifications";
            String description = "Notifications for event updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo) // Replace with your notification icon
                .setContentTitle("Event Notification Enabled")
                .setContentText("You have enabled notifications for this event.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private boolean isEventDateWithinNextFourDays(String eventDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(eventDate);
            if (date != null) {
                String formattedDate = outputFormat.format(date);
                Date formattedEventDate = outputFormat.parse(formattedDate);

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 4);
                Date currentDatePlusFourDays = calendar.getTime();
                return !formattedEventDate.after(currentDatePlusFourDays);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing event date", e);
        }
        return false;
    }

    private void fetchWeatherData(String eventDate) {
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

                                // Parse and format event date to match the date in API
                                SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                try {
                                    Date parsedEventDate = inputFormat.parse(eventDate);
                                    if (parsedEventDate != null) {
                                        String formattedEventDate = outputFormat.format(parsedEventDate);
                                        if (date.equals(formattedEventDate)) {
                                            String forecast = forecastObject.getString("forecast");
                                            JSONObject temperatureObject = forecastObject.getJSONObject("temperature");
                                            int lowTemp = temperatureObject.getInt("low");
                                            int highTemp = temperatureObject.getInt("high");
                                            JSONObject humidityObject = forecastObject.getJSONObject("relative_humidity");
                                            int lowHumidity = humidityObject.getInt("low");
                                            int highHumidity = humidityObject.getInt("high");
                                            JSONObject windObject = forecastObject.getJSONObject("wind");
                                            JSONObject windSpeedObject = windObject.getJSONObject("speed");
                                            int lowWindSpeed = windSpeedObject.getInt("low");
                                            int highWindSpeed = windSpeedObject.getInt("high");
                                            String windDirection = windObject.getString("direction");

                                            // Update UI with weather data
                                            runOnUiThread(() -> {
                                                textViewWeatherDate = findViewById(R.id.textViewWeatherDate);
                                                textViewWeatherDate.setText(formattedEventDate);
                                                textViewWeatherDate.setVisibility(View.VISIBLE);

                                                textViewTemperature = findViewById(R.id.textViewTemperature);
                                                textViewTemperature.setText(String.format(Locale.getDefault(),
                                                        "%d°C / %d°C", lowTemp, highTemp));
                                                textViewTemperature.setVisibility(View.VISIBLE);

                                                textViewForecast= findViewById(R.id.textViewForecast);
                                                textViewForecast.setText(String.format(Locale.getDefault(),
                                                        "%s", forecast));
                                                textViewForecast.setVisibility(View.VISIBLE);

                                                textViewWeatherDetails = findViewById(R.id.textViewWeatherDetails);
                                                textViewWeatherDetails.setText(String.format(Locale.getDefault(),
                                                        "Humidity: %d%% - %d%% Wind Speed: %d km/h - %d km/h Wind Direction: %s",
                                                        lowHumidity, highHumidity, lowWindSpeed, highWindSpeed, windDirection));
                                                textViewWeatherDetails.setVisibility(View.VISIBLE);

                                                ImageView imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon);
                                                // Set weather icon based on forecast
                                                if (forecast.toLowerCase().contains("thundery")) {
                                                    imageViewWeatherIcon.setImageResource(R.drawable.thunderstorm);
                                                } else if (forecast.toLowerCase().contains("sunny")) {
                                                    imageViewWeatherIcon.setImageResource(R.drawable.sunny);
                                                } else if (forecast.toLowerCase().contains("rainy")) {
                                                    imageViewWeatherIcon.setImageResource(R.drawable.weathercloud);
                                                } else {
                                                    imageViewWeatherIcon.setImageResource(R.drawable.weathercloud); // Default image
                                                }
                                                imageViewWeatherIcon.setVisibility(View.VISIBLE);
                                            });
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showDateTimePickerDialog(String eventIdentifier) {
        Calendar calendar = Calendar.getInstance();

        // Date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Validate if selected date is after event date
                    if (isDateAfterEventDate(calendar)) {
                        Toast.makeText(this, "Please select a date on or before the event date", Toast.LENGTH_SHORT).show();
                        disableSwitch();
                    } else {
                        // Time picker dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                                (view1, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    calendar.set(Calendar.SECOND, 0);

                                    // Call method to schedule notification
                                    scheduleNotification(eventIdentifier, calendar);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false);

                        // Handle cancel event of time picker dialog
                        timePickerDialog.setOnCancelListener(dialog -> {
                            // Set default time to 12 PM if user cancels without selecting time
                            if (isDateAfterEventDate(calendar)) {
                                calendar.set(Calendar.HOUR_OF_DAY, 12);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                            }

                            // Call method to schedule notification
                            scheduleNotification(eventIdentifier, calendar);
                        });

                        timePickerDialog.show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Handle cancel event of date picker dialog
        datePickerDialog.setOnCancelListener(dialog -> {
            // Set default date to next day if user cancels without selecting date
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            // Validate if default date is after event date
            if (isDateAfterEventDate(calendar)) {
                Toast.makeText(this, "Please select a date on or before the event date", Toast.LENGTH_SHORT).show();
                disableSwitch();
            } else {
                // Time picker dialog (default time to 12 PM)
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        (view, hourOfDay, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);

                            // Call method to schedule notification
                            scheduleNotification(eventIdentifier, calendar);
                        },
                        12, 0, false); // Default time set to 12 PM

                timePickerDialog.setOnCancelListener(dialog1 -> {
                    // Set default time to 12 PM if user cancels without selecting time
                    if (isDateAfterEventDate(calendar)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 12);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                    }

                    // Call method to schedule notification
                    scheduleNotification(eventIdentifier, calendar);
                });

                timePickerDialog.show();
            }
        });

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Disable past dates
        datePickerDialog.show();
    }

    private void scheduleNotification(String eventIdentifier, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EventWeatherNotification.class);

        // Assuming textViewEventDate and textViewTemperature are TextViews in your layout
        String eventDate = textViewEventDate.getText().toString();
        String temperature = textViewTemperature.getText().toString();
        String forecast = textViewForecast.getText().toString();
        String weatherdate = textViewWeatherDate.getText().toString();
        String weatherdetails = textViewWeatherDetails.getText().toString();
        String eventTitle = getIntent().getStringExtra("event_title");
        String dateBought = getIntent().getStringExtra("date_bought");
        String seatCategory = getIntent().getStringExtra("seat_category");
        String seatNumber = getIntent().getStringExtra("seat_number");
        String totalPrice = getIntent().getStringExtra("total_price");
        String quantity = getIntent().getStringExtra("quantity");
        String paymentMethod = getIntent().getStringExtra("payment_method");

        intent.putExtra("event_date", eventDate);
        intent.putExtra("temperature", temperature);
        intent.putExtra("forecast", forecast);
        intent.putExtra("weatherdate", weatherdate);
        intent.putExtra("weatherdetails", weatherdetails);
        intent.putExtra("event_title", eventTitle);
        intent.putExtra("date_bought", dateBought);
        intent.putExtra("seat_category", seatCategory);
        intent.putExtra("seat_number", seatNumber);
        intent.putExtra("total_price", totalPrice);
        intent.putExtra("quantity", quantity);
        intent.putExtra("payment_method", paymentMethod);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Check if the chosen time is in the past, if so, add one day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Notification scheduled for " + calendar.getTime(), Toast.LENGTH_SHORT).show();

        Log.d(TAG, "scheduleNotification: Notification scheduled for " + calendar.getTime());
    }


    private void cancelScheduledNotification(String eventIdentifier) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EventWeatherNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        Toast.makeText(this, "Notification canceled", Toast.LENGTH_SHORT).show();
    }

    private boolean isDateAfterEventDate(Calendar selectedDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String eventDateString = textViewEventDate.getText().toString(); // Assuming textViewEventDate is your event date TextView
        try {
            Date eventDate = dateFormat.parse(eventDateString);
            return selectedDate.getTimeInMillis() > eventDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void disableSwitch() {
        Switch switchNotification = findViewById(R.id.switchNotification);
        switchNotification.setChecked(false);
    }
}
