package sg.edu.np.mad.TicketFinder;

public class Event {
    private final int imageResId;
    private final String title;

    private final String date;

    public Event(int imageResId, String title, String date) {
        this.imageResId = imageResId;
        this.title = title;
        this.date = date;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
    public String getDate() { return date;
    }
}

