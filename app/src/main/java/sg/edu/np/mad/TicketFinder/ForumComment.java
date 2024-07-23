package sg.edu.np.mad.TicketFinder;

public class ForumComment {
    private String comment;
    private String documentId;
    private long timestamp;
    private String name;
    private String profilePicUrl;

    public ForumComment() {
    }

    public ForumComment(String comment, String documentId, long timestamp, String name,String profilePicUrl) {
        this.comment = comment;
        this.documentId = documentId;
        this.timestamp = timestamp;
        this.profilePicUrl = profilePicUrl;
        this.name = name;

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
}
