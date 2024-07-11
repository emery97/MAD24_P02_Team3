package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private static final String TAG = "FriendsAdapter";
    private List<User> friendsList;
    private Context context;

    // Constructor
    public FriendsAdapter(Context context, List<User> friendsList) {
        this.context = context;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the friend item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendsList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding friend at position " + position + " with name " + friend.getName());

        // Bind data to the views
        holder.friendName.setText(friend.getName());

        // Load profile image using Glide or any other image loading library
        if (friend.getProfileImageUrl() != null && !friend.getProfileImageUrl().isEmpty()) {
            Glide.with(context).load(friend.getProfileImageUrl()).into(holder.profilePicture);
            Log.d(TAG, "Profile picture URL: " + friend.getProfileImageUrl());
        } else {
            holder.profilePicture.setImageResource(R.drawable.profileimage); // Set a default image if no URL is provided
            Log.d(TAG, "No profile picture URL, using default image.");
        }

        // Set click listener for the transfer button
        holder.friendTransferButton.setOnClickListener(v -> {
            // Handle transfer button click
            Log.d(TAG, "Transfer button clicked for friend: " + friend.getName());
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: friendsList size = " + friendsList.size());
        return friendsList.size();
    }

    // ViewHolder class
    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicture;
        TextView friendName;
        Button friendTransferButton;

        public FriendViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            friendName = itemView.findViewById(R.id.friendName);
            friendTransferButton = itemView.findViewById(R.id.friendTransferButton);
        }
    }
}

