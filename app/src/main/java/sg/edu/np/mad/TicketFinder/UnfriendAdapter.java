package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.DialogInterface;
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

import java.text.BreakIterator;
import java.util.List;

public class UnfriendAdapter extends RecyclerView.Adapter<UnfriendAdapter.ViewHolder> {
    private final Context context;
    private final LayoutInflater mInflater;
    private FirebaseFirestore db;
    private List<User> friendList;
    private static String TAG = "unfriendAdapter";
    private String currentUserId;


    //Constructor to initialize data
    public UnfriendAdapter(Context context, List<User> friendList , String currentUserId){
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.friendList = friendList;
        db = FirebaseFirestore.getInstance();
        this.currentUserId = currentUserId;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        User friend = friendList.get(position);
        holder.name.setText(friend.getName());

        // Clear imageview before loading new image
        Glide.with(context).clear(holder.profilePicture);

        if (friend.getProfileImageUrl() != null && !friend.getProfileImageUrl().isEmpty()){
            Glide.with(context).load(friend.getProfileImageUrl()).into(holder.profilePicture);
        }else{
            holder.profilePicture.setImageResource(R.drawable.profileimage);
        }

        holder.unfriendButton.setOnClickListener(v ->
                unfriendFriendDialogue(holder.itemView.getContext(), friend.getUserId())
        );
    }
    @Override
    public int getItemCount(){
        return friendList.size();
    }
    // Inflate the row layout when needed
    @NonNull
    @Override
    public UnfriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = mInflater.inflate(R.layout.unfriend_item,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        public BreakIterator textViewName;
        ImageView profilePicture;
        TextView name;
        Button unfriendButton;
        public ViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            name = itemView.findViewById(R.id.friendName);
            unfriendButton = itemView.findViewById(R.id.unfriendButton);
        }
    }

    private void unfriendFriendDialogue(Context context, String userId) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Unfriend Friend")
                .setMessage("Do you want to unfriend this user?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    // Remove the friend from the database
                    db.collection("Account")
                            .whereEqualTo("userId", Long.parseLong(currentUserId)) // Use currentUserId
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        db.collection("Account").document(document.getId())
                                                .update("friends", FieldValue.arrayRemove(userId))
                                                .addOnSuccessListener(aVoid -> {
                                                    // Remove the friend from the friendList and notify the adapter
                                                    for (int i = 0; i < friendList.size(); i++) {
                                                        if (friendList.get(i).getUserId().equals(userId)) {
                                                            friendList.remove(i);
                                                            notifyDataSetChanged();
                                                            break;
                                                        }
                                                    }
                                                    Toast.makeText(context, "Unfriended successfully", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to unfriend user", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG, "Error removing friend: ", e);
                                                });
                                    }
                                } else {
                                    Log.e(TAG, "No matching documents found or task not successful");
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error getting documents", e));
                })
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();
        dialog.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

}
