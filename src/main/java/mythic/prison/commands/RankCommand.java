
package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class RankCommand extends Command {

    public RankCommand() {
        super("rank");

        // Default executor - show player's rank info
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showRankInfo((Player) sender);
        });

        // /rank <player> - show another player's rank
        ArgumentString playerArg = ArgumentType.String("player");
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);

            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player targetPlayer = findPlayerByName(targetName);
            if (targetPlayer == null) {
                ChatUtil.sendError(sender, "Player not found: " + targetName);
                return;
            }

            showRankInfo((Player) sender, targetPlayer);
        }, playerArg);
    }

    private static void showRankInfo(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        String currentRank = rankingManager.getRank(player);
        int prestige = rankingManager.getPrestige(player);
        String formattedRank = rankingManager.getFormattedRank(player);

        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lRANK INFO §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lPlayer: §a" + player.getUsername());
        ChatUtil.sendMessage(player, "§f§lRank: " + formattedRank);
        ChatUtil.sendMessage(player, "§f§lPrestige: §6" + prestige);
        ChatUtil.sendMessage(player, "");

        // Show rank progress
        showRankProgress(player, rankingManager, currentRank);

        ChatUtil.sendMessage(player, "§f§lCommands:");
        ChatUtil.sendMessage(player, "§d/rankup §7- Rank up to the next rank");
        ChatUtil.sendMessage(player, "§d/prestige §7- View prestige information");
        if (currentRank.equals("Z")) {
            ChatUtil.sendMessage(player, "§d/ascension §7- Ascend to next prestige");
        }
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                ");
    }

    private static void showRankInfo(Player viewer, Player target) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        String currentRank = rankingManager.getRank(target);
        int prestige = rankingManager.getPrestige(target);
        String formattedRank = rankingManager.getFormattedRank(target);

        ChatUtil.sendMessage(viewer, "§d§l§m            §r §d§lRANK INFO §r§d§l§m            ");
        ChatUtil.sendMessage(viewer, "");
        ChatUtil.sendMessage(viewer, "§f§lPlayer: §a" + target.getUsername());
        ChatUtil.sendMessage(viewer, "§f§lRank: " + formattedRank);
        ChatUtil.sendMessage(viewer, "§f§lPrestige: §6" + prestige);
        ChatUtil.sendMessage(viewer, "");

        // Show additional stats if viewing another player
        if (!viewer.equals(target)) {
            showPlayerStats(viewer, target);
        } else {
            showRankProgress(viewer, rankingManager, currentRank);
        }

        ChatUtil.sendMessage(viewer, "§d§l§m                                                ");
    }

    private static void showRankProgress(Player player, RankingManager rankingManager, String currentRank) {
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        double currentMoney = currencyManager.getBalance(player, "money");

        if (currentRank.equals("Z")) {
            ChatUtil.sendMessage(player, "§a§lYou are at the maximum rank!");
            ChatUtil.sendMessage(player, "§7Use §f/prestige §7to see prestige options.");
            ChatUtil.sendMessage(player, "");
            return;
        }

        // Calculate next rank cost
        double nextRankCost = getRankUpCost(currentRank);
        String nextRank = getNextRank(currentRank);

        ChatUtil.sendMessage(player, "§f§lRank Progress:");
        ChatUtil.sendMessage(player, "§7Next rank: §e" + nextRank);
        ChatUtil.sendMessage(player, "§7Cost: §a$" + ChatUtil.formatMoney(nextRankCost));
        ChatUtil.sendMessage(player, "§7Your money: §a$" + ChatUtil.formatMoney(currentMoney));

        // Show progress bar
        float progress = Math.min(1.0f, (float) (currentMoney / nextRankCost));
        showProgressBar(player, progress);

        if (currentMoney >= nextRankCost) {
            ChatUtil.sendMessage(player, "§a§lYou can rank up! Use §f/rankup");
        } else {
            double needed = nextRankCost - currentMoney;
            ChatUtil.sendMessage(player, "§7Still need: §c$" + ChatUtil.formatMoney(needed));
        }
        ChatUtil.sendMessage(player, "");
    }

    private static void showPlayerStats(Player viewer, Player target) {
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

        ChatUtil.sendMessage(viewer, "§f§lPlayer Statistics:");

        // Show approximate wealth (don't show exact amounts for privacy)
        double money = currencyManager.getBalance(target, "money");
        String wealthCategory = getWealthCategory(money);
        ChatUtil.sendMessage(viewer, "§7Wealth level: §f" + wealthCategory);

        // Show achievements or other public stats
        ChatUtil.sendMessage(viewer, "§7Use §f/leaderboard §7to see rankings.");
        ChatUtil.sendMessage(viewer, "");
    }

    private static void showProgressBar(Player player, float progress) {
        int barLength = 20;
        int filledBars = (int) (progress * barLength);

        StringBuilder progressBar = new StringBuilder("§7[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                progressBar.append("§a■");
            } else {
                progressBar.append("§7■");
            }
        }
        progressBar.append("§7] §f").append(String.format("%.1f", progress * 100)).append("%");

        ChatUtil.sendMessage(player, progressBar.toString());
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
        String[] ranks = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        int rankIndex = java.util.Arrays.asList(ranks).indexOf(rank);
        return 1000 * Math.pow(1.5, rankIndex);
    }

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

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new RankCommand());
    }
}