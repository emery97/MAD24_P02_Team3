package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> userList; // List of all users
    private List<User> filteredList; // Holds filtered users based on search
    private LayoutInflater mInflater;
    private Context context;
    private FirebaseFirestore db;
    private OnFriendAddListener friendAddListener; // Listener for friend addition
    private static String TAG = "userAdapter";

    // Constructor to initialize data
    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList); // Initially, filteredList contains all users
        db = FirebaseFirestore.getInstance();
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
        String userName = filteredList.get(position).getName();
        User user = filteredList.get(position);
        holder.myTextView.setText(userName);

        // Clear imageview before loading new image
        Glide.with(context).clear(holder.profilePicture);

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()){
            Glide.with(context).load(user.getProfileImageUrl()).into(holder.profilePicture);
        }else{
            holder.profilePicture.setImageResource(R.drawable.profileimage);
        }

        holder.addFriendButton.setOnClickListener(v ->
                showTransferDialog(holder.itemView.getContext(), user)
        );

    }

    // Setter for friendAddListener
    public void setOnFriendAddListener(OnFriendAddListener listener) {
        this.friendAddListener = listener;
    }

    // Method to show all users
    public void show(List<User> userList) {
        this.filteredList = new ArrayList<>(userList);
        notifyDataSetChanged();
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
                    Log.d(TAG, "filter: "+filteredList);
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }

    // Total number of rows
    @Override
    public int getItemCount() {
        return filteredList.size(); // Use filteredList
    }

    private void showTransferDialog(Context context, User friend) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Add Friend")
                .setMessage("Do you want to add " + friend.getName() + "as your friend?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    Toast.makeText(context, friend.getName() + "is your friend now !!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();
        dialog.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView myTextView;
        Button addFriendButton;

        public ViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            myTextView = itemView.findViewById(R.id.friendName);
            addFriendButton = itemView.findViewById(R.id.addFriendButton);

        }
    }
    public interface OnFriendAddListener {
        void onFriendAdded(User user);
    }
}
