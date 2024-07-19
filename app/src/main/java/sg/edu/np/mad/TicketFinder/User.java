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
    // empty constructor
    public User(){}

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String ProfileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }
}
