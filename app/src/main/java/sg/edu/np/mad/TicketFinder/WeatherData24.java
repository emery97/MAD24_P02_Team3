package sg.edu.np.mad.TicketFinder;

public class WeatherData24 {
    private String area;
    private String forecast;
    private double latitude;
    private double longitude;

    public WeatherData24(String area, String forecast, double latitude, double longitude) {
        this.area = area;
        this.forecast = forecast;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

