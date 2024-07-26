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
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Ticket> ticketList;
    private static final String TAG = "TicketAdapter";

    public TicketAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        Ticket ticket = ticketList.get(position);
        holder.seatCat.setText("Seat Category: " + ticket.getSeatCategory());
        holder.seatNum.setText("Seat Number: " + ticket.getSeatNumber());

        holder.transferTicketsButton.setOnClickListener(v -> {
            Log.d(TAG, "Transfer button clicked for Seat Category: " + ticket.getSeatCategory() + ", Seat Number: " + ticket.getSeatNumber());
            Intent intent = new Intent(holder.itemView.getContext(), TransferTicketsActivity.class);
            intent.putExtra("SEAT_CATEGORY", ticket.getSeatCategory());
            intent.putExtra("SEAT_NUMBER", ticket.getSeatNumber());
            intent.putExtra("CONCERT_NAME",ticket.getConcertTitle());
            holder.itemView.getContext().startActivity(intent);
        });

        Log.d(TAG, "Ticket data set for position: " + position + ", Seat Category: " + ticket.getSeatCategory() + ", Seat Number: " + ticket.getSeatNumber());
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount called, size: " + ticketList.size());
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView seatCat;
        TextView seatNum;
        Button transferTicketsButton;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            seatCat = itemView.findViewById(R.id.seatCat);
            seatNum = itemView.findViewById(R.id.seatNum);
            transferTicketsButton = itemView.findViewById(R.id.transferTickets);
            Log.d(TAG, "TicketViewHolder initialized");
        }
    }
}
