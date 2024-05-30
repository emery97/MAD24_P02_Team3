package sg.edu.np.mad.TicketFinder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class FeedbackDetailsViewHolder extends RecyclerView.ViewHolder {
    TextView categoryTextView;
    TextView messageTextView;
    ImageView imageView;

    public FeedbackDetailsViewHolder(View itemView) {
        super(itemView);
        categoryTextView = itemView.findViewById(R.id.feedbackcategorydetails);
        messageTextView = itemView.findViewById(R.id.feedbackmessagedetails);
        imageView = itemView.findViewById(R.id.feedbackimagedetails);
    }
}
