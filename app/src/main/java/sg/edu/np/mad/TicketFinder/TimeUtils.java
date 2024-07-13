package sg.edu.np.mad.TicketFinder;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static Timestamp convertToFirestoreTimestamp(String purchaseTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Singapore")); // Input timezone

            Date date = inputFormat.parse(purchaseTime);

            // Log the parsed date to see the exact time in milliseconds
            System.out.println("Parsed date (Singapore time): " + date.toString());

            // Convert the date to UTC+8 timezone for Firestore
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'Z", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Singapore")); // Ensuring the output time matches Firestore storage

            // Format the date to the desired output format
            String formattedDate = outputFormat.format(date);
            System.out.println("Formatted date for Firestore (Singapore time): " + formattedDate);

            // Convert to Firestore Timestamp
            Timestamp timestamp = new Timestamp(date);
            System.out.println("Formatted Timestamp for Firestore: " + timestamp.toDate().toString());

            return timestamp;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
