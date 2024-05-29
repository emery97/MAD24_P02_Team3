package sg.edu.np.mad.TicketFinder;
import java.util.ArrayList;
import java.util.Arrays;

public class SeatCategory {
    private String category;
    private double seatCategoryPrice;
    private ArrayList<String> seats;

    // Default constructor
    public SeatCategory() {}

    // Parameterized constructor
    public SeatCategory(Integer seatCategoryPrice, String category, String seats) {
        this.seatCategoryPrice = Double.valueOf(seatCategoryPrice);
        this.category = category;
        this.seats = new ArrayList<>(Arrays.asList(seats.split(",")));
    }

    // Getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSeatCategoryPrice() {
        return seatCategoryPrice;
    }

    public void setSeatCategoryPrice(double seatCategoryPrice) {
        this.seatCategoryPrice = seatCategoryPrice;
    }

    public ArrayList<String> getSeats() {
        return seats;
    }

    public void setSeats(ArrayList<String> seats) {
        this.seats = seats;
    }
}
