package sg.edu.np.mad.TicketFinder;

public class SeatCategory {
    private Double seatCategoryPrice ;
    private String category;
    private String seats;

    // default constructor
    public SeatCategory(){}

    // parameterized constructor
    public SeatCategory(Integer seatCategoryPrice, String category, String seats) {
        this.seatCategoryPrice = Double.valueOf(Integer.valueOf(String.valueOf(seatCategoryPrice)));
        this.category = category;
        this.seats = seats;
    }

    // getters
    public Double getSeatCategoryPrice(){return seatCategoryPrice;}
    public String getCategory(){return category;}
    public String getSeats(){return seats;}

    // setters
    public void setSeatCategoryPrice(Double price){this.seatCategoryPrice = seatCategoryPrice;}
    public void setCategory(String category){this.category = this.category;}
    public void setSeats(String s){this.seats = seats;}

}
