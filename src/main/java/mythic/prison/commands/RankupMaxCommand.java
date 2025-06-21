package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class RankupMaxCommand extends Command {

    public RankupMaxCommand() {
        super("rankupmax", "rumax");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeRankupMax(player);
        });
    }

    private void executeRankupMax(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        
        String startRank = rankingManager.getRank(player);
        int rankupsCount = 0;
        
        // Keep ranking up until player can't afford next rank
        while (rankingManager.canRankup(player)) {
            if (rankingManager.rankup(player)) {
                rankupsCount++;
            } else {
                break;
            }
        }
        
        if (rankupsCount > 0) {
            String endRank = rankingManager.getRank(player);
            ChatUtil.sendSuccess(player, "§d§lRANKUP MAX§r§a Successfully ranked up §e" + rankupsCount + " §atimes!");
            ChatUtil.sendMessage(player, "§7Went from §e" + startRank + " §7to §e" + endRank + "§7!");
        } else {
            ChatUtil.sendError(player, "You cannot afford to rankup!");
            
            String nextRank = rankingManager.getNextRankName(player);
            if (nextRank != null) {
                double cost = rankingManager.getRankupCost(player);
                double balance = MythicPrison.getInstance().getCurrencyManager().getBalance(player, "money");
                double needed = cost - balance;
                
                ChatUtil.sendMessage(player, "§7Next rank: §e" + nextRank);
                ChatUtil.sendMessage(player, "§7Cost: §a$" + ChatUtil.formatMoney(cost));
                ChatUtil.sendMessage(player, "§7You need: §c$" + ChatUtil.formatMoney(needed) + " §7more");
            }
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new RankupMaxCommand());
    }
}