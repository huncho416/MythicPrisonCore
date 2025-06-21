
package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class ResetRankCommand extends Command {

    public ResetRankCommand() {
        super("resetrank");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            showResetInfo(player);
        });

        // /resetrank <rank>
        ArgumentString rankArg = ArgumentType.String("rank");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String newRank = context.get(rankArg);
            resetRank(player, newRank);
        }, rankArg);
    }

    private void showResetInfo(Player player) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        String currentRank = rankingManager.getRank(player);

        ChatUtil.sendMessage(player, "§c§l§m            §r §c§lRESET RANK §r§c§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCurrent Rank: §e" + currentRank);
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§lWARNING: This will reset your rank!");
        ChatUtil.sendMessage(player, "§7This action cannot be undone.");
        ChatUtil.sendMessage(player, "§7Your prestige, rebirth, and ascension will remain unchanged.");
        ChatUtil.sendMessage(player, "§7Your currencies will remain unchanged.");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lUsage:");
        ChatUtil.sendMessage(player, "§c/resetrank <rank> §7- Reset to specific rank (A-Z)");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lExamples:");
        ChatUtil.sendMessage(player, "§c/resetrank A §7- Reset to rank A");
        ChatUtil.sendMessage(player, "§c/resetrank Z §7- Reset to rank Z");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l§m                                                ");
    }

    private void resetRank(Player player, String newRank) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        String oldRank = rankingManager.getRank(player);

        if (!rankingManager.isValidRank(newRank)) {
            ChatUtil.sendError(player, "Invalid rank: " + newRank);
            ChatUtil.sendMessage(player, "§7Valid ranks: §eA, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z");
            return;
        }

        if (rankingManager.resetPlayerRank(player, newRank)) {
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§a§l§m        §r §a§lRANK RESET COMPLETE §r§a§l§m        ");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendSuccess(player, "Your rank has been reset!");
            ChatUtil.sendMessage(player, "§7Old rank: §e" + oldRank);
            ChatUtil.sendMessage(player, "§7New rank: §e" + newRank.toUpperCase());
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lWhat was preserved:");
            ChatUtil.sendMessage(player, "§a• All currencies (money, tokens, souls, etc.)");
            ChatUtil.sendMessage(player, "§a• Prestige level and bonuses");
            ChatUtil.sendMessage(player, "§a• Rebirth level and bonuses");
            ChatUtil.sendMessage(player, "§a• Ascension level and bonuses");
            ChatUtil.sendMessage(player, "§a• Pickaxe level and enchants");
            ChatUtil.sendMessage(player, "§a• All other progress");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lNext Steps:");
            ChatUtil.sendMessage(player, "§d/rank §7- Check your new rank information");
            ChatUtil.sendMessage(player, "§d/rankup §7- Continue ranking up normally");
            ChatUtil.sendMessage(player, "§d/mine §7- Go mining with your new rank");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§a§l§m                                        ");
        } else {
            ChatUtil.sendError(player, "Failed to reset rank! Please try again.");
            ChatUtil.sendMessage(player, "§7If this continues, contact an administrator.");
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new ResetRankCommand());
    }
}