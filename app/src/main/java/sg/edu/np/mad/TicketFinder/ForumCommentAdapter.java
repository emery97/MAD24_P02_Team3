package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class ForumCommentAdapter extends RecyclerView.Adapter<ForumCommentAdapter.ForumCommentViewHolder> {

    private List<ForumComment> comments;

    public ForumCommentAdapter(List<ForumComment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ForumCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forumcomment, parent, false);
        return new ForumCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumCommentViewHolder holder, int position) {
        ForumComment comment = comments.get(position);
        holder.commentTextView.setText(comment.getComment());
        holder.nameTextview.setText(comment.getName());

        if (comment.getProfilePicUrl() == null || comment.getProfilePicUrl().isEmpty()) {
            holder.profileimageview.setImageResource(R.drawable.profileimage); // Default image
        } else {
            FirebaseStorage.getInstance().getReferenceFromUrl(comment.getProfilePicUrl()).getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(holder.itemView.getContext())
                                .load(uri)
                                .apply(new RequestOptions().circleCrop())
                                .placeholder(R.drawable.profileimage) // Optional: Placeholder while loading
                                .into(holder.profileimageview);
                    })
                    .addOnFailureListener(e -> {
                        holder.profileimageview.setImageResource(R.drawable.profileimage); // Fallback to default image
                    });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ForumCommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentTextView, nameTextview;
        ImageView profileimageview;

        public ForumCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
            nameTextview = itemView.findViewById(R.id.name_text_view);
            profileimageview = itemView.findViewById(R.id.profile_image_view);
        }
    }
}
