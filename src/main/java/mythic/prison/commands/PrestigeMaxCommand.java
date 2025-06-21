package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class PrestigeMaxCommand extends Command {

    public PrestigeMaxCommand() {
        super("prestigemax", "pmx");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executePrestigeMax(player);
        });
    }

    private void executePrestigeMax(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        int startPrestige = rankingManager.getPrestige(player);
        int prestigeCount = 0;
        double totalSoulsEarned = 0;

        // Keep prestiging until player can't prestige anymore
        while (rankingManager.canPrestige(player)) {
            double prestigeReward = 1000000 * Math.pow(2, startPrestige + prestigeCount);
            if (rankingManager.prestige(player)) {
                prestigeCount++;
                totalSoulsEarned += prestigeReward;
            } else {
                break;
            }
        }

        if (prestigeCount > 0) {
            int endPrestige = rankingManager.getPrestige(player);
            ChatUtil.sendSuccess(player, "§6§lPRESTIGE MAX§r§a Successfully prestiged §6" + prestigeCount + " §atimes!");
            ChatUtil.sendMessage(player, "§7Went from prestige §6" + startPrestige + " §7to §6" + endPrestige + "§7!");
            ChatUtil.sendMessage(player, "§7Total souls earned: §d" + ChatUtil.formatMoney(totalSoulsEarned));
            ChatUtil.sendMessage(player, "§7Your money multiplier is now: §a" + (100 + (endPrestige * 10)) + "%");
        } else {
            ChatUtil.sendError(player, "You cannot prestige!");

            String currentRank = rankingManager.getRank(player);
            ChatUtil.sendMessage(player, "§7Current rank: §e" + currentRank);
            ChatUtil.sendMessage(player, "§7You need to reach rank §eZZ §7to prestige!");
            ChatUtil.sendMessage(player, "§7Use §e/rankupmax §7to rank up as much as possible first.");
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new PrestigeMaxCommand());
    }
}