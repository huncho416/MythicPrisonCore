
package mythic.prison.data.gangs;

import java.util.UUID;

public class GangMember {

    private UUID uuid;
    private String name;
    private GangRank rank;
    private long joinTime;
    private double contributed;

    public GangMember(UUID uuid, String name, GangRank rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
        this.joinTime = System.currentTimeMillis();
        this.contributed = 0.0;
    }

    public GangMember(String uuid, String name, GangRank rank) {
        this(UUID.fromString(uuid), name, rank);
    }

    public boolean canInvite() {
        return rank.canInvite();
    }

    public boolean canManageMembers() {
        return rank.canPromote() || rank.canKick();
    }

    // Getters and setters
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getUsername() { return name; } // Alias for getName()
    public void setName(String name) { this.name = name; }

    public GangRank getRank() { return rank; }
    public void setRank(GangRank rank) { this.rank = rank; }

    public long getJoinTime() { return joinTime; }

    public double getContributed() { return contributed; }
    public void addContribution(double amount) { this.contributed += amount; }
}