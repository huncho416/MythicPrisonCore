package mythic.prison.data.stats;

import java.util.UUID;

public class PlayerStats {

    private String uuid; // Changed from UUID to String
    private String playerName;
    private long firstJoin;
    private long lastActivity;
    private long timePlayed;

    // Mining stats
    private long blocksBroken;
    private double totalMoneyEarned;
    private double totalTokensEarned;
    private double totalSoulsEarned;
    private double totalEssenceEarned;
    private double totalCreditsEarned;
    private double totalBeaconsEarned;

    // Progression stats
    private int totalRankups;
    private int totalPrestiges;
    private int totalRebirths;
    private int totalAscensions;

    // Activity stats
    private int petsUnboxed;
    private int cratesOpened;
    private int pickaxeUpgrades;
    private int kills;
    private int deaths;

    // Updated constructor to accept String UUID
    public PlayerStats(String uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.firstJoin = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.timePlayed = 0;

        // Initialize all stats to 0
        this.blocksBroken = 0;
        this.totalMoneyEarned = 0;
        this.totalTokensEarned = 0;
        this.totalSoulsEarned = 0;
        this.totalEssenceEarned = 0;
        this.totalCreditsEarned = 0;
        this.totalBeaconsEarned = 0;

        this.totalRankups = 0;
        this.totalPrestiges = 0;
        this.totalRebirths = 0;
        this.totalAscensions = 0;

        this.petsUnboxed = 0;
        this.cratesOpened = 0;
        this.pickaxeUpgrades = 0;
        this.kills = 0;
        this.deaths = 0;
    }

    public void updateLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    // Updated getters
    public String getUuid() { return uuid; } // Changed return type to String
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public long getFirstJoin() { return firstJoin; }
    public long getLastActivity() { return lastActivity; }
    public long getTimePlayed() { return timePlayed; }
    public void setTimePlayed(long timePlayed) { this.timePlayed = timePlayed; }

    public long getBlocksBroken() { return blocksBroken; }
    public void setBlocksBroken(long blocksBroken) { this.blocksBroken = blocksBroken; }

    public double getTotalMoneyEarned() { return totalMoneyEarned; }
    public void setTotalMoneyEarned(double totalMoneyEarned) { this.totalMoneyEarned = totalMoneyEarned; }

    public double getTotalTokensEarned() { return totalTokensEarned; }
    public void setTotalTokensEarned(double totalTokensEarned) { this.totalTokensEarned = totalTokensEarned; }

    public double getTotalSoulsEarned() { return totalSoulsEarned; }
    public void setTotalSoulsEarned(double totalSoulsEarned) { this.totalSoulsEarned = totalSoulsEarned; }

    public double getTotalEssenceEarned() { return totalEssenceEarned; }
    public void setTotalEssenceEarned(double totalEssenceEarned) { this.totalEssenceEarned = totalEssenceEarned; }

    public double getTotalCreditsEarned() { return totalCreditsEarned; }
    public void setTotalCreditsEarned(double totalCreditsEarned) { this.totalCreditsEarned = totalCreditsEarned; }

    public double getTotalBeaconsEarned() { return totalBeaconsEarned; }
    public void setTotalBeaconsEarned(double totalBeaconsEarned) { this.totalBeaconsEarned = totalBeaconsEarned; }

    public int getTotalRankups() { return totalRankups; }
    public void setTotalRankups(int totalRankups) { this.totalRankups = totalRankups; }

    public int getTotalPrestiges() { return totalPrestiges; }
    public void setTotalPrestiges(int totalPrestiges) { this.totalPrestiges = totalPrestiges; }

    public int getTotalRebirths() { return totalRebirths; }
    public void setTotalRebirths(int totalRebirths) { this.totalRebirths = totalRebirths; }

    public int getTotalAscensions() { return totalAscensions; }
    public void setTotalAscensions(int totalAscensions) { this.totalAscensions = totalAscensions; }

    public int getPetsUnboxed() { return petsUnboxed; }
    public void setPetsUnboxed(int petsUnboxed) { this.petsUnboxed = petsUnboxed; }

    public int getCratesOpened() { return cratesOpened; }
    public void setCratesOpened(int cratesOpened) { this.cratesOpened = cratesOpened; }

    public int getPickaxeUpgrades() { return pickaxeUpgrades; }
    public void setPickaxeUpgrades(int pickaxeUpgrades) { this.pickaxeUpgrades = pickaxeUpgrades; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
}