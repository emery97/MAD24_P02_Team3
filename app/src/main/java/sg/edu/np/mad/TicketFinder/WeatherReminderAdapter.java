package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class WeatherReminderAdapter extends RecyclerView.Adapter<WeatherReminderAdapter.ViewHolder> {

    private List<WeatherReminder> reminderList;

    public WeatherReminderAdapter(List<WeatherReminder> reminderList) {
        this.reminderList = reminderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_reminder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherReminder reminder = reminderList.get(position);
        holder.areaTextView.setText(reminder.getArea() + ": ");
        holder.forecastTextView.setText(reminder.getForecast());
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public List<WeatherReminder> getReminderList() {
        return reminderList;
    }
    public void swapItems(int fromPosition, int toPosition) {
        Collections.swap(reminderList, fromPosition, toPosition);
    }
    public WeatherReminder getItem(int position) {
        return reminderList.get(position);
    }

    public void restoreItem(WeatherReminder item, int position) {
        reminderList.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        reminderList.remove(position);
        notifyItemRemoved(position);
    }
    public void setReminderList(List<WeatherReminder> newList) {
        reminderList.clear();
        reminderList.addAll(newList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView areaTextView;
        TextView forecastTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            areaTextView = itemView.findViewById(R.id.areaTextView);
            forecastTextView = itemView.findViewById(R.id.forecastTextView);
        }
    }
}
