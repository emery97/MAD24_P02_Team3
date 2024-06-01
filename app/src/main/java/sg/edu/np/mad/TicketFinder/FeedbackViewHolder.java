package sg.edu.np.mad.TicketFinder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class FeedbackViewHolder extends RecyclerView.ViewHolder {
    //get view for feedback details image item
    public ImageView imageView;

    public FeedbackViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }
}