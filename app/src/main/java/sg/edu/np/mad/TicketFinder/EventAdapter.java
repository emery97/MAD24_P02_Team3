package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<Event> eventList;
    private Context context;
    private boolean isGrid; // Flag to indicate grid layout

    // Single constructor to handle both list and grid layouts
    public EventAdapter(Context context, ArrayList<Event> eventList, boolean isGrid) {
        this.context = context;
        this.eventList = eventList;
        this.isGrid = isGrid;
    }


    public void setSearchList(ArrayList<Event> searchList) {
        this.eventList = searchList;
        notifyDataSetChanged();
    }

    public void clear() {
        eventList.clear();
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout eventCard;
        public ImageView eventImage;
        public TextView eventTitle;
        public TextView eventArtist;
        public TextView eventDate;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventCard = itemView.findViewById(R.id.eventCard);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventArtist = itemView.findViewById(R.id.eventArtist);
            eventDate = itemView.findViewById(R.id.eventDate);
        }
    }

    // Default constructor to maintain compatibility with existing code
    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this(context, eventList, false); // default to list layout
    }
    /*
    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }*/

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (isGrid) {
            // Inflate grid layout item
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_grid, parent, false);
        } else {
            // Inflate default layout item
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event, parent, false);
        }
        return new EventViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // format date
        LocalDate eventObjDate = event.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        String formattedDate = eventObjDate.format(formatter);

        // set text
        holder.eventTitle.setText(event.getTitle());
        holder.eventArtist.setText(event.getArtist());
        holder.eventDate.setText(formattedDate);
        Glide.with(context)
                .load(event.getImgUrl())
                .into(holder.eventImage);

        holder.eventCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventDetails.class);
                intent.putExtra("event", event);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}