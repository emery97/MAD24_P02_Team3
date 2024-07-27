package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class UpcomingConcert {
    private String concertTitle;
    private String eventTime;
    private String name;
    private int quantity;
    private List<Long> ticketIDs;
    private String userId;
    private boolean isExpanded;

    public UpcomingConcert() {
        // Default constructor required for calls to DataSnapshot.getValue(UpcomingConcert.class)
    }

    public UpcomingConcert(String concertTitle, String eventTime, String name, int quantity, List<Long> ticketIDs, String userId) {
        this.concertTitle = concertTitle;
        this.eventTime = eventTime;
        this.name = name;
        this.quantity = quantity;
        this.ticketIDs = ticketIDs;
        this.userId = userId;
        this.isExpanded = false; // Default value
    }

    // Getters and setters for all fields
    public String getConcertTitle() {
        return concertTitle;
    }

    public void setConcertTitle(String concertTitle) {
        this.concertTitle = concertTitle;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<Long> getTicketIDs() {
        return ticketIDs;
    }

    public void setTicketIDs(List<Long> ticketIDs) {
        this.ticketIDs = ticketIDs;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
