package sg.edu.np.mad.TicketFinder;

public class Greeting {
    private String greeting;
    private String response;

    public Greeting() {
        // Default constructor required for calls to DataSnapshot.getValue(Greeting.class)
    }

    public Greeting(String greeting, String response) {
        this.greeting = greeting;
        this.response = response;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
