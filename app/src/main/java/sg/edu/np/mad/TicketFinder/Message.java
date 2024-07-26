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

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Setter for message
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUser() {
        return isUser;
    }

    // Setter for isUser
    public void setUser(boolean user) {
        isUser = user;
    }

    // Getter for events
    public List<Event> getEventList() {
        return events;
    }

    // Setter for events
    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
