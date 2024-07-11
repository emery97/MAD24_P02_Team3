package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpcomingConcertsAdapter extends RecyclerView.Adapter<UpcomingConcertsAdapter.ViewHolder> {
    private List<BookingDetails> upcomingConcertsList;

    public UpcomingConcertsAdapter(List<BookingDetails> upcomingConcertsList) {
        this.upcomingConcertsList = upcomingConcertsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.upcoming_concert_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingDetails bookingDetails = upcomingConcertsList.get(position);
        holder.concertName.setText(bookingDetails.getConcertName());
        holder.purchaseTime.setText(bookingDetails.getpurcasetime());
        holder.time.setText(bookingDetails.gettime());
        holder.seatCategory.setText("Seat Category: " + bookingDetails.getSeatCategory());
        holder.seatNumber.setText("Seat Number: " + bookingDetails.getSeatNumber());
        holder.totalPrice.setText("Total Price: $" + bookingDetails.getTotalPrice());
        holder.quantity.setText("Quantity: " + bookingDetails.getQuantity());
        holder.paymentMethod.setText("Payment Method: " + bookingDetails.getPaymentMethod());

        // Toggle Button logic
        boolean isExpanded = bookingDetails.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.viewMoreButton.setText(isExpanded ? "View Less" : "View More");
        holder.viewMoreButton.setOnClickListener(v -> {
            bookingDetails.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });

        // Set OnClickListener for transferTickets button
        holder.transferTicketsButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TransferTicketsActivity.class);
            holder.itemView.getContext().startActivity(intent);
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
            transferTicketsButton = view.findViewById(R.id.transferTickets); // Make sure the ID matches

            // Set OnClickListener for transferTickets button
            transferTicketsButton.setOnClickListener(v -> {
                Intent intent = new Intent(view.getContext(), TransferTicketsActivity.class);
                view.getContext().startActivity(intent);
            });
        }
    }
}
