package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class Message {
    private String message;
    private boolean isUser;
    private List<Event> events; // Updated to hold a list of events

    // Constructor for user/bot messages
    public Message(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.events = null;
    }

    // Constructor for event messages
    public Message(List<Event> events) {
        this.message = null;
        this.isUser = false;
        this.events = events;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public List<Event> getEventList() {
        return events;
    }
}
