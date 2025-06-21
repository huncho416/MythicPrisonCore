package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.milestones.Milestone;
import mythic.prison.data.milestones.PlayerMilestones;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import mythic.prison.data.stats.PlayerStats;
import mythic.prison.data.milestones.Milestone.MilestoneType;

public class MilestoneManager {

    private final Map<String, PlayerMilestones> playerMilestones = new ConcurrentHashMap<>();
    private final List<Milestone> availableMilestones = new ArrayList<>();

    public MilestoneManager() {
        initializeMilestones();
    }

    private void initializeMilestones() {
        // Mining milestones
        Map<String, Double> rewards1 = new HashMap<>();
        rewards1.put("money", 1000.0);
        availableMilestones.add(new Milestone("first_steps", "First Steps", "Mine 100 blocks", Milestone.MilestoneType.BLOCKS_MINED, 100, rewards1, 1));

        Map<String, Double> rewards2 = new HashMap<>();
        rewards2.put("money", 5000.0);
        rewards2.put("tokens", 10.0);
        availableMilestones.add(new Milestone("getting_started", "Getting Started", "Mine 1,000 blocks", Milestone.MilestoneType.BLOCKS_MINED, 1000, rewards2, 2));

        Map<String, Double> rewards3 = new HashMap<>();
        rewards3.put("money", 25000.0);
        rewards3.put("tokens", 50.0);
        availableMilestones.add(new Milestone("dedicated_miner", "Dedicated Miner", "Mine 10,000 blocks", Milestone.MilestoneType.BLOCKS_MINED, 10000, rewards3, 3));

        Map<String, Double> rewards4 = new HashMap<>();
        rewards4.put("money", 100000.0);
        rewards4.put("tokens", 200.0);
        availableMilestones.add(new Milestone("block_breaker", "Block Breaker", "Mine 50,000 blocks", Milestone.MilestoneType.BLOCKS_MINED, 50000, rewards4, 4));

        Map<String, Double> rewards5 = new HashMap<>();
        rewards5.put("money", 500000.0);
        rewards5.put("tokens", 1000.0);
        availableMilestones.add(new Milestone("mining_machine", "Mining Machine", "Mine 100,000 blocks", Milestone.MilestoneType.BLOCKS_MINED, 100000, rewards5, 5));

        // Money milestones
        Map<String, Double> rewards6 = new HashMap<>();
        rewards6.put("tokens", 25.0);
        availableMilestones.add(new Milestone("first_fortune", "First Fortune", "Earn $10,000", Milestone.MilestoneType.MONEY_EARNED, 10000, rewards6, 6));

        Map<String, Double> rewards7 = new HashMap<>();
        rewards7.put("tokens", 100.0);
        availableMilestones.add(new Milestone("rich_miner", "Rich Miner", "Earn $100,000", Milestone.MilestoneType.MONEY_EARNED, 100000, rewards7, 7));

        Map<String, Double> rewards8 = new HashMap<>();
        rewards8.put("tokens", 500.0);
        availableMilestones.add(new Milestone("millionaire", "Millionaire", "Earn $1,000,000", Milestone.MilestoneType.MONEY_EARNED, 1000000, rewards8, 8));

        // Token milestones
        Map<String, Double> rewards9 = new HashMap<>();
        rewards9.put("money", 50000.0);
        availableMilestones.add(new Milestone("token_collector", "Token Collector", "Earn 1,000 tokens", Milestone.MilestoneType.TOKENS_EARNED, 1000, rewards9, 9));

        Map<String, Double> rewards10 = new HashMap<>();
        rewards10.put("money", 150000.0);
        availableMilestones.add(new Milestone("token_master", "Token Master", "Earn 10,000 tokens", Milestone.MilestoneType.TOKENS_EARNED, 10000, rewards10, 10));
    }

    public void initializePlayer(Object player) {
        if (!(player instanceof Player)) return;
        String uuid = getPlayerUUID(player);
        playerMilestones.putIfAbsent(uuid, new PlayerMilestones(uuid));
    }

    public void checkMilestones(Object player) {
        try {
            String uuid = getPlayerUUID(player);
            initializePlayer(player);

            PlayerMilestones milestones = playerMilestones.get(uuid);
            if (milestones == null) return;

            StatsManager statsManager = MythicPrison.getInstance().getStatsManager();

            for (Milestone milestone : availableMilestones) {
                if (milestones.isCompleted(milestone.getId())) continue;

                long currentProgress = getCurrentProgress(player, milestone, statsManager);
                if (currentProgress >= milestone.getTargetValue()) {
                    completeMilestone(player, milestone);
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking milestones: " + e.getMessage());
        }
    }

    private void completeMilestone(Object player, Milestone milestone) {
        try {
            String uuid = getPlayerUUID(player);
            String username = getPlayerUsername(player);

            PlayerMilestones milestones = playerMilestones.get(uuid);
            if (milestones == null) return;

            milestones.completeMilestone(milestone.getId());

            if (player instanceof Player p) {
                ChatUtil.sendMessage(p, "§a§l✓ MILESTONE COMPLETED!");
                ChatUtil.sendMessage(p, "§f" + milestone.getName() + " - " + milestone.getDescription());
                ChatUtil.sendMessage(p, "§eRewards:");
                
                for (Map.Entry<String, Double> reward : milestone.getRewards().entrySet()) {
                    String rewardType = reward.getKey();
                    Double amount = reward.getValue();
                    ChatUtil.sendMessage(p, "§f+ " + String.format("%.0f", amount) + " " + rewardType);
                }
            }

            broadcastToAll("§d" + username + " §fcompleted milestone: §d" + milestone.getName());
        } catch (Exception e) {
            System.err.println("Error completing milestone: " + e.getMessage());
        }
    }

    public void showMilestones(Object player) {
        try {
            if (!(player instanceof Player p)) return;

            String uuid = getPlayerUUID(player);
            initializePlayer(player);

            PlayerMilestones milestones = playerMilestones.get(uuid);
            if (milestones == null) return;

            StatsManager statsManager = MythicPrison.getInstance().getStatsManager();

            ChatUtil.sendMessage(p, "§d§l§m            §r §d§lMILESTONES §r§d§l§m            ");
            ChatUtil.sendMessage(p, "");

            int completed = getCompletedCount(player);
            double percentage = getCompletionPercentage(player);

            ChatUtil.sendMessage(p, "§fProgress: §a" + completed + "§f/§e" + availableMilestones.size() + " §7(" + String.format("%.1f", percentage) + "%)");
            ChatUtil.sendMessage(p, createProgressBar(percentage / 100.0));
            ChatUtil.sendMessage(p, "");

            // Show next 5 incomplete milestones
            List<Milestone> incomplete = getIncompleteMilestones(player);
            int shown = 0;

            for (Milestone milestone : incomplete) {
                if (shown >= 5) break;

                long current = getCurrentProgress(player, milestone, statsManager);
                long required = milestone.getTargetValue();
                double progress = Math.min(100.0, (double) current / required * 100.0);

                String status = milestones.isCompleted(milestone.getId()) ? "§a✓ COMPLETED" : "§7" + current + "/" + required;

                ChatUtil.sendMessage(p, "§f• §d" + milestone.getName() + " §7- " + status);
                ChatUtil.sendMessage(p, "  §7" + milestone.getDescription());
                ChatUtil.sendMessage(p, "  " + createProgressBar(progress / 100.0));
                ChatUtil.sendMessage(p, "");

                shown++;
            }

            ChatUtil.sendMessage(p, "§d§l§m                                                    ");
        } catch (Exception e) {
            System.err.println("Error showing milestones: " + e.getMessage());
        }
    }

    private String createProgressBar(double percentage) {
        int totalBars = 20;
        int filledBars = (int) (percentage * totalBars);
        
        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append("§7] §f").append(String.format("%.1f", percentage * 100)).append("%");
        
        return bar.toString();
    }

private long getCurrentProgress(Object player, Milestone milestone, StatsManager statsManager) {
    // Convert Object to Player - assuming it's a Player object
    if (!(player instanceof net.minestom.server.entity.Player minestomPlayer)) {
        return 0;
    }
    
    // Get progress based on milestone type using enum values
    MilestoneType milestoneType = milestone.getType();
    MythicPrison plugin = MythicPrison.getInstance();
    
    switch (milestoneType) {
        case BLOCKS_MINED:
            return statsManager.getBlocksMined(minestomPlayer);
            
        case MONEY_EARNED:
            return (long) statsManager.getTotalMoneyEarned(minestomPlayer);
            
        case TOKENS_EARNED:
            // Get tokens from CurrencyManager using generic getBalance method
            if (plugin.getCurrencyManager() != null) {
                return (long) plugin.getCurrencyManager().getBalance(minestomPlayer, "tokens");
            }
            return 0;
            
        case RANK_REACHED:
            // Convert rank string to numeric value for comparison
            if (plugin.getRankingManager() != null) {
                String currentRank = plugin.getRankingManager().getRank(minestomPlayer);
                return convertRankToNumeric(currentRank);
            }
            return 0;
            
        case PRESTIGE_REACHED:
            // Get prestige level from RankingManager
            if (plugin.getRankingManager() != null) {
                return plugin.getRankingManager().getPrestige(minestomPlayer);
            }
            return 0;
            
        default:
            System.err.println("Unknown milestone type: " + milestoneType);
            return 0;
    }
}

/**
 * Converts rank string to numeric value for milestone comparison
 * A=1, B=2, ..., Z=26, AA=27, BB=28, ..., ZZ=52, AAA=53, etc.
 */
private long convertRankToNumeric(String rank) {
    if (rank == null || rank.isEmpty()) {
        return 1; // Default to rank A = 1
    }
    
    // Single letter ranks (A-Z = 1-26)
    if (rank.length() == 1) {
        return rank.charAt(0) - 'A' + 1;
    }
    
    // Double letter ranks (AA-ZZ = 27-52)
    if (rank.length() == 2 && rank.charAt(0) == rank.charAt(1)) {
        return 26 + (rank.charAt(0) - 'A' + 1);
    }
    
    // Triple letter ranks (AAA-ZZZ = 53-78)
    if (rank.length() == 3 && rank.charAt(0) == rank.charAt(1) && rank.charAt(1) == rank.charAt(2)) {
        return 52 + (rank.charAt(0) - 'A' + 1);
    }
    
    // Fallback for any other format
    return 1;
}

    public PlayerMilestones getPlayerMilestones(Object player) {
        String uuid = getPlayerUUID(player);
        return playerMilestones.get(uuid);
    }

    public List<Milestone> getAvailableMilestones() {
        return new ArrayList<>(availableMilestones);
    }

    public List<Milestone> getCompletedMilestones(Object player) {
        String uuid = getPlayerUUID(player);
        PlayerMilestones milestones = playerMilestones.get(uuid);
        if (milestones == null) return new ArrayList<>();

        return availableMilestones.stream()
                .filter(m -> milestones.isCompleted(m.getId()))
                .toList();
    }

    public List<Milestone> getIncompleteMilestones(Object player) {
        String uuid = getPlayerUUID(player);
        PlayerMilestones milestones = playerMilestones.get(uuid);
        if (milestones == null) return new ArrayList<>(availableMilestones);

        return availableMilestones.stream()
                .filter(m -> !milestones.isCompleted(m.getId()))
                .sorted(Comparator.comparingInt(Milestone::getOrder))
                .toList();
    }

    public int getCompletedCount(Object player) {
        return getCompletedMilestones(player).size();
    }

    public double getCompletionPercentage(Object player) {
        if (availableMilestones.isEmpty()) return 0.0;
        return (double) getCompletedCount(player) / availableMilestones.size() * 100.0;
    }

    public Milestone getMilestone(String id) {
        return availableMilestones.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void broadcastToAll(String message) {
    try {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            ChatUtil.sendMessage(player, message);
        }
    } catch (Exception e) {
        System.err.println("Error broadcasting message: " + e.getMessage());
    }
}

    private String getPlayerUUID(Object player) {
        if (player instanceof Player p) {
            return p.getUuid().toString();
        }
        return "";
    }

    private String getPlayerUsername(Object player) {
        if (player instanceof Player p) {
            return p.getUsername();
        }
        return "";
    }
}