package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatBotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<MessageModel> list;

    public ChatBotAdapter(Context context, ArrayList<MessageModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.sender_view, parent, false);
            return new UserView(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.reciever_view, parent, false);
            return new ReceiverView(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel model = list.get(position);
        if (model.getViewType().equals("user")) {
            ((UserView) holder).user.setText(model.getMessage());
            ((UserView) holder).time.setText(model.getTime());
        } else {
            ((ReceiverView) holder).receiver.setText(model.getMessage());
            ((ReceiverView) holder).time.setText(model.getTime());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getViewType().equals("user")) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UserView extends RecyclerView.ViewHolder {

        TextView user;
        TextView time;

        public UserView(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.sender_text);
            time = itemView.findViewById(R.id.sender_time);
        }
    }

    public static class ReceiverView extends RecyclerView.ViewHolder {

        TextView receiver;
        TextView time;

        public ReceiverView(@NonNull View itemView) {
            super(itemView);
            receiver = itemView.findViewById(R.id.receiver_text);
            time = itemView.findViewById(R.id.receiver_time);
        }
    }
}
