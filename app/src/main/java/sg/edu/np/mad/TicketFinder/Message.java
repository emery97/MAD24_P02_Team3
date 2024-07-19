package sg.edu.np.mad.TicketFinder;

public class Message {
    private String message;
    private boolean isUser;
    private Event event; // ******** changes made here

    // Constructor for user/bot messages
    public Message(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.event = null;
    }

    // Constructor for event messages
    public Message(Event event) {
        this.message = null;
        this.isUser = false;
        this.event = event;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public Event getEvent() {
        return event;
    }
}
