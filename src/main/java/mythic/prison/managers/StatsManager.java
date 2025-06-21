package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import net.minestom.server.entity.Player;

public class StatsManager {

    public long getBlocksMined(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        return profile != null ? profile.getBlocksMined() : 0;
    }

    public void addBlocksMined(Player player, long amount) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile != null) {
            profile.addBlocksMined(amount);
        }
    }

    public long getMonstersKilled(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        return profile != null ? profile.getMonstersKilled() : 0;
    }

    public void addMonstersKilled(Player player, long amount) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile != null) {
            profile.addMonstersKilled(amount);
        }
    }

    public double getTotalMoneyEarned(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        return profile != null ? profile.getTotalMoneyEarned() : 0;
    }

    public long getTotalPlaytime(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        return profile != null ? profile.getTotalPlaytime() : 0;
    }

    public long getCommandsUsed(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        return profile != null ? profile.getCommandsUsed() : 0;
    }

    public void addCommandUsed(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile != null) {
            profile.addCommandUsed();
        }
    }

    public long getDeathCount(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        return profile != null ? profile.getDeathCount() : 0;
    }

    public void addDeath(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile != null) {
            profile.addDeath();
        }
    }
}