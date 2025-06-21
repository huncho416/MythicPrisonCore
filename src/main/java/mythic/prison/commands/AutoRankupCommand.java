package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoRankupCommand extends Command {

    private static final Map<String, Boolean> autoRankupPlayers = new ConcurrentHashMap<>();

    public AutoRankupCommand() {
        super("autorankup", "autorank");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            toggleAutoRankup(player);
        });
    }

    private void toggleAutoRankup(Player player) {
        String uuid = player.getUuid().toString();
        boolean current = autoRankupPlayers.getOrDefault(uuid, false);
        boolean newStatus = !current;

        autoRankupPlayers.put(uuid, newStatus);

        if (newStatus) {
            ChatUtil.sendSuccess(player, "Auto-Rankup has been §a§lENABLED§r§a!");
            ChatUtil.sendMessage(player, "§7You will now automatically rankup when you have enough money.");
            // Try to rankup immediately
            checkAutoRankup(player);
        } else {
            ChatUtil.sendSuccess(player, "Auto-Rankup has been §c§lDISABLED§r§c!");
            ChatUtil.sendMessage(player, "§7You will no longer automatically rankup.");
        }
    }

    public static boolean isAutoRankupEnabled(Player player) {
        return autoRankupPlayers.getOrDefault(player.getUuid().toString(), false);
    }

    public static void checkAutoRankup(Player player) {
        if (!isAutoRankupEnabled(player)) return;

        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        
        // Keep ranking up until player can't afford next rank
        while (rankingManager.canRankup(player)) {
            String oldRank = rankingManager.getRank(player);
            if (rankingManager.rankup(player)) {
                String newRank = rankingManager.getRank(player);
                ChatUtil.sendSuccess(player, "§d§lAUTO-RANKUP§r§a Successfully ranked up from §e" + oldRank + " §ato §e" + newRank + "§a!");
            } else {
                break;
            }
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new AutoRankupCommand());
    }
}