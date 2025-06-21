package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.data.player.PlayerProfile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class RebirthCommand extends Command {

    public RebirthCommand() {
        super("rebirth");

        // Default executor - execute rebirth if eligible, show info if not
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
            
            // Check if player can rebirth
            if (rankingManager.canRebirth(player)) {
                performRebirth(player, rankingManager);
            } else {
                showRebirthInfo(player, rankingManager);
            }
        });
    }

    private static void showRebirthInfo(Player player, RankingManager rankingManager) {
        String currentRank = rankingManager.getRank(player);
        int currentPrestige = rankingManager.getPrestige(player);
        int currentRebirth = rankingManager.getRebirth(player);

        ChatUtil.sendMessage(player, "§c§l§m            §r §c§lREBIRTH INFO §r§c§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCurrent Status:");
        ChatUtil.sendMessage(player, "§7Rank: " + rankingManager.getFormattedRank(player));
        ChatUtil.sendMessage(player, "§7Prestige: §6" + currentPrestige);
        ChatUtil.sendMessage(player, "§7Rebirth: §c" + currentRebirth);
        ChatUtil.sendMessage(player, "");

        ChatUtil.sendMessage(player, "§c✗ You cannot rebirth yet!");
        ChatUtil.sendMessage(player, "§7Requirements:");
        ChatUtil.sendMessage(player, "§7• Must have §610+ Prestiges §7(Current: §6" + currentPrestige + "§7)");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§7Continue prestiging to unlock rebirth!");
        ChatUtil.sendMessage(player, "§7Use §d/prestige §7when you reach rank Z.");
        ChatUtil.sendMessage(player, "");
        
        // Show rebirth benefits preview
        ChatUtil.sendMessage(player, "§c§lRebirth Benefits:");
        ChatUtil.sendMessage(player, "§7• Reset rank to §eA §7and prestige to §60");
        ChatUtil.sendMessage(player, "§7• Gain massive permanent multipliers");
        ChatUtil.sendMessage(player, "§7• Earn §eBeacons §7currency");
        ChatUtil.sendMessage(player, "§a• Keep all your currencies!");
        ChatUtil.sendMessage(player, "§7• Unlock rebirth-only features");
        
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l§m                                                    ");
    }

    private static void performRebirth(Player player, RankingManager rankingManager) {
        if (!rankingManager.canRebirth(player)) {
            ChatUtil.sendError(player, "You cannot rebirth yet! You need at least 10 prestiges.");
            return;
        }

        int currentRebirth = rankingManager.getRebirth(player);

        // Use the existing rebirth method from RankingManager
        if (rankingManager.rebirth(player)) {
            int newRebirth = currentRebirth + 1;
            double rebirthReward = 10000000 * Math.pow(3, currentRebirth);

            // Show success message
            showRebirthSuccess(player, newRebirth, rebirthReward);

            // Broadcast rebirth (optional)
            broadcastRebirth(player, newRebirth);
        } else {
            ChatUtil.sendError(player, "Failed to rebirth! Please try again.");
        }
    }

    private static void showRebirthSuccess(Player player, int newRebirth, double rebirthReward) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l§m        §r §c§lREBIRTH SUCCESSFUL! §r§c§l§m        ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCongratulations on your rebirth!");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§a§lNew Status:");
        ChatUtil.sendMessage(player, "§7Rank: §eA §7(Reset)");
        ChatUtil.sendMessage(player, "§7Prestige: §60 §7(Reset)");
        ChatUtil.sendMessage(player, "§7Rebirth: §c§l" + newRebirth + " §7(+1)");
        ChatUtil.sendMessage(player, "§a§lCurrencies: §aKept!");
        ChatUtil.sendMessage(player, "§7Reward: §e+" + ChatUtil.formatMoney(rebirthReward) + " Beacons");
        ChatUtil.sendMessage(player, "");

        ChatUtil.sendMessage(player, "§c§lRebirth Benefits:");
        showRebirthBenefits(player, newRebirth);
        ChatUtil.sendMessage(player, "");

        ChatUtil.sendMessage(player, "§f§lNext Steps:");
        ChatUtil.sendMessage(player, "§d/mine §7- Start mining with massive bonuses");
        ChatUtil.sendMessage(player, "§d/rankup §7- Begin ranking up again");
        ChatUtil.sendMessage(player, "§d/rank §7- Check your new rank display");
        if (newRebirth >= 5) {
            ChatUtil.sendMessage(player, "§5/ascension §7- Check ascension requirements");
        }
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l§m                                        ");
    }

    private static void showRebirthBenefits(Player player, int rebirthLevel) {
        // Calculate rebirth benefits
        double allMultiplier = rebirthLevel * 50.0; // 50% per rebirth
        double miningBonus = rebirthLevel * 25.0; // 25% per rebirth  
        double sellBonus = rebirthLevel * 30.0; // 30% per rebirth

        ChatUtil.sendMessage(player, "§a• All gains multiplier: +§e" + String.format("%.0f", allMultiplier) + "%");
        ChatUtil.sendMessage(player, "§a• Mining speed: +§e" + String.format("%.0f", miningBonus) + "%");
        ChatUtil.sendMessage(player, "§a• Sell price bonus: +§e" + String.format("%.0f", sellBonus) + "%");
        ChatUtil.sendMessage(player, "§a• Rebirth prefix: §c[R" + rebirthLevel + "]");
        ChatUtil.sendMessage(player, "§a• Access to §eBeacon §acurrency");

        // Special benefits for higher rebirths
        if (rebirthLevel >= 3) {
            ChatUtil.sendMessage(player, "§d• Access to rebirth-only areas");
        }
        if (rebirthLevel >= 5) {
            ChatUtil.sendMessage(player, "§d• Unlock §5Ascension §dsystem");
        }
        if (rebirthLevel >= 10) {
            ChatUtil.sendMessage(player, "§d• Exclusive rebirth commands");
        }
        if (rebirthLevel >= 25) {
            ChatUtil.sendMessage(player, "§6• Legendary rebirth benefits");
        }
    }

    private static void broadcastRebirth(Player player, int rebirthLevel) {
        // Broadcast for milestone rebirths
        boolean shouldBroadcast = rebirthLevel % 5 == 0 || rebirthLevel == 1 || rebirthLevel >= 10;

        if (shouldBroadcast) {
            String message = "§c§l[REBIRTH] §f" + player.getUsername() + " §7has been reborn! Rebirth: §c§l" + rebirthLevel;

            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            for (Player onlinePlayer : connectionManager.getOnlinePlayers()) {
                ChatUtil.sendMessage(onlinePlayer, message);
            }
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new RebirthCommand());
    }
}