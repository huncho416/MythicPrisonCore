
package mythic.prison.data.social;

public class Friend {
    private String uuid;
    private String username;
    private long friendSince;
    private boolean online;
    private String currentServer;
    private long lastSeen;
    private boolean favorite;

    public Friend() {}

    public Friend(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.friendSince = System.currentTimeMillis();
        this.online = false;
        this.favorite = false;
    }

    // Getters and setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getFriendSince() { return friendSince; }
    public void setFriendSince(long friendSince) { this.friendSince = friendSince; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getCurrentServer() { return currentServer; }
    public void setCurrentServer(String currentServer) { this.currentServer = currentServer; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}