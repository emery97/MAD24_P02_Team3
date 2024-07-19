package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> userList; // List of all users
    private List<User> filteredList; // Holds filtered users based on search
    private LayoutInflater mInflater;
    private OnFriendAddListener friendAddListener; // Listener for friend addition

    // Interface for friend addition callback
    public interface OnFriendAddListener {
        void onFriendAdded(User user);
    }

    // Setter for friendAddListener
    public void setOnFriendAddListener(OnFriendAddListener listener) {
        this.friendAddListener = listener;
    }

    // Constructor to initialize data
    public UserAdapter(Context context, List<User> userList) {
        this.mInflater = LayoutInflater.from(context);
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList); // Initially, filteredList contains all users
    }

    // Inflate the row layout when needed
    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_find_friend, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        String user = filteredList.get(position).getName(); // Use filteredList
        holder.myTextView.setText(user);
    }

    // Total number of rows
    @Override
    public int getItemCount() {
        return filteredList.size(); // Use filteredList
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        Button addFriendButton;

        public ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.friendName);
            addFriendButton = itemView.findViewById(R.id.addFriendButton);

            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        User selectedUser = filteredList.get(position); // Get user from filteredList
                        // Notify listener that a friend is added
                        if (friendAddListener != null) {
                            friendAddListener.onFriendAdded(selectedUser);
                            Toast.makeText(v.getContext(), "Friend added successfully", Toast.LENGTH_SHORT).show(); // Show toast message
                        }
                    }
                }
            });
        }
    }

    // Method to update the filtered list based on the query
    public void filter(String query) {
        filteredList.clear(); // Clear the previous filtered list
        if (query.trim().isEmpty()) {
            filteredList.addAll(userList); // If query is empty, show all users
        } else {
            query = query.toLowerCase();
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(query)) {
                    filteredList.add(user); // Add user to filtered list if name matches query
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }
}
