package sg.edu.np.mad.TicketFinder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UpcomingConcertsAdapter extends RecyclerView.Adapter<UpcomingConcertsAdapter.ViewHolder> {
    private List<UpcomingConcert> upcomingConcertsList;
    private static final String TAG = "UpcomingConcertsAdapter";
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserName;

    public UpcomingConcertsAdapter(List<UpcomingConcert> upcomingConcertsList, FirebaseFirestore db, String currentUserId, String currentUserName) {
        this.upcomingConcertsList = upcomingConcertsList;
        this.db = db;
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView concertName, eventTime, quantity;
        public ToggleButton viewMoreButton;
        public View expandableLayout;
        public RecyclerView ticketRecyclerView;

        public ViewHolder(View view) {
            super(view);
            concertName = view.findViewById(R.id.EventTitle);
            eventTime = view.findViewById(R.id.eventDate);
            quantity = view.findViewById(R.id.quantity);
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
    /**
     * Called by RecyclerView to display the data at the specified position. This method updates the contents of the ViewHolder to reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        UpcomingConcert concert = upcomingConcertsList.get(position);
        Log.d(TAG, "Concert data: " + concert.getConcertTitle() + ", " + concert.getEventTime() + ", " + concert.getQuantity() + ", " + concert.getTicketIDs());

        holder.concertName.setText(concert.getConcertTitle() != null ? concert.getConcertTitle() : "No Title");
        holder.eventTime.setText("Concert Date: " + (concert.getEventTime() != null ? concert.getEventTime() : "No Date"));
        holder.quantity.setText("Quantity: " + concert.getQuantity());

        boolean isExpanded = concert.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.viewMoreButton.setText(isExpanded ? "View Less" : "View More");
        holder.viewMoreButton.setChecked(isExpanded);
        holder.viewMoreButton.setOnClickListener(v -> {
            concert.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });

        Log.d(TAG, "Fetching tickets for concert: " + concert.getConcertTitle());
        // Fetch and set tickets
        List<Long> ticketIDs = concert.getTicketIDs();
        if (ticketIDs == null) {
            Log.w(TAG, "No ticket IDs found for concert: " + concert.getConcertTitle());
            return;
        }
        getTicketsFromIDs(ticketIDs, tickets -> {
            Log.d(TAG, "Tickets fetched for concert: " + concert.getConcertTitle() + ", Tickets: " + tickets.size());
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
    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */

    @Override
    public int getItemCount() {
        return upcomingConcertsList.size();
    }


    /**
     * Fetches tickets from the database using the provided ticket IDs and returns them through a callback.
     *
     * @param ticketIDs The list of ticket IDs to fetch.
     * @param listener  The callback to return the fetched tickets.
     */

    // Fetch tickets from UpcomingConcert using the ticketIDs
    private void getTicketsFromIDs(List<Long> ticketIDs, OnTicketsFetchedListener listener) {
        List<Ticket> tickets = new ArrayList<>();
        if (ticketIDs == null) {
            Log.w(TAG, "ticketIDs is null");
            listener.onTicketsFetched(tickets);
            return;
        }
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

                            if (seatCategory != null && seatNumber != null && ticketID != null) {
                                // Create a new Ticket object using the retrieved fields
                                Ticket ticket = new Ticket(seatCategory, seatNumber, ticketID, concertTitle);
                                tickets.add(ticket);
                                Log.d(TAG, "Ticket fetched: " + ticket.getSeatNumber() + ", Ticket ID: " + ticket.getTicketID());
                            } else {
                                Log.d(TAG, "Some fields are missing for ticket with ID: " + (ticketID != null ? ticketID : "unknown"));
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
    /**
     * Callback interface for handling the results of ticket fetching operations.
     * This interface should be implemented to define actions that will be performed once tickets have been fetched from the database.
     */
    public interface OnTicketsFetchedListener {
        void onTicketsFetched(List<Ticket> tickets);
    }
}
