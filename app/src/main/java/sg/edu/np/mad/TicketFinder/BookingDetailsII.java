package sg.edu.np.mad.TicketFinder;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.List;

public class BookingDetailsII {
    private String concertTitle;
    private String eventTime;
    private String name;
    private String paymentMethod;
    private Timestamp purchaseTime;
    private int quantity;
    private List<Long> ticketIDs;
    private double totalPrice;
    private String userId;
    private boolean isExpanded;

    public BookingDetailsII() {
        // Default constructor required for calls to DataSnapshot.getValue(BookingDetailsII.class)
    }
    public BookingDetailsII(String concertTitle, String eventTime, String name, String paymentMethod, Timestamp purchaseTime, int quantity, List<Long> ticketIDs, double totalPrice) {
        this.concertTitle = concertTitle;
        this.eventTime = eventTime;
        this.name = name;
        this.paymentMethod = paymentMethod;
        this.purchaseTime = purchaseTime;
        this.quantity = quantity;
        this.ticketIDs = ticketIDs;
        this.totalPrice = totalPrice;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Timestamp getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Timestamp purchaseTime) {
        this.purchaseTime = purchaseTime;
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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
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
