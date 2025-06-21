
package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class RebirthMaxCommand extends Command {

    public RebirthMaxCommand() {
        super("rebirthmax", "rmx");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeRebirthMax(player);
        });
    }

    private void executeRebirthMax(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        int startRebirth = rankingManager.getRebirth(player);
        int rebirthCount = 0;
        double totalBeaconsEarned = 0;

        // Keep rebirthing until player can't rebirth anymore
        while (rankingManager.canRebirth(player)) {
            double rebirthReward = 10000000 * Math.pow(2, startRebirth + rebirthCount);
            if (rankingManager.rebirth(player)) {
                rebirthCount++;
                totalBeaconsEarned += rebirthReward;
            } else {
                break;
            }
        }

        if (rebirthCount > 0) {
            int endRebirth = rankingManager.getRebirth(player);
            ChatUtil.sendSuccess(player, "§5§lREBIRTH MAX§r§a Successfully rebirthed §5" + rebirthCount + " §atimes!");
            ChatUtil.sendMessage(player, "§7Went from rebirth §5" + startRebirth + " §7to §5" + endRebirth + "§7!");
            ChatUtil.sendMessage(player, "§7Total beacons earned: §e" + ChatUtil.formatMoney(totalBeaconsEarned));
            ChatUtil.sendMessage(player, "§7Your token multiplier is now: §b" + (100 + (endRebirth * 15)) + "%");
        } else {
            ChatUtil.sendError(player, "You cannot rebirth!");

            int currentPrestige = rankingManager.getPrestige(player);
            ChatUtil.sendMessage(player, "§7Current prestige: §6" + currentPrestige);
            ChatUtil.sendMessage(player, "§7You need to reach prestige §610 §7to rebirth!");
            ChatUtil.sendMessage(player, "§7Use §6/prestigemax §7to prestige as much as possible first.");
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new RebirthMaxCommand());
    }
}