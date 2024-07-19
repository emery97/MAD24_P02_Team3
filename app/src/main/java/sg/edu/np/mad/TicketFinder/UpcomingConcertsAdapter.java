package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.Map;

public class UpcomingConcertsAdapter extends RecyclerView.Adapter<UpcomingConcertsAdapter.ViewHolder> {
    private List<BookingDetails> upcomingConcertsList;
    private Map<BookingDetails, Timestamp> upcomingConcertsTimestamps; // Store timestamps separately
    private static final String TAG = "UpcomingConcertsAdapter";

    public UpcomingConcertsAdapter(List<BookingDetails> upcomingConcertsList, Map<BookingDetails, Timestamp> upcomingConcertsTimestamps) {
        this.upcomingConcertsList = upcomingConcertsList;
        this.upcomingConcertsTimestamps = upcomingConcertsTimestamps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_concert_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingDetails bookingDetails = upcomingConcertsList.get(position);
        holder.concertName.setText(bookingDetails.getConcertName());
        holder.purchaseTime.setText(bookingDetails.getpurcasetime()); // Display in Singapore time
        holder.time.setText(bookingDetails.gettime());
        holder.seatCategory.setText("Seat Category: " + bookingDetails.getSeatCategory());
        holder.seatNumber.setText("Seat Number: " + bookingDetails.getSeatNumber());
        holder.time.setText("Concert Date: " + bookingDetails.gettime());
        holder.totalPrice.setText("Total Price: $" + bookingDetails.getTotalPrice());
        holder.quantity.setText("Quantity: " + bookingDetails.getQuantity());
        holder.paymentMethod.setText("Payment Method: " + bookingDetails.getPaymentMethod());

        boolean isExpanded = bookingDetails.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.viewMoreButton.setText(isExpanded ? "View Less" : "View More");
        holder.viewMoreButton.setOnClickListener(v -> {
            bookingDetails.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });

        holder.transferTicketsButton.setOnClickListener(v -> {
            String concertName = bookingDetails.getConcertName();
            String purchaseTime = bookingDetails.getpurcasetime(); // Local time for display
            Timestamp purchaseTimeDb = upcomingConcertsTimestamps.get(bookingDetails); // Retrieve the timestamp

            if (purchaseTimeDb != null) {
                long seconds = purchaseTimeDb.getSeconds();
                int nanoseconds = purchaseTimeDb.getNanoseconds();

                Log.d(TAG, "Passing concertName: " + concertName + ", purchaseTime: " + purchaseTime + ", purchaseTimeDb: " + purchaseTimeDb.toDate().toString());

                Intent intent = new Intent(holder.itemView.getContext(), TransferTicketsActivity.class);
                intent.putExtra("CONCERT_NAME", concertName);
                intent.putExtra("PURCHASE_TIME", purchaseTime);
                intent.putExtra("PURCHASE_TIME_DB_SECONDS", seconds);
                intent.putExtra("PURCHASE_TIME_DB_NANOSECONDS", nanoseconds);
                holder.itemView.getContext().startActivity(intent);
            } else {
                Log.e(TAG, "Failed to get purchaseTimeDb.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return upcomingConcertsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView concertName, purchaseTime, time, seatCategory, seatNumber, totalPrice, quantity, paymentMethod;
        public Button viewMoreButton, transferTicketsButton;
        public View expandableLayout;

        public ViewHolder(View view) {
            super(view);
            concertName = view.findViewById(R.id.EventTitle);
            purchaseTime = view.findViewById(R.id.Datebought);
            time = view.findViewById(R.id.eventDate);
            seatCategory = view.findViewById(R.id.seatCategory);
            seatNumber = view.findViewById(R.id.seatNumber);
            totalPrice = view.findViewById(R.id.totalPrice);
            quantity = view.findViewById(R.id.quantity);
            paymentMethod = view.findViewById(R.id.paymentMethod);
            viewMoreButton = view.findViewById(R.id.viewMoreButton);
            expandableLayout = view.findViewById(R.id.expandableLayout);
            transferTicketsButton = view.findViewById(R.id.transferTickets);
        }
    }
}

