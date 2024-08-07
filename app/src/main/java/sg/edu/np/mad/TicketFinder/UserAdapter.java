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
import com.google.firebase.firestore.FieldValue;
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
    private String currentUserId;

    // Constructor to initialize data
    public UserAdapter(Context context, List<User> userList, String currentUserId) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList); // Initially, filteredList contains all users
        this.currentUserId = currentUserId;
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
                addFriendDialogue(holder.itemView.getContext(), user)
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

    private void addFriendDialogue(Context context, User friend) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Add Friend")
                .setMessage("Do you want to add " + friend.getName() + " as your friend?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    if(checkIfCanAddAsFriend(friend) == true){
                        addFriendToCurrentUser(friend.getUserId());
                        Toast.makeText(context, friend.getName() + " is your friend now !!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();
        dialog.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

    private boolean checkIfCanAddAsFriend( User friend){
        Log.d(TAG, "checkIfCanAddAsFriend: " +friend.getUserId() +" "+ currentUserId );
        if(friend.getUserId().equals(currentUserId)){
            Log.d(TAG, "checkIfCanAddAsFriend: LOLLLLS");
            Toast.makeText(context, "You cannot add yourself as a friend, sorry if that bites :( ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
    public void addFriendToCurrentUser(String friendUserId) {
        Log.d(TAG, "addFriendToCurrentUser: Adding friend " + friendUserId + " to user " + currentUserId);
        db.collection("Account")
                .whereEqualTo("userId", Integer.parseInt(currentUserId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("Account").document(document.getId())
                                    .update("friends", FieldValue.arrayUnion(friendUserId))
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "addFriendToCurrentUser: DocumentSnapshot successfully updated!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "addFriendToCurrentUser: Error updating document", e));
                        }
                    } else {
                        Log.w(TAG, "addFriendToCurrentUser: No matching documents found or task not successful");
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "addFriendToCurrentUser: Error getting documents", e));
    }

}
