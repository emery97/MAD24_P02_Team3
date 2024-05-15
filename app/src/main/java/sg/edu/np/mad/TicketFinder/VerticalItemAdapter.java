package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VerticalItemAdapter extends RecyclerView.Adapter<VerticalItemAdapter.VerticalItemViewHolder> {

    private ArrayList<Event> recoList;

    public static class VerticalItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView eventImage;
        public TextView eventTitle;
        public TextView eventArtist;
        public TextView eventDate;

        public VerticalItemViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventArtist = itemView.findViewById(R.id.eventArtist);
            eventDate = itemView.findViewById(R.id.eventDate);
        }
    }

    public VerticalItemAdapter(ArrayList<Event> recoList) {
        this.recoList = recoList;
    }

    @NonNull
    @Override
    public VerticalItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new VerticalItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VerticalItemViewHolder holder, int position) {
        Event event = recoList.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventArtist.setText(event.getArtist());
        holder.eventDate.setText(event.getDate());
        holder.eventImage.setImageResource(event.getImageResId());  // Assuming images are drawable resources
    }

    @Override
    public int getItemCount() {
        return recoList.size();
    }
}
