package mythic.prison.data.social;

public class FriendRequest {
    private String fromUuid;
    private String fromUsername;
    private String toUuid;
    private String toUsername;
    private long sentAt;
    private FriendRequestStatus status;
    private String message;

    public enum FriendRequestStatus {
        PENDING, ACCEPTED, DECLINED, CANCELLED
    }

    public FriendRequest() {}

    public FriendRequest(String fromUuid, String fromUsername, String toUuid, String toUsername) {
        this.fromUuid = fromUuid;
        this.fromUsername = fromUsername;
        this.toUuid = toUuid;
        this.toUsername = toUsername;
        this.sentAt = System.currentTimeMillis();
        this.status = FriendRequestStatus.PENDING;
    }

    // Getters and setters
    public String getFromUuid() { return fromUuid; }
    public void setFromUuid(String fromUuid) { this.fromUuid = fromUuid; }

    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }

    public String getToUuid() { return toUuid; }
    public void setToUuid(String toUuid) { this.toUuid = toUuid; }

    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }

    public long getSentAt() { return sentAt; }
    public void setSentAt(long sentAt) { this.sentAt = sentAt; }

    public FriendRequestStatus getStatus() { return status; }
    public void setStatus(FriendRequestStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}