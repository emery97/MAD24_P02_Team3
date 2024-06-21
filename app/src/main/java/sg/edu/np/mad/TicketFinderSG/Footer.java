package sg.edu.np.mad.TicketFinderSG;

import android.widget.ImageButton;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class Footer {
    public static void setUpFooter(final Activity activity) {
        // Finding the footer icons by their IDs
        ImageButton homeIcon = activity.findViewById(R.id.footerHomeIcon);
        ImageButton searchIcon = activity.findViewById(R.id.footerSearchIcon);
        ImageButton bookingIcon = activity.findViewById(R.id.footerBookingIcon);
        ImageButton accountIcon = activity.findViewById(R.id.footerAccountIcon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Create an intent to start the homepage activity
                Intent intent = new Intent(activity, homepage.class);
                activity.startActivity(intent); // Start the homepage activity
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set click listener for the search icon
                Intent intent = new Intent(activity, ExploreEvents.class);
                activity.startActivity(intent); // Start the ExploreEvents activity
            }
        });

        // ---- replace class when purchase history / booking history initialised
        // **** REPLACED ****
        bookingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the BookingHistoryDetails activity
                Intent intent = new Intent(activity, BookingHistoryDetails.class);
                activity.startActivity(intent); // Start the BookingHistoryDetails activity
            }
        });
        accountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the profilePage activity
                Intent intent = new Intent(activity, profilePage.class);
                activity.startActivity(intent); // Start the profilePage activity
            }
        });
    }
}
