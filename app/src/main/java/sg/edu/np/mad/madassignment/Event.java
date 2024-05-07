package sg.edu.np.mad.madassignment;

public class Event {
    private final int imageResId;
    private final String title;

    public Event(int imageResId, String title) {
        this.imageResId = imageResId;
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
}
