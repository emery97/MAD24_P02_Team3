//package sg.edu.np.mad.TicketFinder;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
//
//    private ArrayList<Event> eventList;
//    private Context context;
//    private boolean isGrid; // Flag to indicate grid layout
//
//    // Single constructor to handle both list and grid layouts
//    public EventAdapter(Context context, ArrayList<Event> eventList, boolean isGrid) {
//        this.context = context;
//        this.eventList = eventList;
//        this.isGrid = isGrid;
//    }
//
//    // shows filtered event list
//    public void setSearchList(ArrayList<Event> searchList) {
//        this.eventList = searchList; // sets this eventlist as the searchlist
//        notifyDataSetChanged(); // alert
//    }
//
//    //get views from xml
//    public static class EventViewHolder extends RecyclerView.ViewHolder {
//        public ConstraintLayout eventCard;
//        public ImageView eventImage;
//        public TextView eventTitle;
//        public TextView eventArtist;
//        public TextView eventDate;
//
//        public EventViewHolder(View itemView) {
//            super(itemView);
//            eventCard = itemView.findViewById(R.id.eventCard);
//            eventImage = itemView.findViewById(R.id.eventImage);
//            eventTitle = itemView.findViewById(R.id.eventTitle);
//            eventArtist = itemView.findViewById(R.id.eventArtist);
//            eventDate = itemView.findViewById(R.id.eventDate);
//        }
//    }
//
//    // Default constructor to maintain compatibility with existing code
//    public EventAdapter(Context context, ArrayList<Event> eventList) {
//        this(context, eventList, false); // default to list layout
//    }
//
//    @Override
//    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView;
//        if (isGrid) {
//            // Inflate grid layout item
//            itemView = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_event_grid, parent, false);
//        } else {
//            // Inflate default layout item
//            itemView = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_event, parent, false);
//        }
//        return new EventViewHolder(itemView);
//    }
//
//    // putting data in each item_event / item_event_grid xml
//    @Override
//    public void onBindViewHolder(EventViewHolder holder, int position) {
//        // get event
//        Event event = eventList.get(position);
//
//        // format date
//        LocalDate eventObjDate = event.getDate();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
//        String formattedDate = eventObjDate.format(formatter);
//
//        // set values
//        holder.eventTitle.setText(event.getTitle());
//        holder.eventArtist.setText(event.getArtist());
//        holder.eventDate.setText(formattedDate);
//        // set image with Glide
//        Glide.with(context)
//                .load(event.getImgUrl())
//                .into(holder.eventImage);
//
//        // when clicking on event, sends to the event's respective eventDetails page
//        holder.eventCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, EventDetails.class);
//                intent.putExtra("event", event); // sending event data
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return eventList.size();
//    }
//}

package sg.edu.np.mad.TicketFinder;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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

    // shows filtered event list
    public void setSearchList(ArrayList<Event> searchList) {
        this.eventList = searchList; // sets this eventlist as the searchlist
        notifyDataSetChanged(); // alert
    }

    //get views from xml
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        //public ConstraintLayout eventCard;

        public CardView eventCard; // CHANGE: Use CardView instead of ConstraintLayout

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

    // putting data in each item_event / item_event_grid xml
    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        // get event
        Event event = eventList.get(position);

        // format date
        LocalDate eventObjDate = event.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        String formattedDate = eventObjDate.format(formatter);

        // set values
        holder.eventTitle.setText(event.getTitle());
        holder.eventArtist.setText(event.getArtist());
        holder.eventDate.setText(formattedDate);
        // set image with Glide
        Glide.with(context)
                .load(event.getImgUrl())
                .into(holder.eventImage);

        // when clicking on event, sends to the event's respective eventDetails page
        holder.eventCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventDetails.class);
                intent.putExtra("event", event); // sending event data
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}