package sg.edu.np.mad.TicketFinder;

public class Message {
    private String text;
    private boolean isUser;
    private Event event;

    // Constructor for text messages
    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.event = null;
    }

    // Constructor for event messages
    public Message(Event event) {
        this.event = event;
        this.isUser = false; // Events are from the system
        this.text = null;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isEvent() {
        return event != null;
    }
}
