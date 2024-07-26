package sg.edu.np.mad.TicketFinder;

public class Ticket {
    private String seatCategory;
    private String seatNumber;
    private String concertTitle;
    private long TicketID; // Use long for TicketID

    // No-argument constructor required for Firestore
    public Ticket() {}

    public Ticket(String seatCategory, String seatNumber, long TicketID, String concertTitle) {
        this.seatCategory = seatCategory;
        this.seatNumber = seatNumber;
        this.TicketID = TicketID;
        this.concertTitle = concertTitle;
    }

    public String getSeatCategory() {
        return seatCategory;
    }

    public void setSeatCategory(String seatCategory) {
        this.seatCategory = seatCategory;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public long getTicketID() {
        return TicketID;
    }

    public void setTicketID(long TicketID) {
        this.TicketID = TicketID;
    }
    public String getConcertTitle(){return concertTitle;}
    public void setConcertTitle(){this.concertTitle = concertTitle;}
}
