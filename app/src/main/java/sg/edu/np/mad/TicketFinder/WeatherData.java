package sg.edu.np.mad.TicketFinder;

public class WeatherData {
    private String date;
    private String forecast;
    private String humidityRange;
    private String temperatureRange;
    private String windRange;
    private int weatherIcon;

    public WeatherData(String date, String forecast, String humidityRange, String temperatureRange, String windRange, int weatherIcon) {
        this.date = date;
        this.forecast = forecast;
        this.humidityRange = humidityRange;
        this.temperatureRange = temperatureRange;
        this.windRange = windRange;
        this.weatherIcon = weatherIcon;
    }

    public String getDate() {
        return date;
    }

    public String getForecast() {
        return forecast;
    }

    public String getHumidityRange() {
        return humidityRange;
    }

    public String getTemperatureRange() {
        return temperatureRange;
    }

    public String getWindRange() {
        return windRange;
    }

    public int getWeatherIcon() {
        return weatherIcon;
    }
}
