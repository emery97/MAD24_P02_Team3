package sg.edu.np.mad.TicketFinder;
public class User {
    private String name;
    private String profileImageUrl;
    private String userId;

    public User(String name, String profileImageUrl, String userId) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }
}
