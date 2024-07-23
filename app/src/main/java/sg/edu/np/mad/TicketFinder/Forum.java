package sg.edu.np.mad.TicketFinder;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Forum {
    private String userId;
    private String name;
    private String email;
    private String message;
    private String event;
    private long timestamp;
    private String profilePicUrl;
    private Timestamp currenttimestamp;
    private List<String> imageUrls;
    private String documentId;

    public Forum() {
    }
    public Forum(String documentId,String userId, String name, String email, String message, String event, String profilePicUrl,List<String> imageUrls) {
        this.documentId = documentId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.message = message;
        this.event = event;
        this.timestamp = System.currentTimeMillis();
        this.profilePicUrl = profilePicUrl;
        this.imageUrls = imageUrls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
