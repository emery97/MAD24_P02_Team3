package sg.edu.np.mad.TicketFinder;

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

public class TransferTicketsAdapter extends RecyclerView.Adapter<TransferTicketsAdapter.ViewHolder> {
    private List<User> userList;

    public TransferTicketsAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        Glide.with(holder.itemView.getContext())
                .load(user.getProfileImageUrl())
                .into(holder.profileImageView);

        // Set up Transfer button click listener if needed
        holder.transferButton.setOnClickListener(v -> {
            // Handle transfer logic here
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImageView;
        public TextView nameTextView;
        public Button transferButton;

        public ViewHolder(View view) {
            super(view);
            profileImageView = view.findViewById(R.id.profilePicture);
            nameTextView = view.findViewById(R.id.textView2);
            transferButton = view.findViewById(R.id.transferTickets);
        }
    }
}
