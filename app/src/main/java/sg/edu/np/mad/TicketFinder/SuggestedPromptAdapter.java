package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestedPromptAdapter extends RecyclerView.Adapter<SuggestedPromptAdapter.ViewHolder> {

    private List<String> suggestedPrompts;
    private OnItemClickListener listener;

    public SuggestedPromptAdapter(List<String> suggestedPrompts, OnItemClickListener listener) {
        this.suggestedPrompts = suggestedPrompts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_prompt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String prompt = suggestedPrompts.get(position);
        holder.promptTextView.setText(prompt);
        holder.promptTextView.setOnClickListener(v -> listener.onItemClick(prompt));
    }

    @Override
    public int getItemCount() {
        return suggestedPrompts.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String prompt);
    }

    public void updateData(List<String> newPrompts) {
        this.suggestedPrompts = newPrompts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView promptTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            promptTextView = itemView.findViewById(R.id.promptTextView);
        }
    }
}
