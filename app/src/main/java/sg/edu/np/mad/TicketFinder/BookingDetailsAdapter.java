package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingDetailsAdapter extends RecyclerView.Adapter<BookingDetailsAdapter.ViewHolder> {
    private List<BookingDetails> bookingDetailsList;

    // Constructor
    public BookingDetailsAdapter(List<BookingDetails> bookingDetailsList) {
        this.bookingDetailsList = bookingDetailsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate booking details item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_details_item, parent, false);
        return new ViewHolder(view);
    }

    // putting data in each booking details item xml
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingDetails bookingDetails = bookingDetailsList.get(position);

        // Bind initial data
        holder.EventTitle.setText(bookingDetails.getConcertName());
        holder.eventDate.setText(bookingDetails.gettime());
        holder.Datebought.setText(bookingDetails.getpurcasetime());

        // Handle expansion state
        boolean isExpanded = bookingDetails.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            holder.seatCategory.setText("Seat Category: " + bookingDetails.getSeatCategory());
            holder.seatNumber.setText("Seat Number: " + bookingDetails.getSeatNumber());
            holder.totalPrice.setText("Total Price: $" + bookingDetails.getTotalPrice());
            holder.quantity.setText("Quantity: " + bookingDetails.getQuantity());
            holder.paymentMethod.setText("Payment Method: " + bookingDetails.getPaymentMethod());
            holder.viewMoreButton.setText("View Less");
        } else {
            holder.viewMoreButton.setText("View More");
        }

        // Handle click listener for view more button
        holder.viewMoreButton.setOnClickListener(v -> {
            bookingDetails.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return bookingDetailsList.size();
    }


    public BookingDetails getBookingDetailsAtPosition(int position) {
        return bookingDetailsList.get(position);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < bookingDetailsList.size()) {
            bookingDetailsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, bookingDetailsList.size()); // Notify the range change
        }
    }

    // get views from xml
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView EventTitle,Datebought,eventDate,seatCategory, seatNumber, totalPrice, quantity, paymentMethod;
        LinearLayout expandableLayout;
        Button viewMoreButton, detailsbtn, deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            EventTitle = itemView.findViewById(R.id.EventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            Datebought = itemView.findViewById(R.id.Datebought);
            seatCategory = itemView.findViewById(R.id.seatCategory);
            seatNumber = itemView.findViewById(R.id.seatNumber);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            quantity = itemView.findViewById(R.id.quantity);
            paymentMethod = itemView.findViewById(R.id.paymentMethod);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            viewMoreButton = itemView.findViewById(R.id.viewMoreButton);
            detailsbtn = itemView.findViewById(R.id.detailsbtn);

            detailsbtn.setOnClickListener(v -> {
                // Handle click action here
                Intent intent = new Intent(itemView.getContext(), Bookingdetailsmore.class);
                intent.putExtra("event_title", EventTitle.getText().toString());
                intent.putExtra("event_date", eventDate.getText().toString());
                intent.putExtra("date_bought", Datebought.getText().toString());
                intent.putExtra("seat_category", seatCategory.getText().toString());
                intent.putExtra("seat_number", seatNumber.getText().toString());
                intent.putExtra("total_price", totalPrice.getText().toString());
                intent.putExtra("quantity", quantity.getText().toString());
                intent.putExtra("payment_method", paymentMethod.getText().toString());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
