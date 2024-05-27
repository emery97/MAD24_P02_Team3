package sg.edu.np.mad.TicketFinder;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackViewHolder> {
    private List<Uri> imageUris;

    public FeedbackAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback_image, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.imageView.setImageURI(imageUri);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }
}
