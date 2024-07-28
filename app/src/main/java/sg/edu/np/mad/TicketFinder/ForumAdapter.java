package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {

    private List<Forum> forumList;
    private FirebaseFirestore db;
    private ForumPage forumPage;

    public ForumAdapter(List<Forum> forumList) {
        this.forumList = forumList;
        this.forumPage = forumPage;
        db = FirebaseFirestore.getInstance();
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
        if (forum.getImageUrls() != null && !forum.getImageUrls().isEmpty()) {
            holder.arrowLeft.setVisibility(View.VISIBLE);
            holder.arrowRight.setVisibility(View.VISIBLE);
        } else {
            holder.arrowLeft.setVisibility(View.GONE);
            holder.arrowRight.setVisibility(View.GONE);
        }

        final int scrollAmount = 200; // Adjust this value based on your needs
        // left scroll
        holder.arrowLeft.setOnClickListener(v -> {
            holder.imagesRecyclerView.smoothScrollBy(-scrollAmount, 0);
        });
        // Right scroll
        holder.arrowRight.setOnClickListener(v -> {
            holder.imagesRecyclerView.smoothScrollBy(scrollAmount, 0);
        });


        holder.commentsButton.setOnClickListener(v -> {
            CommentsBottomSheetFragment bottomSheet = new CommentsBottomSheetFragment();
            Bundle args = new Bundle();
            args.putString("documentId", forum.getDocumentId());
            bottomSheet.setArguments(args);
            bottomSheet.show(((FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    @Override
    public int getItemCount() {
        return forumList.size();
    }


    public void updateList(List<Forum> newList) {
        forumList = newList;
        notifyDataSetChanged();
    }
    public void filterList(List<Forum> filteredList) {
        this.forumList = filteredList;
        notifyDataSetChanged();
    }
    private String getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserId", null); // Default to null if not found
    }

    // Deleting data from database and recyclerview
    public void onDeleteClicked(int position, Context context) {
        Forum forumToRemove = forumList.get(position);
        String postUserId = forumToRemove.getUserId(); // Assume `Forum` has a `getUserId` method

        // Retrieve current user's ID from SharedPreferences
        String currentUserId = getUserId(context);

        if (currentUserId != null && currentUserId.equals(postUserId)) {
            // User is allowed to delete the post
            String documentId = forumToRemove.getDocumentId();

            // Delete from Firestore
            db.collection("Forum").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ForumAdapter", "DocumentSnapshot successfully deleted!");
                        forumList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> Log.w("ForumAdapter", "Error deleting document", e));
        } else {
            // User is not allowed to delete the post
            Toast.makeText(context, "You can only delete your own posts.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onEditClicked(int position) {
        // Handle edit action
        Forum forumToEdit = forumList.get(position);
        // Implement your edit logic
    }
    public List<Forum> getForumList() {
        return forumList;
    }

    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, messageTextView, eventTextView, emailTextView,timestampTextView;
        ImageView profileImageView;
        RecyclerView imagesRecyclerView;
        Button commentsButton;
        ImageButton arrowLeft, arrowRight;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.forum_name);
            messageTextView = itemView.findViewById(R.id.forum_message);
            eventTextView = itemView.findViewById(R.id.forum_event);
            timestampTextView = itemView.findViewById(R.id.forum_timestamp);
            profileImageView = itemView.findViewById(R.id.profile_image);
            imagesRecyclerView = itemView.findViewById(R.id.image_recycler_view);
            commentsButton = itemView.findViewById(R.id.commentsbutton);
            arrowLeft = itemView.findViewById(R.id.arrow_left);
            arrowRight = itemView.findViewById(R.id.arrow_right);
        }
    }
}
