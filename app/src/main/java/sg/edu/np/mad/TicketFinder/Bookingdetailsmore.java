package sg.edu.np.mad.TicketFinder;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private TextView textViewCountdownTimer;
    private CountDownTimer countDownTimer;
    private List<String> remindersList = new ArrayList<>();
    private String currentReminderDetails;
    private Button generateQRButton;
    private JSONObject eventDetails;
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

        textViewCountdownTimer = findViewById(R.id.textViewCountdownTimer);

        generateQRButton = findViewById(R.id.buttonGenerateQR);

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
            ArrayList<String> remindersList = extras.getStringArrayList("reminders");

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

            // Disable switch if event date has passed
            if (hasEventDatePassed(eventDate)) {
                switchNotification.setEnabled(false);
                switchNotification.setChecked(false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(eventIdentifier + "_SwitchState", false);
                editor.apply();
            } else {
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
            }

            // Check if event date is within 4 days of the current date
            if (isEventDateWithinNextFourDays(eventDate)) {
                fetchWeatherData(eventDate);
            }

            if (remindersList != null && !remindersList.isEmpty()) {
                for (String reminder : remindersList) {
                    addReminderTextViewToLayout(reminder);
                }
            }

            generateQRButton.setOnClickListener(v -> generateQRCodeOnClick(v));

            calculateCountdown(eventDate);
        }

        Button buttonAddReminder = findViewById(R.id.buttonaddreminder);
        buttonAddReminder.setOnClickListener(v -> showAddReminderDialog());

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
                        showTimePickerDialog(eventIdentifier, calendar);
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
                // Set default time to 12 PM
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                // Call method to schedule notification
                scheduleNotification(eventIdentifier, calendar);
            }
        });

        // Set button text color for datePickerDialog
        datePickerDialog.setOnShowListener(dialog -> {
            // Get the positive (OK) button of the date picker dialog
            Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.BLACK);
            }

            // Get the negative (Cancel) button of the date picker dialog
            Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLACK);
            }
        });

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Disable past dates

        datePickerDialog.show();
    }

    private void showTimePickerDialog(String eventIdentifier, Calendar calendar) {
        // Time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
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
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Call method to schedule notification
            scheduleNotification(eventIdentifier, calendar);
        });

        // Set button text color for timePickerDialog
        timePickerDialog.setOnShowListener(dialog -> {
            // Get the positive (OK) button of the time picker dialog
            Button positiveButton = timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.BLACK);
            }

            // Get the negative (Cancel) button of the time picker dialog
            Button negativeButton = timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE);
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLACK);
            }
        });

        timePickerDialog.show();
    }

    private void scheduleNotification(String eventIdentifier, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EventWeatherNotification.class);

        // Ensure TextViews are not null
        String eventDate = (textViewEventDate != null) ? textViewEventDate.getText().toString() : "No Event Date";
        String temperature = (textViewTemperature != null) ? textViewTemperature.getText().toString() : "No Temperature";
        String forecast = (textViewForecast != null) ? textViewForecast.getText().toString() : "No Forecast";
        String weatherdate = (textViewWeatherDate != null) ? textViewWeatherDate.getText().toString() : "No Weather Date";
        String weatherdetails = (textViewWeatherDetails != null) ? textViewWeatherDetails.getText().toString() : "No Weather Details";
        String eventTitle = getIntent().getStringExtra("event_tistle");
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

        intent.putStringArrayListExtra("reminders", new ArrayList<>(remindersList));

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

    private void calculateCountdown(String eventDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        try {
            Date eventDateTime = dateFormat.parse(eventDate);
            long eventTimeMillis = eventDateTime.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long timeDiffMillis = eventTimeMillis - currentTimeMillis;

            if (timeDiffMillis > 0) {
                countDownTimer = new CountDownTimer(timeDiffMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;
                        long days = hours / 24;

                        // Format the countdown text
                        String countdownText = String.format(Locale.getDefault(),
                                "%02d:%02d:%02d:%02d",
                                days, hours % 24, minutes % 60, seconds % 60);

                        // Update the countdown timer TextView
                        textViewCountdownTimer.setText(countdownText);
                        textViewCountdownTimer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFinish() {
                        // Optionally, handle onFinish actions
                        textViewCountdownTimer.setText("Event started");
                    }
                };
                countDownTimer.start();
            } else {
                // Handle case where event time is in the past or very near future
                textViewCountdownTimer.setText("Event started or is very near");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the countdown timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void showAddReminderDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.reminder_dialog);
        dialog.setCancelable(true);

        EditText editTextReminder = dialog.findViewById(R.id.editTextReminder);
        Button buttonSaveReminder = dialog.findViewById(R.id.buttonSaveReminder);

        buttonSaveReminder.setOnClickListener(v -> {
            String reminderText = editTextReminder.getText().toString().trim();
            if (!reminderText.isEmpty()) {
                // Save reminder and update UI
                saveReminder(reminderText);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveReminder(String reminderText) {
        remindersList.add(reminderText);

        // Create a new ConstraintLayout to hold the TextView and delete icon
        ConstraintLayout reminderLayout = new ConstraintLayout(this);
        reminderLayout.setId(ViewCompat.generateViewId()); // Generate a unique ID for the ConstraintLayout

        // Create a new TextView for the reminder
        TextView textViewSavedReminder = new TextView(this);
        textViewSavedReminder.setId(ViewCompat.generateViewId()); // Generate a unique ID for the TextView
        textViewSavedReminder.setText(reminderText);
        textViewSavedReminder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Adjust text size as needed
        textViewSavedReminder.setTextColor(Color.BLACK); // Adjust text color as needed
        reminderLayout.addView(textViewSavedReminder);

        // Create a delete icon (ImageView) and set its properties
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setId(ViewCompat.generateViewId()); // Generate a unique ID for the ImageView
        deleteIcon.setImageResource(android.R.drawable.ic_delete);
        reminderLayout.addView(deleteIcon);

        // Set constraints for the TextView and delete icon within the ConstraintLayout
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(reminderLayout);

        // Constraints for textViewSavedReminder
        constraintSet.connect(textViewSavedReminder.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START, 16); // Adjust start margin as needed
        constraintSet.connect(textViewSavedReminder.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8); // Adjust top margin as needed
        constraintSet.connect(textViewSavedReminder.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8); // Adjust bottom margin as needed

        // Constraints for deleteIcon
        constraintSet.connect(deleteIcon.getId(), ConstraintSet.START,
                textViewSavedReminder.getId(), ConstraintSet.END, 16); // Adjust start margin as needed
        constraintSet.connect(deleteIcon.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8); // Adjust top margin as needed
        constraintSet.connect(deleteIcon.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8); // Adjust bottom margin as needed

        constraintSet.applyTo(reminderLayout);

        // Add the reminderLayout to the main ConstraintLayout (layoutReminderContainer)
        ConstraintLayout layout = findViewById(R.id.layoutReminderContainer); // Replace with your actual parent ConstraintLayout
        layout.addView(reminderLayout);

        // Set constraints for the reminderLayout within layoutReminderContainer
        setReminderLayoutConstraints(layout);

        // Set click listener for deleteIcon to remove this reminder
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the reminder from the list and layout
                remindersList.remove(reminderText);
                layout.removeView(reminderLayout); // Remove entire reminder layout

                // Reapply constraints to remaining reminders
                setReminderLayoutConstraints(layout);
            }
        });
    }

    private void setReminderLayoutConstraints(ConstraintLayout layout) {
        ConstraintSet layoutConstraintSet = new ConstraintSet();
        layoutConstraintSet.clone(layout);

        // Iterate over all children of layoutReminderContainer and set constraints
        int previousViewId = R.id.textViewReminder; // Start below the textViewReminder
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ConstraintLayout && child.getId() != R.id.textViewReminder) {
                int currentViewId = child.getId();
                layoutConstraintSet.connect(currentViewId, ConstraintSet.TOP,
                        previousViewId, ConstraintSet.BOTTOM, 8); // Adjust top margin as needed
                layoutConstraintSet.connect(currentViewId, ConstraintSet.START,
                        R.id.textViewReminder, ConstraintSet.START);
                previousViewId = currentViewId;
            }
        }

        layoutConstraintSet.applyTo(layout);
    }


    private void addReminderTextViewToLayout(String reminderText) {
        // Create a new ConstraintLayout to hold the TextView and delete icon
        ConstraintLayout reminderLayout = new ConstraintLayout(this);
        reminderLayout.setId(ViewCompat.generateViewId()); // Generate a unique ID for the ConstraintLayout

        // Create a new TextView for the reminder
        TextView textViewSavedReminder = new TextView(this);
        textViewSavedReminder.setId(ViewCompat.generateViewId()); // Generate a unique ID for the TextView
        textViewSavedReminder.setText(reminderText);
        textViewSavedReminder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Adjust text size as needed
        textViewSavedReminder.setTextColor(Color.BLACK); // Adjust text color as needed
        reminderLayout.addView(textViewSavedReminder);

        // Create a delete icon (ImageView) and set its properties
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setId(ViewCompat.generateViewId()); // Generate a unique ID for the ImageView
        deleteIcon.setImageResource(android.R.drawable.ic_delete);
        reminderLayout.addView(deleteIcon);

        // Set constraints for the TextView and delete icon within the reminderLayout
        ConstraintSet reminderLayoutConstraintSet = new ConstraintSet();
        reminderLayoutConstraintSet.clone(reminderLayout);

        // Constraints for textViewSavedReminder
        reminderLayoutConstraintSet.connect(textViewSavedReminder.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START, 16); // Adjust start margin as needed
        reminderLayoutConstraintSet.connect(textViewSavedReminder.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8); // Adjust top margin as needed
        reminderLayoutConstraintSet.connect(textViewSavedReminder.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8); // Adjust bottom margin as needed

        // Constraints for deleteIcon
        reminderLayoutConstraintSet.connect(deleteIcon.getId(), ConstraintSet.START,
                textViewSavedReminder.getId(), ConstraintSet.END, 16); // Adjust start margin as needed
        reminderLayoutConstraintSet.connect(deleteIcon.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8); // Adjust top margin as needed
        reminderLayoutConstraintSet.connect(deleteIcon.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8); // Adjust bottom margin as needed

        reminderLayoutConstraintSet.applyTo(reminderLayout);

        // Add the reminderLayout to the main ConstraintLayout (layoutReminderContainer)
        ConstraintLayout layout = findViewById(R.id.layoutReminderContainer); // Replace with your actual parent ConstraintLayout
        layout.addView(reminderLayout);

        // Set constraints for the reminderLayout within layoutReminderContainer
        setReminderLayoutConstraints(layout);

        // Set click listener for deleteIcon to remove this reminder
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the reminder from the list and layout
                remindersList.remove(reminderText);
                layout.removeView(reminderLayout); // Remove entire reminder layout

                // Reapply constraints to remaining reminders
                setReminderLayoutConstraints(layout);
            }
        });
    }


    private boolean hasEventDatePassed(String eventDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        try {
            Date eventDateParsed = dateFormat.parse(eventDate);
            return eventDateParsed != null && eventDateParsed.before(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void generateQRCodeOnClick(View view) {
        // Create event details JSON object
        JSONObject eventDetailsJson = new JSONObject();
        Bundle extras = getIntent().getExtras();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        try {
            eventDetailsJson.put("event_title", extras.getString("event_title"));
            eventDetailsJson.put("event_date", extras.getString("event_date"));
            eventDetailsJson.put("date_bought", extras.getString("date_bought"));
            eventDetailsJson.put("seat_category", extras.getString("seat_category"));
            eventDetailsJson.put("seat_number", extras.getString("seat_number"));
            eventDetailsJson.put("total_price", extras.getString("total_price"));
            eventDetailsJson.put("quantity", extras.getString("quantity"));
            eventDetailsJson.put("payment_method", extras.getString("payment_method"));

            String userId = sharedPreferences.getString("UserId", "");
            String name = sharedPreferences.getString("Name", "");
            String email = sharedPreferences.getString("Email", "");
            String phoneNum = sharedPreferences.getString("PhoneNum", "");

            eventDetailsJson.put("user_id", userId);
            eventDetailsJson.put("name", name);
            eventDetailsJson.put("email", email);
            eventDetailsJson.put("phoneNum", phoneNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Start new activity to display QR code
        Intent intent = new Intent(Bookingdetailsmore.this, DisplayQRActivity.class);
        intent.putExtra("eventDetailsJson", eventDetailsJson.toString());
        startActivity(intent);
    }

}
