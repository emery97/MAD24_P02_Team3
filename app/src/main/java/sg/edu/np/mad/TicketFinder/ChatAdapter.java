package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.isUser()) {
            holder.userMessageContainer.setVisibility(View.VISIBLE);
            holder.userIcon.setVisibility(View.VISIBLE);
            holder.botMessageContainer.setVisibility(View.GONE);
            holder.botIcon.setVisibility(View.GONE);
            holder.userMessage.setText(message.getText());
        } else {
            holder.botMessageContainer.setVisibility(View.VISIBLE);
            holder.botIcon.setVisibility(View.VISIBLE);
            holder.userMessageContainer.setVisibility(View.GONE);
            holder.userIcon.setVisibility(View.GONE);
            holder.botMessage.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, botMessage;
        View userMessageContainer, botMessageContainer;
        ImageView botIcon, userIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessage);
            botMessage = itemView.findViewById(R.id.botMessage);
            userMessageContainer = itemView.findViewById(R.id.userMessageContainer);
            botMessageContainer = itemView.findViewById(R.id.botMessageContainer);
            botIcon = itemView.findViewById(R.id.botIcon);
            userIcon = itemView.findViewById(R.id.userIcon);
        }
    }
}
