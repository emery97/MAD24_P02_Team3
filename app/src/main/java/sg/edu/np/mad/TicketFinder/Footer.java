package sg.edu.np.mad.TicketFinder;

import android.widget.ImageButton;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class Footer {
    public static void setUpFooter(final Activity activity) {
        ImageButton homeIcon = activity.findViewById(R.id.footerHomeIcon);
        ImageButton searchIcon = activity.findViewById(R.id.footerSearchIcon);
        ImageButton bookingIcon = activity.findViewById(R.id.footerBookingIcon);
        ImageButton accountIcon = activity.findViewById(R.id.footerAccountIcon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(activity, homepage.class);
                activity.startActivity(intent);
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ExploreEvents.class);
                activity.startActivity(intent);
            }
        });

        // ---- replace class when purchase history / booking history initialised
        bookingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, homepage.class);
                activity.startActivity(intent);
            }
        });

        accountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, profilePage.class);
                activity.startActivity(intent);
            }
        });
    }
}
