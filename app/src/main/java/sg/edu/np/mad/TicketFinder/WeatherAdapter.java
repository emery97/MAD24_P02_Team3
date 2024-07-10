package sg.edu.np.mad.TicketFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private List<WeatherData> weatherDataList;

    public void setWeatherData(List<WeatherData> weatherDataList) {
        this.weatherDataList = weatherDataList;
        notifyDataSetChanged(); // Notify adapter of data change
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weatherpageitem, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        WeatherData weatherData = weatherDataList.get(position);
        holder.weatherDate.setText(weatherData.getDate());
        holder.weatherForecast.setText(weatherData.getForecast());
        holder.weatherHumidity.setText(weatherData.getHumidityRange());
        holder.weatherTemperature.setText(weatherData.getTemperatureRange());
        holder.weatherWind.setText(weatherData.getWindRange());

        holder.weatherIcon.setImageResource(weatherData.getWeatherIcon());
    }

    @Override
    public int getItemCount() {
        return weatherDataList == null ? 0 : weatherDataList.size();
    }

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView weatherDate;
        TextView weatherForecast;
        TextView weatherHumidity;
        TextView weatherTemperature;
        TextView weatherWind;
        ImageView weatherIcon;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            weatherDate = itemView.findViewById(R.id.weatherDate);
            weatherForecast = itemView.findViewById(R.id.weatherForecast);
            weatherHumidity = itemView.findViewById(R.id.weatherHumidity);
            weatherTemperature = itemView.findViewById(R.id.weatherTemperature);
            weatherWind = itemView.findViewById(R.id.weatherWind);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}
