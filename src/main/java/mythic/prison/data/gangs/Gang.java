package mythic.prison.data.gangs;

import java.util.*;

public class Gang {
    private final String id;
    private String name;
    private final String ownerUUID;
    private final Map<String, GangRole> members;
    private final Set<String> invites;
    private double bank;
    private int level;
    private long experience;
    private long createdTime;

    public Gang(String id, String name, String ownerUUID) {
        this.id = id;
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.members = new HashMap<>();
        this.invites = new HashSet<>();
        this.bank = 0.0;
        this.level = 1;
        this.experience = 0;
        this.createdTime = System.currentTimeMillis();
        
        // Add owner as leader
        this.members.put(ownerUUID, GangRole.LEADER);
    }

    public void addMember(String playerUUID, GangRole role) {
        members.put(playerUUID, role);
        invites.remove(playerUUID);
    }

    public void removeMember(String playerUUID) {
        members.remove(playerUUID);
    }

    public boolean isMember(String playerUUID) {
        return members.containsKey(playerUUID);
    }

    public GangRole getMemberRole(String playerUUID) {
        return members.get(playerUUID);
    }

    public int getMemberCount() {
        return members.size();
    }

    public Set<String> getMembers() {
        return new HashSet<>(members.keySet());
    }

    public int getMaxMembers() {
        // Base members + level bonus
        return 10 + (level - 1) * 2;
    }

    public void addToBank(double amount) {
        this.bank += amount;
    }

    public boolean withdrawFromBank(double amount) {
        if (bank >= amount) {
            bank -= amount;
            return true;
        }
        return false;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerUUID() { return ownerUUID; }
    public double getBank() { return bank; }
    public void setBank(double bank) { this.bank = bank; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getExperience() { return experience; }
    public void setExperience(long experience) { this.experience = experience; }
    public long getCreatedTime() { return createdTime; }

    public enum GangRole {
        MEMBER("Member", "§7"),
        MODERATOR("Moderator", "§e"),
        LEADER("Leader", "§c");

        private final String displayName;
        private final String color;

        GangRole(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
}