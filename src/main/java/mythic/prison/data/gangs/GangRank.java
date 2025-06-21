
package mythic.prison.data.gangs;

public enum GangRank {
    MEMBER("§f", "Member", false, false, false, false),
    TRUSTED("§a", "Trusted", false, false, false, false),
    MODERATOR("§9", "Moderator", true, true, false, false),
    ADMIN("§c", "Admin", true, true, true, true),
    LEADER("§6", "Leader", true, true, true, true);

    private final String color;
    private final String displayName;
    private final boolean canInvite;
    private final boolean canKick;
    private final boolean canPromote;
    private final boolean canManageBank;

    GangRank(String color, String displayName, boolean canInvite, boolean canKick, boolean canPromote, boolean canManageBank) {
        this.color = color;
        this.displayName = displayName;
        this.canInvite = canInvite;
        this.canKick = canKick;
        this.canPromote = canPromote;
        this.canManageBank = canManageBank;
    }

    public GangRank getNext() {
        GangRank[] ranks = values();
        for (int i = 0; i < ranks.length - 1; i++) {
            if (ranks[i] == this) {
                return ranks[i + 1];
            }
        }
        return null; // Already at highest rank
    }

    public GangRank getPrevious() {
        GangRank[] ranks = values();
        for (int i = 1; i < ranks.length; i++) {
            if (ranks[i] == this) {
                return ranks[i - 1];
            }
        }
        return null; // Already at lowest rank
    }

    public boolean canManageMembers() {
        return canPromote || canKick;
    }

    public String getColor() { return color; }
    public String getDisplayName() { return displayName; }
    public boolean canInvite() { return canInvite; }
    public boolean canKick() { return canKick; }
    public boolean canPromote() { return canPromote; }
    public boolean canManageBank() { return canManageBank; }
}