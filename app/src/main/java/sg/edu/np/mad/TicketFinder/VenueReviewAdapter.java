package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class VenueReviewAdapter extends RecyclerView.Adapter<VenueReviewAdapter.VenueReviewViewHolder> {
    private List<PlacesResponse.Review> reviewsList;
    public VenueReviewAdapter(List<PlacesResponse.Review> reviewsList) {
        this.reviewsList = reviewsList;
    }

    public class VenueReviewViewHolder extends RecyclerView.ViewHolder{
        TextView reviewAuthor;
        TextView reviewRating;
        TextView reviewDate;
        TextView reviewText;

        public VenueReviewViewHolder(View itemView) {
            super(itemView);
            reviewAuthor = itemView.findViewById(R.id.reviewAuthor);
            reviewRating = itemView.findViewById(R.id.reviewRating);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            reviewText = itemView.findViewById(R.id.reviewText);
        }
    }
    @NonNull
    @Override
    public VenueReviewAdapter.VenueReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venue_reviews, parent, false);
        return new VenueReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueReviewAdapter.VenueReviewViewHolder holder, int position) {
        PlacesResponse.Review review = reviewsList.get(position);
        holder.reviewAuthor.setText(review.author_name);
        holder.reviewDate.setText(review.relative_time_description);
        holder.reviewRating.setText("Rating: " + review.rating + "/5");
        holder.reviewText.setText(review.text);

    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }
}
