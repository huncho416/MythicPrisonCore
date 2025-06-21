package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardCommand extends Command {

    public LeaderboardCommand() {
        super("leaderboard", "lb", "top");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showLeaderboardHelp(sender);
        });

        // /leaderboard money
        ArgumentWord moneyArg = ArgumentType.Word("money").from("money", "bal", "balance");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showMoneyLeaderboard(sender);
        }, moneyArg);

        // /leaderboard tokens
        ArgumentWord tokensArg = ArgumentType.Word("tokens").from("tokens", "token");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showTokensLeaderboard(sender);
        }, tokensArg);

        // /leaderboard gems
        ArgumentWord gemsArg = ArgumentType.Word("gems").from("gems", "gem");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showGemsLeaderboard(sender);
        }, gemsArg);

        // /leaderboard rank
        ArgumentWord rankArg = ArgumentType.Word("rank").from("rank", "ranks");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showRankLeaderboard(sender);
        }, rankArg);

        // /leaderboard prestige
        ArgumentWord prestigeArg = ArgumentType.Word("prestige").from("prestige", "prestiges");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showPrestigeLeaderboard(sender);
        }, prestigeArg);

        // /leaderboard blocks
        ArgumentWord blocksArg = ArgumentType.Word("blocks").from("blocks", "block", "mined");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showBlocksLeaderboard(sender);
        }, blocksArg);

        // /leaderboard playtime
        ArgumentWord playtimeArg = ArgumentType.Word("playtime").from("playtime", "time", "played");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showPlaytimeLeaderboard(sender);
        }, playtimeArg);
    }

    private static void showLeaderboardHelp(Object player) {
        Player p = (Player) player;
        ChatUtil.sendMessage(p, "§6§l§m            §r §6§lLEADERBOARDS §r§6§l§m            ");
        ChatUtil.sendMessage(p, "");
        ChatUtil.sendMessage(p, "§e/leaderboard money §7- Top players by money");
        ChatUtil.sendMessage(p, "§e/leaderboard tokens §7- Top players by tokens");
        ChatUtil.sendMessage(p, "§e/leaderboard gems §7- Top players by gems");
        ChatUtil.sendMessage(p, "§e/leaderboard rank §7- Top players by rank");
        ChatUtil.sendMessage(p, "§e/leaderboard prestige §7- Top players by prestige");
        ChatUtil.sendMessage(p, "§e/leaderboard blocks §7- Top players by blocks mined");
        ChatUtil.sendMessage(p, "§e/leaderboard playtime §7- Top players by playtime");
        ChatUtil.sendMessage(p, "");
        ChatUtil.sendMessage(p, "§6§l§m                                                    ");
    }

    private static void showMoneyLeaderboard(Object player) {
        List<LeaderboardEntry> entries = getTopPlayersByCurrency("money", 10);
        displayLeaderboard(player, "MONEY LEADERBOARD", "§a", entries, "money");
    }

    private static void showTokensLeaderboard(Object player) {
        List<LeaderboardEntry> entries = getTopPlayersByCurrency("tokens", 10);
        displayLeaderboard(player, "TOKENS LEADERBOARD", "§b", entries, "tokens");
    }

    private static void showGemsLeaderboard(Object player) {
        List<LeaderboardEntry> entries = getTopPlayersByCurrency("souls", 10);
        displayLeaderboard(player, "SOULS LEADERBOARD", "§d", entries, "souls");
    }

    private static void showRankLeaderboard(Object player) {
        Player p = (Player) player;
        ChatUtil.sendMessage(p, "§c§lRANK LEADERBOARD");
        ChatUtil.sendMessage(p, "§7This feature is coming soon!");
    }

    private static void showPrestigeLeaderboard(Object player) {
        Player p = (Player) player;
        ChatUtil.sendMessage(p, "§c§lPRESTIGE LEADERBOARD");
        ChatUtil.sendMessage(p, "§7This feature is coming soon!");
    }

    private static void showBlocksLeaderboard(Object player) {
        Player p = (Player) player;
        ChatUtil.sendMessage(p, "§c§lBLOCKS LEADERBOARD");
        ChatUtil.sendMessage(p, "§7This feature is coming soon!");
    }

    private static void showPlaytimeLeaderboard(Object player) {
        Player p = (Player) player;
        ChatUtil.sendMessage(p, "§c§lPLAYTIME LEADERBOARD");
        ChatUtil.sendMessage(p, "§7This feature is coming soon!");
    }

    private static List<LeaderboardEntry> getTopPlayersByCurrency(String currency, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();

        try {
            // Get all online players and their balances
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            var onlinePlayers = connectionManager.getOnlinePlayers();

            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

            // Create entries for all online players
            for (Player player : onlinePlayers) {
                double balance = currencyManager.getBalance(player, currency);
                if (balance > 0) { // Only include players with positive balance
                    entries.add(new LeaderboardEntry(player.getUsername(), balance));
                }
            }

            // Sort by balance (highest first) and limit results
            return entries.stream()
                    .sorted((e1, e2) -> Double.compare(e2.value, e1.value))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting leaderboard data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void displayLeaderboard(Object player, String title, String color, List<LeaderboardEntry> entries, String currency) {
        Player p = (Player) player;

        ChatUtil.sendMessage(p, color + "§l§m            §r " + color + "§l" + title + " §r" + color + "§l§m            ");
        ChatUtil.sendMessage(p, "");

        if (entries.isEmpty()) {
            ChatUtil.sendMessage(p, "§7No data available yet!");
        } else {
            for (int i = 0; i < entries.size(); i++) {
                LeaderboardEntry entry = entries.get(i);
                String positionColor = getPositionColor(i + 1);
                String formattedValue = currency.equals("money") ?
                        ChatUtil.formatMoney(entry.value) :
                        String.format("%.0f", entry.value);

                ChatUtil.sendMessage(p, positionColor + (i + 1) + ". §f" + entry.username + " §7- " + color + formattedValue);
            }
        }

        ChatUtil.sendMessage(p, "");
        ChatUtil.sendMessage(p, color + "§l§m                                                    ");
    }

    private static String getPositionColor(int position) {
        return switch (position) {
            case 1 -> "§6"; // Gold
            case 2 -> "§7"; // Silver
            case 3 -> "§c"; // Bronze
            default -> "§f"; // White
        };
    }

    private static String getPlayerUsername(Object player) {
        if (player instanceof Player p) {
            return p.getUsername();
        }
        return "Unknown";
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new LeaderboardCommand());
    }

    private static class LeaderboardEntry {
        final String username;
        final double value;

        LeaderboardEntry(String username, double value) {
            this.username = username;
            this.value = value;
        }
    }
}