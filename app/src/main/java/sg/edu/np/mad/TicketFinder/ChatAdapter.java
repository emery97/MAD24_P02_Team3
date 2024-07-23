package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_BOT = 1;
    private static final int VIEW_TYPE_EVENT = 2;

    private final Context context;
    private final ArrayList<Message> messageList;

    public ChatAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(context).inflate(R.layout.user_message, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == VIEW_TYPE_BOT) {
            view = LayoutInflater.from(context).inflate(R.layout.bot_message, parent, false);
            return new BotViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_event, parent, false);
            return new EventViewHolder(view); // Use the correct layout for events
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder.getItemViewType() == VIEW_TYPE_BOT) {
            ((BotViewHolder) holder).bind(message);
        } else {
            ((EventViewHolder) holder).bind(message.getEventList());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else if (message.getEventList() != null) {
            return VIEW_TYPE_EVENT;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView userMessage;
        private final de.hdodenhof.circleimageview.CircleImageView profileImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.user_message);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }

        public void bind(Message message) {
            userMessage.setText(message.getMessage());

            // ----------- Start of changes -----------
            // Retrieve the profile picture URL from SharedPreferences
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String profilePicUrl = sharedPreferences.getString("ProfilePicUrl", null);

            // Load the profile picture using Glide
            if (profilePicUrl != null) {
                Glide.with(context)
                        .load(profilePicUrl)
                        .circleCrop()
                        .into(profileImageView);
            }
            // ----------- End of changes -----------
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        private final TextView botMessage;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMessage = itemView.findViewById(R.id.bot_message);
        }

        public void bind(Message message) {
            botMessage.setText(message.getMessage());
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView eventRecyclerView;
        private final EventAdapter eventAdapter; // Adapter for the inner RecyclerView

        public EventViewHolder(View itemView) {
            super(itemView);
            eventRecyclerView = itemView.findViewById(R.id.eventRecyclerView);
            eventAdapter = new EventAdapter(itemView.getContext(), new ArrayList<>(), false);
            eventRecyclerView.setAdapter(eventAdapter);
            eventRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        public void bind(List<Event> events) {
            eventAdapter.setSearchList(new ArrayList<>(events)); // Update the adapter with the list of events
        }
    }
}
