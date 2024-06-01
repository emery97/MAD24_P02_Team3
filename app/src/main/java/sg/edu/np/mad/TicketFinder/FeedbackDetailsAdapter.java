package sg.edu.np.mad.TicketFinder;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FeedbackDetailsAdapter extends RecyclerView.Adapter<FeedbackDetailsViewHolder> {
    //displaying feedback details, including image
    private List<Feedbackclass> feedbackList;

    //Constructor
    public FeedbackDetailsAdapter(List<Feedbackclass> feedbackList) {
        this.feedbackList = feedbackList;
    }

    //get views from xml
    @Override
    public FeedbackDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewfeedbackrecycler, parent, false);
        return new FeedbackDetailsViewHolder(view);
    }

    // putting data xml
    @Override
    public void onBindViewHolder(FeedbackDetailsViewHolder holder, int position) {
        //get feedback
        Feedbackclass feedbackclass = feedbackList.get(position);

        // set data
        holder.categoryTextView.setText(feedbackclass.getCategory());
        holder.messageTextView.setText(feedbackclass.getMessage());

        // Load image using Glide
        if (feedbackclass.getImageURIs() != null && !feedbackclass.getImageURIs().isEmpty()) {
            String imageUrl = feedbackclass.getImageURIs().get(0);
            Uri contentUri = Uri.parse(imageUrl);

            Glide.with(holder.imageView.getContext())
                    .load(contentUri)
                    .into(holder.imageView);
        } else { // If no image, don't display
            holder.imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }
}
