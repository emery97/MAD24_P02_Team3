package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingConcertsAdapter extends RecyclerView.Adapter<UpcomingConcertsAdapter.ViewHolder> {
    private List<BookingDetailsII> upcomingConcertsList;
    private static final String TAG = "UpcomingConcertsAdapter";
    private FirebaseFirestore db;

    public UpcomingConcertsAdapter(List<BookingDetailsII> upcomingConcertsList, FirebaseFirestore db) {
        this.upcomingConcertsList = upcomingConcertsList;
        this.db = db;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView concertName, purchaseTime, eventTime, totalPrice, quantity, paymentMethod;
        public Button viewMoreButton;
        public View expandableLayout;
        public RecyclerView ticketRecyclerView;

        public ViewHolder(View view) {
            super(view);
            concertName = view.findViewById(R.id.EventTitle);
            purchaseTime = view.findViewById(R.id.Datebought);
            eventTime = view.findViewById(R.id.eventDate);
            totalPrice = view.findViewById(R.id.totalPrice);
            quantity = view.findViewById(R.id.quantity);
            paymentMethod = view.findViewById(R.id.paymentMethod);
            viewMoreButton = view.findViewById(R.id.viewMoreButton);
            expandableLayout = view.findViewById(R.id.expandableLayout);
            ticketRecyclerView = view.findViewById(R.id.ticketRecyclerView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_concert_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        BookingDetailsII bookingDetails = upcomingConcertsList.get(position);

        holder.concertName.setText(bookingDetails.getConcertTitle());
        holder.eventTime.setText("Concert Date: " + bookingDetails.getEventTime());
        holder.totalPrice.setText("Total Price: $" + bookingDetails.getTotalPrice());
        holder.quantity.setText("Quantity: " + bookingDetails.getQuantity());
        holder.paymentMethod.setText("Payment Method: " + bookingDetails.getPaymentMethod());

        // Format and set purchase time
        Timestamp purchaseTimestamp = bookingDetails.getPurchaseTime();
        if (purchaseTimestamp != null) {
            Date purchaseDate = purchaseTimestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
            String purchaseTimeString = sdf.format(purchaseDate);
            holder.purchaseTime.setText(purchaseTimeString);
        } else {
            holder.purchaseTime.setText("");
        }

        boolean isExpanded = bookingDetails.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.viewMoreButton.setText(isExpanded ? "View Less" : "View More");
        holder.viewMoreButton.setOnClickListener(v -> {
            bookingDetails.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });

        Log.d(TAG, "Fetching tickets for bookingDetails: " + bookingDetails.getConcertTitle());
        // Fetch and set tickets
        getTicketsFromIDs(bookingDetails.getTicketIDs(), tickets -> {
            Log.d(TAG, "Tickets fetched for bookingDetails: " + bookingDetails.getConcertTitle() + ", Tickets: " + tickets.size());
            if (!tickets.isEmpty()) {
                Log.d(TAG, "Setting TicketAdapter for RecyclerView at position: " + position);
                TicketAdapter ticketAdapter = new TicketAdapter(tickets);
                holder.ticketRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                holder.ticketRecyclerView.setAdapter(ticketAdapter);
                Log.d(TAG, "TicketAdapter set for RecyclerView at position: " + position);
            } else {
                Log.d(TAG, "No tickets fetched for RecyclerView at position: " + position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return upcomingConcertsList.size();
    }

    // Fetch tickets from BookingDetailsII using the ticketIDs
    private void getTicketsFromIDs(List<Long> ticketIDs, OnTicketsFetchedListener listener) {
        List<Ticket> tickets = new ArrayList<>();
        Log.d(TAG, "getTicketsFromIDs called with ticketIDs: " + ticketIDs.toString());

        if (ticketIDs.isEmpty()) {
            listener.onTicketsFetched(tickets);
            return;
        }

        // Use Firestore query to fetch tickets
        db.collection("Ticket")
                .whereIn("TicketID", ticketIDs)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Manually retrieve each field
                            String seatCategory = documentSnapshot.getString("SeatCategory");
                            String seatNumber = documentSnapshot.getString("SeatNumber");
                            Long ticketID = documentSnapshot.getLong("TicketID");
                            String concertTitle = documentSnapshot.getString("ConcertTitle");
                            Log.d(TAG, "getTicketsFromIDs: " + seatCategory);
                            Log.d(TAG, "getTicketsFromIDs: " + seatNumber);
                            Log.d(TAG, "getTicketsFromIDs: "+ ticketID);

                            if (seatCategory != null && seatNumber != null && ticketID != null) {
                                // Create a new Ticket object using the retrieved fields
                                Ticket ticket = new Ticket(seatCategory, seatNumber, ticketID, concertTitle);
                                tickets.add(ticket);
                                Log.d(TAG, "Ticket fetched: " + ticket.getSeatNumber() + ", Ticket ID: " + ticket.getTicketID());
                            } else {
                                Log.d(TAG, "Some fields are missing for ticket with ID: " + ticketID);
                            }
                        }
                        Log.d(TAG, "All tickets fetched for IDs: " + ticketIDs.toString());
                        listener.onTicketsFetched(tickets);
                    } else {
                        Log.d(TAG, "No tickets found for IDs: " + ticketIDs.toString());
                        listener.onTicketsFetched(tickets);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching ticket details", e);
                    listener.onTicketsFetched(tickets); // Handle partial results
                });
    }


    public interface OnTicketsFetchedListener {
        void onTicketsFetched(List<Ticket> tickets);
    }
}
