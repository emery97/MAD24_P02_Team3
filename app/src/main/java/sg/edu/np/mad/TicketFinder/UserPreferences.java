package sg.edu.np.mad.TicketFinder;

public class UserPreferences {
    private String choice1;
    private String choice2;
    private String choice3;

    public UserPreferences() {
        // Default constructor required for calls to DataSnapshot.getValue(UserPreferences.class)
    }

    public UserPreferences(String choice1, String choice2, String choice3) {
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
    }

    public String getChoice1() {
        return choice1;
    }

    public void setChoice1(String choice1) {
        this.choice1 = choice1;
    }

    public String getChoice2() {
        return choice2;
    }

    public void setChoice2(String choice2) {
        this.choice2 = choice2;
    }

    public String getChoice3() {
        return choice3;
    }

    public void setChoice3(String choice3) {
        this.choice3 = choice3;
    }

    public boolean matches(Event event) {
        String genre = event.getGenre();
        return genre.equalsIgnoreCase(choice1) || genre.equalsIgnoreCase(choice2) || genre.equalsIgnoreCase(choice3);
    }
}
