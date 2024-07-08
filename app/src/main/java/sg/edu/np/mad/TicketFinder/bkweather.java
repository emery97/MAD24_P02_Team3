package sg.edu.np.mad.TicketFinder;

public class bkweather {
    private String date;
    private int iconResId; // Drawable resource ID for the weather icon
    private String forecast;
    private int lowTemp;
    private int highTemp;

    public bkweather(String date, int iconResId, String forecast, int lowTemp, int highTemp) {
        this.date = date;
        this.iconResId = iconResId;
        this.forecast = forecast;
        this.lowTemp = lowTemp;
        this.highTemp = highTemp;
    }

    public String getDate() {
        return date;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getForecast() {
        return forecast;
    }
    public int getLowTemp() {
        return lowTemp;
    }
    public int getHighTemp() {
        return highTemp;
    }
    public String getTemperatureString() {
        return "Low: " + lowTemp + "°C, High: " + highTemp + "°C";
    }
}
