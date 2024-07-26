package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherAdapter24Hour extends RecyclerView.Adapter<WeatherAdapter24Hour.ViewHolder> {

    private List<WeatherData24> weatherDataList;
    private Context context;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    public WeatherAdapter24Hour(Context context,List<WeatherData24> weatherDataList) {
        this.context = context;
        this.weatherDataList = weatherDataList;
        this.db = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather24hour, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherData24 weatherData = weatherDataList.get(position);
        holder.areaTextView.setText(weatherData.getArea());
        holder.forecastTextView.setText(weatherData.getForecast());
        holder.latitudeTextView.setText("Latitude: " + weatherData.getLatitude());
        holder.longitudeTextView.setText("Longtitude: " + weatherData.getLongitude());

        holder.actionButton.setOnClickListener(v -> {
            String area = weatherData.getArea();
            String forecast = weatherData.getForecast();
            String userId = sharedPreferences.getString("UserId", "unknown");

            // Create a map to hold the data to be sent to Firestore
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("area", area);
            reminder.put("forecast", forecast);
            reminder.put("userId", userId);

            // Add a new document with a generated ID
            db.collection("Reminders")
                    .add(reminder)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Reminder added successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error adding reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    public void filterList(List<WeatherData24> filteredList) {
        this.weatherDataList = filteredList;
        notifyDataSetChanged();
    }

    public void addWeatherData(List<WeatherData24> newWeatherData) {
        weatherDataList.addAll(newWeatherData);
        notifyDataSetChanged(); // Notify adapter of the updated data
    }

    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView areaTextView;
        TextView forecastTextView;
        TextView latitudeTextView;
        TextView longitudeTextView;
        ImageButton actionButton;

        public ViewHolder(View itemView) {
            super(itemView);
            areaTextView = itemView.findViewById(R.id.areaTextView);
            forecastTextView = itemView.findViewById(R.id.forecastTextView);
            latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
            longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
            actionButton =itemView.findViewById(R.id.actionButton);
        }
    }
}
