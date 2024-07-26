package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestedPromptAdapter extends RecyclerView.Adapter<SuggestedPromptAdapter.ViewHolder> {

    // List of suggested prompts
    private List<String> suggestedPrompts;
    // Listener for item click events
    private OnItemClickListener listener;

    // Constructor to initialize the adapter with data and a listener
    public SuggestedPromptAdapter(List<String> suggestedPrompts, OnItemClickListener listener) {
        this.suggestedPrompts = suggestedPrompts;
        this.listener = listener;
    }

    @NonNull
    @Override
    // Method to create a new ViewHolder instance
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_prompt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Method to bind data to the ViewHolder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String prompt = suggestedPrompts.get(position); // Get the prompt at the specified position
        holder.promptTextView.setText(prompt); // Set the text of the prompt TextView
        holder.promptTextView.setOnClickListener(v -> listener.onItemClick(prompt)); // Set an OnClickListener to handle item clicks
    }

    @Override
    public int getItemCount() {
        return suggestedPrompts.size();
    }   // Method to return the total number of items in the data set

    // Interface to handle item click events
    public interface OnItemClickListener {
        void onItemClick(String prompt);
    }

    // Method to update the data in the adapter
    public void updateData(List<String> newPrompts) {
        this.suggestedPrompts = newPrompts;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // TextView to display the prompt
        TextView promptTextView;

        // Constructor to initialize the ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the TextView in the layout
            promptTextView = itemView.findViewById(R.id.promptTextView);
        }
    }
}
