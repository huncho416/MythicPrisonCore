
package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats");

        // Default executor - show own stats
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            showPlayerStats(player, player);
        });

        // /stats <player> - show another player's stats
        ArgumentString playerArg = ArgumentType.String("player");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player viewer = (Player) sender;
            String targetName = context.get(playerArg);

            Player targetPlayer = findPlayerByName(targetName);
            if (targetPlayer == null) {
                ChatUtil.sendError(viewer, "Player not found: " + targetName);
                return;
            }

            showPlayerStats(viewer, targetPlayer);
        }, playerArg);
    }

    private static void showPlayerStats(Player viewer, Player target) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

        boolean isOwnStats = viewer.equals(target);
        String playerName = target.getUsername();

        // Header
        ChatUtil.sendMessage(viewer, "§b§l§m            §r §b§lPLAYER STATS §r§b§l§m            ");
        ChatUtil.sendMessage(viewer, "");
        ChatUtil.sendMessage(viewer, "§f§lPlayer: §a" + playerName);
        ChatUtil.sendMessage(viewer, "");

        // Basic rank information
        showBasicRankInfo(viewer, target, rankingManager);

        // Detailed stats based on whether viewing own or others
        if (isOwnStats) {
            showDetailedPersonalStats(viewer, target, currencyManager, rankingManager);
        } else {
            showPublicStats(viewer, target, currencyManager, rankingManager);
        }

        // Progress information
        showProgressInfo(viewer, target, rankingManager, currencyManager, isOwnStats);

        // Footer
        ChatUtil.sendMessage(viewer, "");
        ChatUtil.sendMessage(viewer, "§b§l§m                                                    ");
    }

    private static void showBasicRankInfo(Player viewer, Player target, RankingManager rankingManager) {
        String currentRank = rankingManager.getRank(target);
        int prestige = rankingManager.getPrestige(target);
        String formattedRank = rankingManager.getFormattedRank(target);

        ChatUtil.sendMessage(viewer, "§f§lRank Information:");
        ChatUtil.sendMessage(viewer, "§7Current Rank: " + formattedRank);
        ChatUtil.sendMessage(viewer, "§7Prestige Level: §6" + prestige);

        // Calculate total rank progression
        int rankIndex = getRankIndex(currentRank);
        int totalRanks = 26; // A-Z
        double rankProgress = (double) rankIndex / totalRanks * 100;

        ChatUtil.sendMessage(viewer, "§7Rank Progress: §e" + String.format("%.1f", rankProgress) + "%");
        ChatUtil.sendMessage(viewer, "");
    }

    private static void showDetailedPersonalStats(Player viewer, Player target, CurrencyManager currencyManager, RankingManager rankingManager) {
        // Personal detailed stats (only for own stats)
        ChatUtil.sendMessage(viewer, "§f§lFinancial Information:");

        double money = currencyManager.getBalance(target, "money");
        ChatUtil.sendMessage(viewer, "§7Money: §a$" + ChatUtil.formatMoney(money));

        // Show other currencies if they exist
        showOtherCurrencies(viewer, target, currencyManager);

        ChatUtil.sendMessage(viewer, "");

        // Achievement/milestone information
        showAchievements(viewer, target, rankingManager);
    }

    private static void showPublicStats(Player viewer, Player target, CurrencyManager currencyManager, RankingManager rankingManager) {
        // Public stats (when viewing other players)
        ChatUtil.sendMessage(viewer, "§f§lPublic Information:");

        double money = currencyManager.getBalance(target, "money");
        String wealthCategory = getWealthCategory(money);
        ChatUtil.sendMessage(viewer, "§7Wealth Level: §f" + wealthCategory);

        // Show public achievements
        showPublicAchievements(viewer, target, rankingManager);

        ChatUtil.sendMessage(viewer, "");
    }

    private static void showOtherCurrencies(Player viewer, Player target, CurrencyManager currencyManager) {
        // Show other currencies if they exist in the system
        try {
            double tokens = currencyManager.getBalance(target, "tokens");
            if (tokens > 0) {
                ChatUtil.sendMessage(viewer, "§7Tokens: §d" + ChatUtil.formatMoney(tokens));
            }
        } catch (Exception e) {
            // Tokens currency might not exist
        }

        try {
            double gems = currencyManager.getBalance(target, "gems");
            if (gems > 0) {
                ChatUtil.sendMessage(viewer, "§7Gems: §b" + ChatUtil.formatMoney(gems));
            }
        } catch (Exception e) {
            // Gems currency might not exist
        }
    }

    private static void showProgressInfo(Player viewer, Player target, RankingManager rankingManager, CurrencyManager currencyManager, boolean isPersonal) {
        String currentRank = rankingManager.getRank(target);

        if (currentRank.equals("Z")) {
            ChatUtil.sendMessage(viewer, "§f§lProgression Status:");
            ChatUtil.sendMessage(viewer, "§a§lMaximum rank reached!");

            if (isPersonal) {
                int prestige = rankingManager.getPrestige(target);
                double rebirthCost = getRebirthCost(prestige);
                double currentMoney = currencyManager.getBalance(target, "money");

                ChatUtil.sendMessage(viewer, "§7Next Goal: §dRebirth to Prestige " + (prestige + 1));
                ChatUtil.sendMessage(viewer, "§7Rebirth Cost: §a$" + ChatUtil.formatMoney(rebirthCost));

                if (currentMoney >= rebirthCost) {
                    ChatUtil.sendMessage(viewer, "§a§lReady to rebirth! Use §f/rebirth");
                } else {
                    double needed = rebirthCost - currentMoney;
                    ChatUtil.sendMessage(viewer, "§7Still need: §c$" + ChatUtil.formatMoney(needed));

                    // Show progress bar for rebirth
                    showProgressBar(viewer, currentMoney, rebirthCost, "Rebirth Progress");
                }
            }
        } else {
            String nextRank = getNextRank(currentRank);
            double rankUpCost = getRankUpCost(currentRank);

            ChatUtil.sendMessage(viewer, "§f§lProgression Status:");
            ChatUtil.sendMessage(viewer, "§7Next Rank: §e" + nextRank);
            ChatUtil.sendMessage(viewer, "§7Rank Up Cost: §a$" + ChatUtil.formatMoney(rankUpCost));

            if (isPersonal) {
                double currentMoney = currencyManager.getBalance(target, "money");

                if (currentMoney >= rankUpCost) {
                    ChatUtil.sendMessage(viewer, "§a§lReady to rank up! Use §f/rankup");
                } else {
                    double needed = rankUpCost - currentMoney;
                    ChatUtil.sendMessage(viewer, "§7Still need: §c$" + ChatUtil.formatMoney(needed));

                    // Show progress bar for rank up
                    showProgressBar(viewer, currentMoney, rankUpCost, "Rank Up Progress");
                }
            }
        }
    }

    private static void showAchievements(Player viewer, Player target, RankingManager rankingManager) {
        ChatUtil.sendMessage(viewer, "§f§lAchievements:");

        String currentRank = rankingManager.getRank(target);
        int prestige = rankingManager.getPrestige(target);
        int rankIndex = getRankIndex(currentRank);

        // Rank-based achievements
        if (rankIndex >= 5) ChatUtil.sendMessage(viewer, "§a✓ Reached Rank F+");
        if (rankIndex >= 12) ChatUtil.sendMessage(viewer, "§a✓ Reached Rank M+");
        if (rankIndex >= 19) ChatUtil.sendMessage(viewer, "§a✓ Reached Rank T+");
        if (currentRank.equals("Z")) ChatUtil.sendMessage(viewer, "§6✓ Maximum Rank Achieved!");

        // Prestige-based achievements
        if (prestige >= 1) ChatUtil.sendMessage(viewer, "§d✓ First Rebirth");
        if (prestige >= 5) ChatUtil.sendMessage(viewer, "§d✓ Veteran (5+ Prestiges)");
        if (prestige >= 10) ChatUtil.sendMessage(viewer, "§d✓ Expert (10+ Prestiges)");
        if (prestige >= 25) ChatUtil.sendMessage(viewer, "§6✓ Legend (25+ Prestiges)");
        if (prestige >= 50) ChatUtil.sendMessage(viewer, "§c✓ Ultimate (50+ Prestiges)");

        ChatUtil.sendMessage(viewer, "");
    }

    private static void showPublicAchievements(Player viewer, Player target, RankingManager rankingManager) {
        ChatUtil.sendMessage(viewer, "§f§lNotable Achievements:");

        String currentRank = rankingManager.getRank(target);
        int prestige = rankingManager.getPrestige(target);

        if (currentRank.equals("Z")) {
            ChatUtil.sendMessage(viewer, "§6✓ Maximum Rank Master");
        }

        if (prestige >= 25) {
            ChatUtil.sendMessage(viewer, "§6✓ Legendary Player");
        } else if (prestige >= 10) {
            ChatUtil.sendMessage(viewer, "§d✓ Expert Player");
        } else if (prestige >= 5) {
            ChatUtil.sendMessage(viewer, "§d✓ Veteran Player");
        } else if (prestige >= 1) {
            ChatUtil.sendMessage(viewer, "§a✓ Reborn Player");
        }
    }

    private static void showProgressBar(Player viewer, double current, double required, String label) {
        float progress = Math.min(1.0f, (float) (current / required));
        int barLength = 15;
        int filledBars = (int) (progress * barLength);

        StringBuilder progressBar = new StringBuilder("§7" + label + ": [");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                progressBar.append("§a■");
            } else {
                progressBar.append("§7■");
            }
        }
        progressBar.append("§7] §f").append(String.format("%.1f", progress * 100)).append("%");

        ChatUtil.sendMessage(viewer, progressBar.toString());
    }

    private static String getWealthCategory(double money) {
        if (money < 10000) return "§7Beginner";
        if (money < 100000) return "§f§lNovice";
        if (money < 1000000) return "§a§lRising";
        if (money < 10000000) return "§e§lProsperous";
        if (money < 100000000) return "§6§lWealthy";
        if (money < 1000000000) return "§c§lRich";
        return "§d§lMagnate";
    }

    // Helper methods
    private static Player findPlayerByName(String playerName) {
        try {
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            var players = connectionManager.getOnlinePlayers();

            for (Player player : players) {
                if (player.getUsername().equalsIgnoreCase(playerName)) {
                    return player;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding player: " + e.getMessage());
        }
        return null;
    }

    private static String getNextRank(String currentRank) {
        String[] ranks = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        for (int i = 0; i < ranks.length - 1; i++) {
            if (ranks[i].equals(currentRank)) {
                return ranks[i + 1];
            }
        }
        return null; // Already at max rank
    }

    private static double getRankUpCost(String rank) {
        int rankIndex = getRankIndex(rank);
        return 1000 * Math.pow(1.5, rankIndex);
    }

    private static int getRankIndex(String rank) {
        String[] ranks = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        return java.util.Arrays.asList(ranks).indexOf(rank);
    }

    private static double getRebirthCost(int currentPrestige) {
        return 1000000 * Math.pow(2, currentPrestige);
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new StatsCommand());
    }
}