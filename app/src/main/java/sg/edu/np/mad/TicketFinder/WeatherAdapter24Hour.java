package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherAdapter24Hour extends RecyclerView.Adapter<WeatherAdapter24Hour.ViewHolder> {

    private List<WeatherData24> weatherDataList;

    public WeatherAdapter24Hour(List<WeatherData24> weatherDataList) {
        this.weatherDataList = weatherDataList;
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
    }

    public void filterList(List<WeatherData24> filteredList) {
        weatherDataList = filteredList;
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

        public ViewHolder(View itemView) {
            super(itemView);
            areaTextView = itemView.findViewById(R.id.areaTextView);
            forecastTextView = itemView.findViewById(R.id.forecastTextView);
            latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
            longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
        }
    }
}

