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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private static final String TAG = "FriendsAdapter";
    private List<User> friendsList;
    private Context context;
    private FirebaseFirestore db;
    private String concertTitle;
    private String seatCategory;
    private String seatNumber;
    private String currentUserId;
    private String currentUserName;

    public FriendsAdapter(Context context, List<User> friendsList, String concertTitle, String seatCategory, String seatNumber, String currentUserId, String currentUserName) {
        this.context = context;
        this.friendsList = friendsList;
        this.concertTitle = concertTitle;
        this.seatCategory = seatCategory;
        this.seatNumber = seatNumber;
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendsList.get(position);

        holder.friendName.setText(friend.getName());

        if (friend.getProfileImageUrl() != null && !friend.getProfileImageUrl().isEmpty()) {
            Glide.with(context).load(friend.getProfileImageUrl()).into(holder.profilePicture);
        } else {
            holder.profilePicture.setImageResource(R.drawable.profileimage); // Set a default image if no URL is provided
        }

        holder.friendTransferButton.setOnClickListener(v -> showTransferDialog(holder.itemView.getContext(), friend));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

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

    private void showTransferDialog(Context context, User friend) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Transfer Ticket")
                .setMessage("Transfer ticket to " + friend.getName() + "?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    Toast.makeText(context, "Ticket transferred to " + friend.getName(), Toast.LENGTH_SHORT).show();
                    transferTicketFromDatabase(friend);
                })
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();
        dialog.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

    private void transferTicketFromDatabase(User friend) {
        Log.d(TAG, "Querying with concertTitle: " + concertTitle + ", seatCategory: " + seatCategory + ", seatNumber: " + seatNumber);

        db.collection("Ticket")
                .whereEqualTo("ConcertTitle", concertTitle)
                .whereEqualTo("SeatCategory", seatCategory)
                .whereEqualTo("SeatNumber", seatNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Found document: " + document.getId());
                            Long ticketID = document.getLong("TicketID"); // Fetch the actual ticketID field
                            if (ticketID != null) {
                                db.collection("Ticket").document(document.getId())
                                        .update("Name", friend.getName(), "userId", friend.getUserId())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            removeTicketIDFromUpcomingConcert(ticketID);
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
                            } else {
                                Log.w(TAG, "No ticketID found in the document");
                            }
                        }
                    } else {
                        Log.w(TAG, "No matching document found for update.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error querying tickets", e));
    }

    private void removeTicketIDFromUpcomingConcert(Long ticketID) {
        Log.d(TAG, "Removing ticketID: " + ticketID + " from UpcomingConcert with concertTitle: " + concertTitle);

        db.collection("UpcomingConcert")
                .whereEqualTo("ConcertTitle", concertTitle)
                .whereEqualTo("UserID", currentUserId) // Ensure we are removing from the current user's document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Found UpcomingConcert document: " + document.getId());
                            List<Long> ticketIDs = (List<Long>) document.get("TicketIDs");
                            if (ticketIDs != null && ticketIDs.contains(ticketID)) {
                                Log.d(TAG, "TicketID found in UpcomingConcert document. Removing it.");
                                ticketIDs.remove(ticketID);
                                db.collection("UpcomingConcert").document(document.getId())
                                        .update("TicketIDs", ticketIDs)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "TicketID successfully removed from UpcomingConcert!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error removing TicketID from UpcomingConcert", e));
                            } else {
                                Log.w(TAG, "TicketID not found in UpcomingConcert document.");
                            }
                        }
                    } else {
                        Log.w(TAG, "No matching UpcomingConcert document found for update.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error querying UpcomingConcert", e));
    }


}
