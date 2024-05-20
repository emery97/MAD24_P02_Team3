package sg.edu.np.mad.TicketFinder;

import java.io.Serializable;

public class Event implements Serializable {
    private String title;
    private String caption;
    private double price;
    private String description;
    private String artist;
    private String genre;
    private String venue;
    private String date;
    private String time;

    // Default constructor
    public Event() {}

    // Parameterized constructor
    public Event(String title, String caption, double price, String description,
                 String artist, String genre, String venue, String date, String time) {
        this.title = title;
        this.caption = caption;
        this.price = price;
        this.description = description;
        this.artist = artist;
        this.genre = genre;
        this.venue = venue;
        this.date = date;
        this.time = time;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", venue='" + venue + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}

