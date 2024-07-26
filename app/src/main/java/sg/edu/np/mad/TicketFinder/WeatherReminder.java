package sg.edu.np.mad.TicketFinder;

public class WeatherReminder {

    private String area;
    private String forecast;
    private String userId;
    private String documentId;

    public WeatherReminder() {
        // No-argument constructor required for Firestore
    }
    // Constructor
    public WeatherReminder(String area, String forecast, String documentId) {
        this.area = area;
        this.forecast = forecast;
        this.documentId = documentId;
    }

    // Getter and Setter for area
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
