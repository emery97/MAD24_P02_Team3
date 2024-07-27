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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                            removeTicketIDFromUpcomingConcert(ticketID, friend);
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

    private void removeTicketIDFromUpcomingConcert(Long ticketID, User friend) {
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
                                ticketIDs.remove(ticketID);
                                if (ticketIDs.isEmpty()) {
                                    // Delete the entire document if there are no TicketIDs left
                                    db.collection("UpcomingConcert").document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "UpcomingConcert document successfully deleted!");
                                                updateFriendUpcomingConcert(friend, ticketID, concertTitle, "01 August 2024, 16:00");
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error deleting UpcomingConcert document", e));
                                } else {
                                    // Update the document with the new list of TicketIDs
                                    db.collection("UpcomingConcert").document(document.getId())
                                            .update("TicketIDs", ticketIDs)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "TicketID successfully removed from UpcomingConcert!");
                                                updateFriendUpcomingConcert(friend, ticketID, concertTitle, "01 August 2024, 16:00");
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error removing TicketID from UpcomingConcert", e));
                                }
                            } else {
                                Log.w(TAG, "TicketID not found in UpcomingConcert document or ticketIDs is null.");
                            }
                        }
                    } else {
                        Log.w(TAG, "No matching UpcomingConcert document found for update.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error querying UpcomingConcert", e));
    }

    private void updateFriendUpcomingConcert(User friend, Long ticketID, String concertTitle, String eventTime) {
        Log.d(TAG, "Updating friend's UpcomingConcert: " + friend.getUserId());

        db.collection("UpcomingConcert")
                .whereEqualTo("ConcertTitle", concertTitle)
                .whereEqualTo("UserID", friend.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Found friend's UpcomingConcert document: " + document.getId());
                            List<Long> ticketIDs = (List<Long>) document.get("TicketIDs");
                            if (ticketIDs != null) {
                                ticketIDs.add(ticketID);
                                db.collection("UpcomingConcert").document(document.getId())
                                        .update("TicketIDs", ticketIDs)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "TicketID successfully added to friend's UpcomingConcert!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error adding TicketID to friend's UpcomingConcert", e));
                            } else {
                                Log.w(TAG, "TicketIDs list is null in friend's UpcomingConcert document.");
                            }
                        }
                    } else {
                        Log.d(TAG, "No existing UpcomingConcert document found for friend, creating a new one.");
                        Map<String, Object> newConcertData = new HashMap<>();
                        newConcertData.put("ConcertTitle", concertTitle);
                        newConcertData.put("EventTime", eventTime);
                        newConcertData.put("Name", friend.getName());
                        newConcertData.put("Quantity", 1);
                        newConcertData.put("TicketIDs", Arrays.asList(ticketID));
                        newConcertData.put("UserID", friend.getUserId());

                        db.collection("UpcomingConcert").add(newConcertData)
                                .addOnSuccessListener(documentReference -> Log.d(TAG, "New UpcomingConcert document successfully created for friend!"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error creating new UpcomingConcert document for friend", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error querying friend's UpcomingConcert", e));
    }
}
