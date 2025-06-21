package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class RankupCommand extends Command {

    public RankupCommand() {
        super("rankup", "ru");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeRankup(player);
        });
    }

    private void executeRankup(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        if (!rankingManager.canRankup(player)) {
            showRankupInfo(player);
            return;
        }

        String oldRank = rankingManager.getRank(player);
        double cost = rankingManager.getRankupCost(player);

        if (rankingManager.rankup(player)) {
            String newRank = rankingManager.getRank(player);

            ChatUtil.sendSuccess(player, "§d§lRANKUP SUCCESS§r§a Successfully ranked up!");
            ChatUtil.sendMessage(player, "§7Went from §e" + oldRank + " §7to §e" + newRank + "§7!");
            ChatUtil.sendMessage(player, "§7Cost: §a$" + ChatUtil.formatMoney(cost));

            double newBalance = MythicPrison.getInstance().getCurrencyManager().getBalance(player, "money");
            ChatUtil.sendMessage(player, "§7New balance: §a$" + ChatUtil.formatMoney(newBalance));

            // Check if they can rank up again
            if (rankingManager.canRankup(player)) {
                String nextRank = rankingManager.getNextRankName(player);
                double nextCost = rankingManager.getRankupCost(player);
                ChatUtil.sendMessage(player, "§7Next rank: §e" + nextRank + " §7(§a$" + ChatUtil.formatMoney(nextCost) + "§7)");
                ChatUtil.sendMessage(player, "§7Use §d/rankupmax §7to rank up multiple times!");
            } else if (newRank.equals("Z")) {
                ChatUtil.sendMessage(player, "§6§lCongratulations! You've reached the maximum rank!");
                if (rankingManager.canPrestige(player)) {
                    ChatUtil.sendMessage(player, "§6You can now prestige! Use §f/prestige");
                } else {
                    double prestigeCost = rankingManager.getPrestigeCost(player);
                    ChatUtil.sendMessage(player, "§7Prestige cost: §c$" + ChatUtil.formatMoney(prestigeCost));
                    ChatUtil.sendMessage(player, "§7Save up to prestige with §f/prestige");
                }
            }
        } else {
            ChatUtil.sendError(player, "Failed to rank up! You may not have enough money.");
        }
    }

    private void showRankupInfo(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

        String currentRank = rankingManager.getRank(player);
        String nextRank = rankingManager.getNextRankName(player);

        ChatUtil.sendMessage(player, "§e§l§m            §r §e§lRANKUP INFO §r§e§l§m            ");
        ChatUtil.sendMessage(player, "");

        if (nextRank == null) {
            // At max rank
            ChatUtil.sendMessage(player, "§c§lYou are at the maximum rank!");
            ChatUtil.sendMessage(player, "§7Current rank: §e" + currentRank);
            ChatUtil.sendMessage(player, "");

            int prestige = rankingManager.getPrestige(player);
            if (rankingManager.canPrestige(player)) {
                ChatUtil.sendMessage(player, "§6§lYou can prestige! Use §f/prestige");
                double prestigeCost = rankingManager.getPrestigeCost(player);
                ChatUtil.sendMessage(player, "§7Prestige cost: §c$" + ChatUtil.formatMoney(prestigeCost));
            } else {
                ChatUtil.sendMessage(player, "§7Current prestige: §6" + prestige);
                double prestigeCost = rankingManager.getPrestigeCost(player);
                double balance = currencyManager.getBalance(player, "money");
                if (balance < prestigeCost) {
                    ChatUtil.sendMessage(player, "§7You need §c$" + ChatUtil.formatMoney(prestigeCost - balance) + " §7more to prestige.");
                }
            }
        } else {
            // Can potentially rank up
            double cost = rankingManager.getRankupCost(player);
            double currentBalance = currencyManager.getBalance(player, "money");

            ChatUtil.sendMessage(player, "§f§lCurrent Rank: §e" + currentRank);
            ChatUtil.sendMessage(player, "§f§lNext Rank: §e" + nextRank);
            ChatUtil.sendMessage(player, "§f§lCost: §a$" + ChatUtil.formatMoney(cost));
            ChatUtil.sendMessage(player, "§f§lYour Balance: §a$" + ChatUtil.formatMoney(currentBalance));
            ChatUtil.sendMessage(player, "");

            if (currentBalance >= cost) {
                ChatUtil.sendMessage(player, "§a§l✓ You can afford this rankup!");
                ChatUtil.sendMessage(player, "§7Use §e/rankup §7to rank up now");
            } else {
                double needed = cost - currentBalance;
                ChatUtil.sendMessage(player, "§c§l✗ You cannot afford this rankup yet.");
                ChatUtil.sendMessage(player, "§7Still need: §c$" + ChatUtil.formatMoney(needed));
                ChatUtil.sendMessage(player, "§7Tip: Mine blocks to earn money!");
            }

            // Show progress bar
            float progress = Math.min(1.0f, (float) (currentBalance / cost));
            showProgressBar(player, progress);
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCommands:");
        ChatUtil.sendMessage(player, "§e/rankup §7- Rank up once");
        ChatUtil.sendMessage(player, "§e/rankupmax §7- Rank up as many times as possible");
        ChatUtil.sendMessage(player, "§e/rank §7- View detailed rank information");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§l§m                                                ");
    }

    private void showProgressBar(Player player, float progress) {
        int barLength = 20;
        int filledBars = (int) (progress * barLength);

        StringBuilder progressBar = new StringBuilder("§7Progress: [");
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

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new RankupCommand());
    }
}