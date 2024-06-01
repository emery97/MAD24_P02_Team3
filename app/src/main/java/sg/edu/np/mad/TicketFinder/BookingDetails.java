package sg.edu.np.mad.TicketFinder;

public class BookingDetails {
    // attributes
    private String concertName;
    private String seatCategory;
    private String seatNumber;
    private String totalPrice;
    private String quantity;
    private String paymentMethod;

    // Parameterized constructor
    public BookingDetails(String seatCategory, String seatNumber, String totalPrice, String quantity, String paymentMethod) {
        this.seatCategory = seatCategory;
        this.seatNumber = seatNumber;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.paymentMethod = paymentMethod;
    }

    // Getters and setters
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

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
