package sg.edu.np.mad.TicketFinder;

public class Message {
    private String message;
    private boolean isUser;

    public Message(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public boolean isBot() {
        return !isUser;
    }
}
