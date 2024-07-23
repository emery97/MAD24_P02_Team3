package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {

    private List<Forum> forumList;

    public ForumAdapter(List<Forum> forumList) {
        this.forumList = forumList;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forummessage, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, int position) {
        Forum forum = forumList.get(position);

        holder.nameTextView.setText(forum.getName());
        holder.messageTextView.setText(forum.getMessage());
        holder.eventTextView.setText(forum.getEvent());

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM", Locale.getDefault());
        String formattedDate = sdf.format(new Date(forum.getTimestamp()));
        holder.timestampTextView.setText(formattedDate);

        if (forum.getProfilePicUrl() == null || forum.getProfilePicUrl().isEmpty()) {
            holder.profileImageView.setImageResource(R.drawable.profileimage); // Default image
        } else {
            FirebaseStorage.getInstance().getReferenceFromUrl(forum.getProfilePicUrl()).getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(holder.itemView.getContext())
                                .load(uri)
                                .apply(new RequestOptions().circleCrop())
                                .placeholder(R.drawable.profileimage) // Optional: Placeholder while loading
                                .into(holder.profileImageView);
                    })
                    .addOnFailureListener(e -> {
                        holder.profileImageView.setImageResource(R.drawable.profileimage); // Fallback to default image
                    });
        }

        ForumImageAdapter imageAdapter = new ForumImageAdapter(forum.getImageUrls());
        holder.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.imagesRecyclerView.setAdapter(imageAdapter);
    }

    @Override
    public int getItemCount() {
        return forumList.size();
    }

    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, messageTextView, eventTextView, emailTextView,timestampTextView;
        ImageView profileImageView;
        RecyclerView imagesRecyclerView;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.forum_name);
            messageTextView = itemView.findViewById(R.id.forum_message);
            eventTextView = itemView.findViewById(R.id.forum_event);
            timestampTextView = itemView.findViewById(R.id.forum_timestamp);
            profileImageView = itemView.findViewById(R.id.profile_image);
            imagesRecyclerView = itemView.findViewById(R.id.image_recycler_view);
        }
    }
}
