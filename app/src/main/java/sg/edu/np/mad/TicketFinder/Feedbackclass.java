package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class Feedbackclass {

    //attributes
    private String category;
    private String message;
    private List<String> imageURIs;

    // Parameterized constructor
    public Feedbackclass(String category, String message, List<String> imageURIs) {
        this.category = category;
        this.message = message;
        this.imageURIs = imageURIs;
    }

    // getters
    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getImageURIs() {
        return imageURIs;
    }
}
