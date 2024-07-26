package sg.edu.np.mad.TicketFinder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.text.TextUtils;

public class WeatherNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "WeatherNotification";
    private static final String CHANNEL_ID = "weather_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        fetchRemindersAndNotify(context);
        scheduleNextAlarm(context);
    }

    private void fetchRemindersAndNotify(final Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserId", null);

        if (userId == null) {
            Log.e(TAG, "User ID is null");
            return;
        }

        db.collection("Reminders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> remindersText = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String area = doc.getString("area");
                            String forecast = doc.getString("forecast");
                            remindersText.add(area + ": " + forecast);
                        }

                        String remindersString = remindersText.isEmpty() ? "No reminders for today." : TextUtils.join("\n", remindersText);
                        sendNotification(context, remindersString);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void sendNotification(Context context, String remindersString) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo) // Ensure this icon exists
                .setContentTitle("Daily Weather Reminders")
                .setContentText(remindersString)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remindersString)) // For longer texts
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Notifications";
            String description = "Channel for daily weather reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.BLUE);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleNextAlarm(Context context) {
        Log.d(TAG, "Scheduling next day's weather notification");

        Intent intent = new Intent(context, WeatherNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Set for the next day
        calendar.set(Calendar.HOUR_OF_DAY, 13); // Set to 1 PM (24-hour format)
        calendar.set(Calendar.MINUTE, 2); // Set to 2 minutes
        calendar.set(Calendar.SECOND, 0); // Set to 0 seconds

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
