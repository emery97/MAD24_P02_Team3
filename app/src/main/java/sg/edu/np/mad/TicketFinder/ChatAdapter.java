package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_EVENT = 1;

    private ArrayList<Message> messageList;
    private Context context;

    public ChatAdapter(ArrayList<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.isEvent()) {
            return TYPE_EVENT;
        } else {
            return TYPE_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EVENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_event, parent, false);
            return new EventViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bind(message.getEvent());
        } else {
            ((MessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView botMessage;
        TextView userMessage;
        ImageView botIcon;
        ImageView userIcon;
        CardView botMessageContainer;
        CardView userMessageContainer;

        public MessageViewHolder(View itemView) {
            super(itemView);
            botMessage = itemView.findViewById(R.id.botMessage);
            userMessage = itemView.findViewById(R.id.userMessage);
            botIcon = itemView.findViewById(R.id.botIcon);
            userIcon = itemView.findViewById(R.id.userIcon);
            botMessageContainer = itemView.findViewById(R.id.botMessageContainer);
            userMessageContainer = itemView.findViewById(R.id.userMessageContainer);
        }

        public void bind(Message message) {
            if (message.isUser()) {
                userMessageContainer.setVisibility(View.VISIBLE);
                userMessage.setText(message.getText());
                userIcon.setVisibility(View.VISIBLE);

                botMessageContainer.setVisibility(View.GONE);
                botIcon.setVisibility(View.GONE);
            } else {
                botMessageContainer.setVisibility(View.VISIBLE);
                botMessage.setText(message.getText());
                botIcon.setVisibility(View.VISIBLE);

                userMessageContainer.setVisibility(View.GONE);
                userIcon.setVisibility(View.GONE);
            }
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView chatbotIcon;
        RecyclerView eventRecyclerView;
        EventAdapter eventAdapter;

        public EventViewHolder(View itemView) {
            super(itemView);
            chatbotIcon = itemView.findViewById(R.id.chatbotIcon);
            eventRecyclerView = itemView.findViewById(R.id.eventRecyclerView);

            eventRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            eventAdapter = new EventAdapter(itemView.getContext(), new ArrayList<>());
            eventRecyclerView.setAdapter(eventAdapter);
        }

        public void bind(Event event) {
            chatbotIcon.setImageResource(R.drawable.ic_chatbot);
            ArrayList<Event> singleEventList = new ArrayList<>();
            singleEventList.add(event);
            eventAdapter.setSearchList(singleEventList);
        }
    }
}
