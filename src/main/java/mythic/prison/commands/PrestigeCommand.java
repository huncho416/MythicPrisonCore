package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class PrestigeCommand extends Command {

    public PrestigeCommand() {
        super("prestige");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;

            // Check if player can prestige
            RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
            if (rankingManager.canPrestige(player)) {
                executePrestige(player);
            } else {
                showPrestigeInfo(player);
            }
        });
    }

    private void showPrestigeInfo(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        String currentRank = rankingManager.getRank(player);
        int currentPrestige = rankingManager.getPrestige(player);
        double cost = rankingManager.getPrestigeCost(player);
        double balance = MythicPrison.getInstance().getCurrencyManager().getBalance(player, "money");

        ChatUtil.sendMessage(player, "§6§l§m                §r §6§lPRESTIGE §r§6§l§m                ");
        ChatUtil.sendMessage(player, "");

        if (!currentRank.equals("Z")) {
            ChatUtil.sendMessage(player, "§c✗ You are not eligible for Prestige yet.");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lCurrent Rank: §e" + currentRank);
            ChatUtil.sendMessage(player, "§f§lRequired Rank: §eZ");
            ChatUtil.sendMessage(player, "§f§lCurrent Prestige: §6" + currentPrestige);
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§7You must reach rank §eZ §7to prestige!");
            ChatUtil.sendMessage(player, "§7Continue ranking up with §d/rankup §7or §d/rankupmax");
        } else {
            // Player is rank Z but can't afford it
            ChatUtil.sendMessage(player, "§c✗ You cannot afford to prestige yet.");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lCurrent Rank: §e" + currentRank + " §a✓");
            ChatUtil.sendMessage(player, "§f§lCurrent Prestige: §6" + currentPrestige);
            ChatUtil.sendMessage(player, "§f§lPrestige Cost: §c$" + ChatUtil.formatMoney(cost));
            ChatUtil.sendMessage(player, "§f§lYour Balance: §a$" + ChatUtil.formatMoney(balance));
            ChatUtil.sendMessage(player, "§f§lYou Need: §c$" + ChatUtil.formatMoney(cost - balance) + " §7more");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§7Keep mining to earn more money!");
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§lPrestige Benefits:");
        ChatUtil.sendMessage(player, "§7• Reset rank to §eA §7but keep prestige level");
        ChatUtil.sendMessage(player, "§7• Gain §610% §7permanent money multiplier");
        ChatUtil.sendMessage(player, "§7• Unlock prestige-only features");
        ChatUtil.sendMessage(player, "§7• Earn §dSouls §7currency");
        ChatUtil.sendMessage(player, "§a• Keep all your currencies!");
        ChatUtil.sendMessage(player, "§7• Cost: §c$" + ChatUtil.formatMoney(cost));

        if (currentPrestige + 1 >= 10) {
            ChatUtil.sendMessage(player, "§d• At 10 prestiges: Unlock Rebirth system!");
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§l§m                                                ");
    }

    private void executePrestige(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        if (!rankingManager.canPrestige(player)) {
            String currentRank = rankingManager.getRank(player);
            double cost = rankingManager.getPrestigeCost(player);
            double balance = MythicPrison.getInstance().getCurrencyManager().getBalance(player, "money");

            if (!currentRank.equals("Z")) {
                ChatUtil.sendError(player, "You cannot prestige yet! You must reach rank Z first.");
            } else {
                ChatUtil.sendError(player, "You need $" + ChatUtil.formatMoney(cost) + " to prestige!");
                ChatUtil.sendMessage(player, "§7You have: §a$" + ChatUtil.formatMoney(balance));
                ChatUtil.sendMessage(player, "§7You need: §c$" + ChatUtil.formatMoney(cost - balance) + " §7more");
            }
            return;
        }

        int oldPrestige = rankingManager.getPrestige(player);
        double cost = rankingManager.getPrestigeCost(player);

        if (rankingManager.prestige(player)) {
            int newPrestige = rankingManager.getPrestige(player);
            double prestigeReward = 1000000 * Math.pow(2, oldPrestige);

            // Show success message
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§6§l§m        §r §6§lPRESTIGE COMPLETE! §r§6§l§m        ");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lCongratulations on your prestige!");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§a§lNew Status:");
            ChatUtil.sendMessage(player, "§7Rank: §eA §7(Reset)");
            ChatUtil.sendMessage(player, "§7Prestige: §6§l" + newPrestige + " §7(+1)");
            ChatUtil.sendMessage(player, "§a§lCurrencies: §aKept!");
            ChatUtil.sendMessage(player, "§7Cost Paid: §c$" + ChatUtil.formatMoney(cost));
            ChatUtil.sendMessage(player, "§7Reward: §d+" + ChatUtil.formatMoney(prestigeReward) + " Souls");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§6§lPrestige Benefits:");
            ChatUtil.sendMessage(player, "§a• §610% §aperma money multiplier");
            ChatUtil.sendMessage(player, "§a• Prestige prefix: §6✦" + newPrestige);
            ChatUtil.sendMessage(player, "§a• Access to §dSouls §acurrency");
            ChatUtil.sendMessage(player, "§a• Unlock prestige features");
            if (newPrestige >= 10) {
                ChatUtil.sendMessage(player, "§d• Rebirth system unlocked!");
            }
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lNext Steps:");
            ChatUtil.sendMessage(player, "§d/rankup §7- Start ranking again with bonuses");
            ChatUtil.sendMessage(player, "§d/mine §7- Mine with enhanced rewards");
            if (newPrestige >= 10) {
                ChatUtil.sendMessage(player, "§5/rebirth §7- Check rebirth requirements");
            }
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§6§l§m                                        ");

            // Broadcast for milestone prestiges
            if (newPrestige % 5 == 0 || newPrestige == 1) {
                broadcastPrestige(player, newPrestige);
            }
        } else {
            ChatUtil.sendError(player, "Failed to prestige! You may not have enough money.");
            double currentBalance = MythicPrison.getInstance().getCurrencyManager().getBalance(player, "money");
            ChatUtil.sendMessage(player, "§7Required: §c$" + ChatUtil.formatMoney(cost));
            ChatUtil.sendMessage(player, "§7You have: §a$" + ChatUtil.formatMoney(currentBalance));
        }
    }

    private void broadcastPrestige(Player player, int prestigeLevel) {
        String message = "§6§l[PRESTIGE] §f" + player.getUsername() + " §7has prestiged! Prestige: §6§l✦" + prestigeLevel;

        var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
        for (Player onlinePlayer : connectionManager.getOnlinePlayers()) {
            ChatUtil.sendMessage(onlinePlayer, message);
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new PrestigeCommand());
    }
}