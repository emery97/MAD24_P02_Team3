package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Message> messageList;

    public ChatAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.bot_message, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.isUser()) {
            ((UserViewHolder) holder).userMessage.setText(message.getMessage());
        } else {
            ((BotViewHolder) holder).botMessage.setText(message.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).isUser()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView userMessage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.user_message);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder {

        TextView botMessage;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMessage = itemView.findViewById(R.id.bot_message);
        }
    }
}
