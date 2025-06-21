package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class RankingManager {

    private static final String[] RANKS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public String getRank(Player player) {
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        return profile != null ? profile.getCurrentRank() : "A";
    }

    public int getPrestige(Player player) {
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        return profile != null ? profile.getPrestige() : 0;
    }

    public int getRebirth(Player player) {
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        return profile != null ? profile.getRebirth() : 0;
    }

    public int getAscension(Player player) {
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        return profile != null ? profile.getAscension() : 0;
    }

    public String getFormattedRank(Player player) {
        String rank = getRank(player);
        String rankColor = getRankColor(rank);
        return rankColor + rank;
    }

    public boolean canRankup(Player player) {
        String currentRank = getRank(player);
        if (currentRank.equals("Z")) return false;

        double cost = getRankupCost(player);
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        return currencyManager.hasBalance(player, "money", cost);
    }

    public boolean canPrestige(Player player) {
        String currentRank = getRank(player);
        if (!currentRank.equals("Z")) return false;

        double cost = getPrestigeCost(player);
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        return currencyManager.hasBalance(player, "money", cost);
    }

    public boolean canRebirth(Player player) {
        int prestige = getPrestige(player);
        if (prestige < 10) return false;

        double cost = getRebirthCost(player);
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        return currencyManager.hasBalance(player, "money", cost);
    }

    public boolean canAscend(Player player) {
        int rebirth = getRebirth(player);
        if (rebirth < 5) return false;

        double cost = getAscensionCost(player);
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        return currencyManager.hasBalance(player, "money", cost);
    }

    public double getRankupCost(Player player) {
        String currentRank = getRank(player);
        int rankIndex = getRankIndex(currentRank);
        if (rankIndex == -1) return 0;

        // Base cost increases exponentially
        return 1000 * Math.pow(1.5, rankIndex);
    }

    public double getPrestigeCost(Player player) {
        int currentPrestige = getPrestige(player);
        // Prestige costs significantly more than rank Z
        // Base cost: 10x the cost of rank Z, then exponential increase
        double rankZCost = 1000 * Math.pow(1.5, 25); // Cost of getting to rank Z
        return rankZCost * 10 * Math.pow(1.8, currentPrestige);
    }

    public double getRebirthCost(Player player) {
        int currentRebirth = getRebirth(player);
        // Rebirth costs significantly more than prestige
        // Base cost: 100x the cost of first prestige, then exponential increase
        double firstPrestigeCost = getBaseCostForLevel(0, "prestige");
        return firstPrestigeCost * 100 * Math.pow(2.5, currentRebirth);
    }

    public double getAscensionCost(Player player) {
        int currentAscension = getAscension(player);
        // Ascension costs significantly more than rebirth
        // Base cost: 1000x the cost of first rebirth, then exponential increase
        double firstRebirthCost = getBaseCostForLevel(0, "rebirth");
        return firstRebirthCost * 1000 * Math.pow(3.0, currentAscension);
    }

    private double getBaseCostForLevel(int level, String type) {
        double rankZCost = 1000 * Math.pow(1.5, 25);
        return switch (type) {
            case "prestige" -> rankZCost * 10 * Math.pow(1.8, level);
            case "rebirth" -> rankZCost * 10 * 100 * Math.pow(2.5, level);
            case "ascension" -> rankZCost * 10 * 100 * 1000 * Math.pow(3.0, level);
            default -> 0;
        };
    }

    public boolean rankup(Player player) {
        if (!canRankup(player)) return false;

        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        double cost = getRankupCost(player);

        if (!currencyManager.removeBalance(player, "money", cost)) return false;

        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;

        String currentRank = profile.getCurrentRank();
        String nextRank = getNextRank(currentRank);
        if (nextRank == null) return false;

        profile.setCurrentRank(nextRank);
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        updatePlayerPrefix(player);

        return true;
    }

    public boolean prestige(Player player) {
        if (!canPrestige(player)) return false;

        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        double cost = getPrestigeCost(player);

        // Check if player has enough money
        if (!currencyManager.hasBalance(player, "money", cost)) return false;

        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;

        // Remove the cost
        if (!currencyManager.removeBalance(player, "money", cost)) return false;

        int currentPrestige = profile.getPrestige();
        double soulReward = 1000000 * Math.pow(2, currentPrestige);

        // Reset rank to A, but keep all currencies
        profile.setCurrentRank("A");
        profile.setPrestige(currentPrestige + 1);

        // Give souls reward
        profile.addBalance("souls", soulReward);

        // Add permanent money multiplier
        MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
        multiplierManager.addMultiplier(player, "money", 0.1); // 10% per prestige

        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        updatePlayerPrefix(player);

        return true;
    }

    public boolean rebirth(Player player) {
        if (!canRebirth(player)) return false;

        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        double cost = getRebirthCost(player);

        // Check if player has enough money
        if (!currencyManager.hasBalance(player, "money", cost)) return false;

        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;

        // Remove the cost
        if (!currencyManager.removeBalance(player, "money", cost)) return false;

        int currentRebirth = profile.getRebirth();
        double beaconReward = 10000000 * Math.pow(3, currentRebirth);

        // Reset rank and prestige, but keep all currencies
        profile.setCurrentRank("A");
        profile.setPrestige(0);
        profile.setRebirth(currentRebirth + 1);

        // Give beacon reward
        profile.addBalance("beacons", beaconReward);

        // Add massive permanent multipliers
        MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
        multiplierManager.addMultiplier(player, "money", 0.5); // 50% per rebirth
        multiplierManager.addMultiplier(player, "souls", 0.25); // 25% per rebirth

        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        updatePlayerPrefix(player);

        return true;
    }

    public boolean ascend(Player player) {
        if (!canAscend(player)) return false;

        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        double cost = getAscensionCost(player);

        // Check if player has enough money
        if (!currencyManager.hasBalance(player, "money", cost)) return false;

        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;

        // Remove the cost
        if (!currencyManager.removeBalance(player, "money", cost)) return false;

        int currentAscension = profile.getAscension();

        // Reset rank, prestige, and rebirth, but keep ALL currencies
        profile.setCurrentRank("A");
        profile.setPrestige(0);
        profile.setRebirth(0);
        profile.setAscension(currentAscension + 1);

        // Add ultimate permanent multipliers
        MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
        multiplierManager.addMultiplier(player, "money", 1.0); // 100% per ascension
        multiplierManager.addMultiplier(player, "souls", 0.5); // 50% per ascension
        multiplierManager.addMultiplier(player, "beacons", 0.25); // 25% per ascension

        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        updatePlayerPrefix(player);

        return true;
    }

    // Administrative rank reset method
    public boolean resetPlayerRank(Player player, String newRank) {
        if (newRank == null) {
            newRank = "A"; // Default rank
        }

        newRank = newRank.toUpperCase();

        if (!isValidRank(newRank)) {
            return false; // Invalid rank
        }

        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;

        profile.setCurrentRank(newRank);
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        updatePlayerPrefix(player);

        return true;
    }

    // Helper method to validate ranks
    public boolean isValidRank(String rank) {
        if (rank == null) return false;
        rank = rank.toUpperCase();

        for (String validRank : RANKS) {
            if (validRank.equals(rank)) {
                return true;
            }
        }
        return false;
    }

    // Display methods for commands
    public void showAscensionInfo(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§5§l§m            §r §5§lASCENSION INFO §r§5§l§m            ");
        ChatUtil.sendMessage(player, "");

        int currentAscension = getAscension(player);
        int currentRebirth = getRebirth(player);
        double cost = getAscensionCost(player);
        double balance = MythicPrison.getInstance().getCurrencyManager().getBalance(player, "money");

        ChatUtil.sendMessage(player, "§f§lCurrent Ascension: §5⭐" + currentAscension);
        ChatUtil.sendMessage(player, "§f§lCurrent Rebirth: §c⚡" + currentRebirth);
        ChatUtil.sendMessage(player, "");

        // Requirements
        ChatUtil.sendMessage(player, "§f§lRequirements:");
        if (currentRebirth >= 5) {
            ChatUtil.sendMessage(player, "§a✓ §7Have 5+ rebirths §7(§a" + currentRebirth + "§7)");
        } else {
            ChatUtil.sendMessage(player, "§c✗ §7Have 5+ rebirths §7(§c" + currentRebirth + "§7/§a5§7)");
        }

        if (balance >= cost) {
            ChatUtil.sendMessage(player, "§a✓ §7Have enough money §7(§a$" + ChatUtil.formatMoney(balance) + "§7)");
        } else {
            ChatUtil.sendMessage(player, "§c✗ §7Have enough money §7(§c$" + ChatUtil.formatMoney(balance) + "§7/§a$" + ChatUtil.formatMoney(cost) + "§7)");
        }

        ChatUtil.sendMessage(player, "");

        // Cost and rewards
        ChatUtil.sendMessage(player, "§f§lAscension Cost: §c$" + ChatUtil.formatMoney(cost));
        ChatUtil.sendMessage(player, "");

        ChatUtil.sendMessage(player, "§f§lWhat happens when you ascend:");
        ChatUtil.sendMessage(player, "§c• Reset rank to A");
        ChatUtil.sendMessage(player, "§c• Reset prestige to 0");
        ChatUtil.sendMessage(player, "§c• Reset rebirth to 0");
        ChatUtil.sendMessage(player, "§a• Keep ALL currencies (money, tokens, souls, etc.)");
        ChatUtil.sendMessage(player, "§a• Gain +100% permanent money multiplier");
        ChatUtil.sendMessage(player, "§a• Gain +50% permanent soul multiplier");
        ChatUtil.sendMessage(player, "§a• Gain +25% permanent beacon multiplier");
        ChatUtil.sendMessage(player, "");

        // Show current multipliers from ascensions
        if (currentAscension > 0) {
            ChatUtil.sendMessage(player, "§f§lCurrent Ascension Bonuses:");
            ChatUtil.sendMessage(player, "§a+" + (currentAscension * 100) + "% money multiplier");
            ChatUtil.sendMessage(player, "§a+" + (currentAscension * 50) + "% soul multiplier");
            ChatUtil.sendMessage(player, "§a+" + (currentAscension * 25) + "% beacon multiplier");
            ChatUtil.sendMessage(player, "");
        }

        // Can ascend?
        if (canAscend(player)) {
            ChatUtil.sendSuccess(player, "You can ascend! Use §f/ascension confirm §ato ascend!");
        } else {
            ChatUtil.sendError(player, "You cannot ascend yet!");
            if (currentRebirth < 5) {
                ChatUtil.sendMessage(player, "§7You need " + (5 - currentRebirth) + " more rebirths.");
            } else if (balance < cost) {
                double needed = cost - balance;
                ChatUtil.sendMessage(player, "§7You need $" + ChatUtil.formatMoney(needed) + " more money.");
            }
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§5§l§m                                                ");
    }

    public String getNextRankName(Player player) {
        String currentRank = getRank(player);
        return getNextRank(currentRank);
    }

    private String getNextRank(String currentRank) {
        int currentIndex = getRankIndex(currentRank);
        if (currentIndex == -1 || currentIndex >= RANKS.length - 1) return null;
        return RANKS[currentIndex + 1];
    }

    private int getRankIndex(String rank) {
        for (int i = 0; i < RANKS.length; i++) {
            if (RANKS[i].equals(rank)) {
                return i;
            }
        }
        return -1;
    }

    private String getRankColor(String rank) {
        return switch (rank) {
            case "A", "B", "C" -> "§a";
            case "D", "E", "F" -> "§2";
            case "G", "H", "I" -> "§e";
            case "J", "K", "L" -> "§6";
            case "M", "N", "O" -> "§c";
            case "P", "Q", "R" -> "§4";
            case "S", "T", "U" -> "§d";
            case "V", "W", "X" -> "§5";
            case "Y" -> "§b";
            case "Z" -> "§f";
            default -> "§7";
        };
    }

    public void updatePlayerPrefix(Player player) {
        try {
            String rank = getRank(player);
            int prestige = getPrestige(player);
            int rebirth = getRebirth(player);
            int ascension = getAscension(player);

            StringBuilder prefix = new StringBuilder("§8[");
            prefix.append(getRankColor(rank)).append(rank);

            if (prestige > 0) {
                prefix.append("§6✦").append(prestige);
            }

            if (rebirth > 0) {
                prefix.append("§c⚡").append(rebirth);
            }

            if (ascension > 0) {
                prefix.append("§5⭐").append(ascension);
            }

            prefix.append("§8] §f").append(player.getUsername());

            Component displayName = LegacyComponentSerializer.legacySection().deserialize(prefix.toString());
            player.setDisplayName(displayName);
        } catch (Exception e) {
            System.err.println("Error updating player prefix: " + e.getMessage());
        }
    }

    // Helper methods for cost display
    public String getFormattedPrestigeCost(Player player) {
        return ChatUtil.formatMoney(getPrestigeCost(player));
    }

    public String getFormattedRebirthCost(Player player) {
        return ChatUtil.formatMoney(getRebirthCost(player));
    }

    public String getFormattedAscensionCost(Player player) {
        return ChatUtil.formatMoney(getAscensionCost(player));
    }
}