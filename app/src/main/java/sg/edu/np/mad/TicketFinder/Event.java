package sg.edu.np.mad.TicketFinder;

import java.io.Serializable;

public class Event implements Serializable {
    private final int imageResId;
    private final String title;
    private double price;
    private String description;
    private String artist;
    private String genre;
    private String venue;
    private final String date;
    private String time;


    public Event(int imageResId,
                 String title,
                 double price,
                 String description,
                 String artist,
                 String genre,
                 String venue,
                 String date,
                 String time) {
        this.imageResId = imageResId;
        this.title = title;
        this.price = price;
        this.description = description;
        this.artist = artist;
        this.genre = genre;
        this.venue = venue;
        this.date = date;
        this.time = time;
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

