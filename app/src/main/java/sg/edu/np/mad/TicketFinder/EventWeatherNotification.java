package sg.edu.np.mad.TicketFinder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class EventWeatherNotification extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_notifications";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "EventWeatherNotification";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Alarm received");

        // Retrieve data from intent extras
        String eventDate = intent.getStringExtra("event_date");
        String temperature = intent.getStringExtra("temperature");
        String forecast = intent.getStringExtra("forecast");
        String weatherdate = intent.getStringExtra("weatherdate");
        String weatherdetails = intent.getStringExtra("weatherdetails");
        String eventTitle = intent.getStringExtra("event_title");
        String dateBought = intent.getStringExtra("date_bought");
        String seatCategory = intent.getStringExtra("seat_category");
        String seatNumber = intent.getStringExtra("seat_number");
        String totalPrice = intent.getStringExtra("total_price");
        String quantity = intent.getStringExtra("quantity");
        String paymentMethod = intent.getStringExtra("payment_method");

        sendNotification(context, eventDate, temperature, forecast, eventTitle, dateBought, seatCategory, seatNumber, totalPrice, quantity, paymentMethod,weatherdate,weatherdetails);
    }

    private void sendNotification(Context context, String eventDate, String temperature, String forecast, String eventTitle, String dateBought, String seatCategory, String seatNumber, String totalPrice, String quantity, String paymentMethod, String weatherdate, String weatherdetails) {
        Intent resultIntent = new Intent(context, Bookingdetailsmore.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Add extras to the intent
        resultIntent.putExtra("event_date", eventDate);
        resultIntent.putExtra("temperature", temperature);
        resultIntent.putExtra("forecast", forecast);
        resultIntent.putExtra("weatherdate", weatherdate);
        resultIntent.putExtra("weatherdetails",weatherdetails);
        resultIntent.putExtra("event_title", eventTitle);
        resultIntent.putExtra("date_bought", dateBought);
        resultIntent.putExtra("seat_category", seatCategory);
        resultIntent.putExtra("seat_number", seatNumber);
        resultIntent.putExtra("total_price", totalPrice);
        resultIntent.putExtra("quantity", quantity);
        resultIntent.putExtra("payment_method", paymentMethod);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo) // Replace with your notification icon
                .setContentTitle("Weather Forecast for " + eventDate)
                .setContentText("Temperature: " + temperature + "\n" + forecast)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Set the PendingIntent
                .setAutoCancel(true); // Automatically removes the notification when tapped

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
