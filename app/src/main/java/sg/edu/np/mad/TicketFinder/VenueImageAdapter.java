package sg.edu.np.mad.TicketFinder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import android.content.Context;


public class VenueImageAdapter extends RecyclerView.Adapter<VenueImageAdapter.VenueImageViewHolder> {
    private List<PlacesResponse.Photo> photosList;
    private Context context;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String API_KEY = "AIzaSyAtrYJH3VUAJgo-qhxicKkjihd8pPSuEII";
    public VenueImageAdapter(Context context, List<PlacesResponse.Photo> photosList) {
        this.context = context;
        this.photosList = photosList;
    }

    public class VenueImageViewHolder extends RecyclerView.ViewHolder{
        ImageView venueImage;

        public VenueImageViewHolder(View itemView) {
            super(itemView);
            venueImage = itemView.findViewById(R.id.photoImageView);
        }
    }
    @NonNull
    @Override
    public VenueImageAdapter.VenueImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venue_image, parent, false);
        return new VenueImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueImageAdapter.VenueImageViewHolder holder, int position) {
        PlacesResponse.Photo photo = photosList.get(position);
        String photoUrl = BASE_URL + "place/photo?maxwidth=400&photoreference=" + photo.photo_reference + "&key=" + API_KEY;
        Glide.with(context)
                .load(photoUrl)
                .into(holder.venueImage);

    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }
}
