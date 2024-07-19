package sg.edu.np.mad.TicketFinder;

public class MessageModel {
    private String message;
    private String sender;
    private String time;

    public MessageModel(String message, String sender, String time) {
        this.message = message;
        this.sender = sender;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getTime() {
        return time;
    }

    public String getViewType() {
        return sender;
    }
}
