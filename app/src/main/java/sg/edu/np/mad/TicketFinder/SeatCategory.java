package sg.edu.np.mad.TicketFinder;

public class SeatCategory {
    private Double seatCategoryPrice;
    private String category;
    private String seats;

    // default constructor
    public SeatCategory(){}

    // parameterized constructor
    public SeatCategory(Integer seatCategoryPrice, String category, String seats) {
        this.seatCategoryPrice = Double.valueOf(seatCategoryPrice);
        this.category = category;
        this.seats = seats;
    }

    // getters
    public Double getSeatCategoryPrice() { return seatCategoryPrice; }
    public String getCategory() { return category; }
    public String getSeats() { return seats; }

    // setters
    public void setSeatCategoryPrice(Double price) { this.seatCategoryPrice = price; }
    public void setCategory(String category) { this.category = category; } // Fixed setter
    public void setSeats(String seats) { this.seats = seats; } // Fixed setter
}
