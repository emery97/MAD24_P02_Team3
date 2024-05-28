package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class bookingHistory extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_booking_history);

                // Set up the footer
                Footer.setUpFooter(this);
        }
}
