package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingDetailsAdapter extends RecyclerView.Adapter<BookingDetailsAdapter.ViewHolder> {
    private List<BookingDetails> bookingDetailsList;

    public BookingDetailsAdapter(List<BookingDetails> bookingDetailsList) {
        this.bookingDetailsList = bookingDetailsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_details_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingDetails bookingDetails = bookingDetailsList.get(position);
        holder.seatCategory.setText("Seat Category: " + bookingDetails.getSeatCategory());
        holder.seatNumber.setText("Seat Number: " + bookingDetails.getSeatNumber());
        holder.totalPrice.setText("Total Price: $" + bookingDetails.getTotalPrice());
        holder.quantity.setText("Quantity: " + bookingDetails.getQuantity());
        holder.paymentMethod.setText("Payment Method: " + bookingDetails.getPaymentMethod());
    }

    @Override
    public int getItemCount() {
        return bookingDetailsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView seatCategory, seatNumber, totalPrice, quantity, paymentMethod;

        public ViewHolder(View itemView) {
            super(itemView);
            seatCategory = itemView.findViewById(R.id.seatCategory);
            seatNumber = itemView.findViewById(R.id.seatNumber);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            quantity = itemView.findViewById(R.id.quantity);
            paymentMethod = itemView.findViewById(R.id.paymentMethod);
        }
    }
}
