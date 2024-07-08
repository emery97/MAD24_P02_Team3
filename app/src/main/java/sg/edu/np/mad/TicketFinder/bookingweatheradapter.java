package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class bookingweatheradapter extends RecyclerView.Adapter<bookingweatheradapter.WeatherViewHolder> {

    private List<bkweather> weatherItemList;

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        public TextView weatherDate;
        public ImageView weatherIcon;
        public TextView weatherForecast;
        public TextView weatherTemperature;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            weatherDate = itemView.findViewById(R.id.weather_date);
            weatherIcon = itemView.findViewById(R.id.weather_icon);
            weatherForecast = itemView.findViewById(R.id.weather_forecast);
            weatherTemperature = itemView.findViewById(R.id.weather_temperature);
        }
    }

    public bookingweatheradapter(List<bkweather> weatherItemList) {
        this.weatherItemList = weatherItemList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_history_weather, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        bkweather weatherItem = weatherItemList.get(position);
        holder.weatherDate.setText(weatherItem.getDate());
        holder.weatherIcon.setImageResource(weatherItem.getIconResId()); // Set appropriate weather icon
        holder.weatherForecast.setText(weatherItem.getForecast());
        holder.weatherTemperature.setText(weatherItem.getTemperatureString());
    }

    @Override
    public int getItemCount() {
        return weatherItemList.size();
    }
}