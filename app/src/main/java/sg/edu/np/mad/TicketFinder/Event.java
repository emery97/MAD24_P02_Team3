package sg.edu.np.mad.TicketFinder;

public class Event {
    private final int imageResId;
    private final String title;
    private float price;
    private String description;
    private String artist;
    private String genre;
    private String venue;
    private final String date;
    private String time;


    public Event(int imageResId, String title, String artist, String date) {
        this.imageResId = imageResId;
        this.title = title;
        this.artist = artist;
        this.date = date;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
    public String getArtist() {return artist; }
    public String getDate() { return date;
    }
}

