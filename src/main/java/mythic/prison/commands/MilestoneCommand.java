package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class MilestoneCommand extends Command {

    public MilestoneCommand() {
        super("milestones", "milestone");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can view milestones!");
                return;
            }
            executeMilestones(sender);
        });

        // /milestones progress
        ArgumentWord progressArg = ArgumentType.Word("progress").from("progress");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showProgress(sender);
        }, progressArg);

        // /milestones completed
        ArgumentWord completedArg = ArgumentType.Word("completed").from("completed", "done");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showCompletedMilestones(sender);
        }, completedArg);

        // /milestones check (to manually check for new milestone completions)
        ArgumentWord checkArg = ArgumentType.Word("check").from("check", "update");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            checkMilestones(sender);
        }, checkArg);
    }

    private static void executeMilestones(Object player) {
        try {
            MythicPrison.getInstance().getMilestoneManager().showMilestones(player);
        } catch (Exception e) {
            // Fallback if milestone manager isn't available yet
            showMilestonesPlaceholder(player);
        }
    }

    private static void showMilestonesPlaceholder(Object player) {
        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lMILESTONES §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§7Milestones track your progress in various activities!");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCategories:");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§a§lMining Milestones:");
        ChatUtil.sendMessage(player, "§7• Mine 1,000 blocks");
        ChatUtil.sendMessage(player, "§7• Mine 10,000 blocks");
        ChatUtil.sendMessage(player, "§7• Mine 100,000 blocks");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§lEconomy Milestones:");
        ChatUtil.sendMessage(player, "§7• Earn $10,000");
        ChatUtil.sendMessage(player, "§7• Earn $100,000");
        ChatUtil.sendMessage(player, "§7• Earn $1,000,000");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§5§lRank Milestones:");
        ChatUtil.sendMessage(player, "§7• Reach Rank C");
        ChatUtil.sendMessage(player, "§7• Reach Rank A");
        ChatUtil.sendMessage(player, "§7• Reach Rank S");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§fCommands:");
        ChatUtil.sendMessage(player, "§d/milestones progress §7- View your progress");
        ChatUtil.sendMessage(player, "§d/milestones completed §7- View completed milestones");
        ChatUtil.sendMessage(player, "§d/milestones check §7- Check for new completions");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                ");
    }

    private static void checkMilestones(Object player) {
        try {
            MythicPrison.getInstance().getMilestoneManager().checkMilestones(player);
            ChatUtil.sendMessage(player, "§a✓ Checked for new milestone completions!");
        } catch (Exception e) {
            ChatUtil.sendError(player, "Milestone system is not yet fully implemented!");
            ChatUtil.sendMessage(player, "§7Please try again later when the system is ready.");
        }
    }

    private static void showProgress(Object player) {
        try {
            var milestoneManager = MythicPrison.getInstance().getMilestoneManager();
            int completed = milestoneManager.getCompletedCount(player);
            int total = milestoneManager.getAvailableMilestones().size();
            double percentage = milestoneManager.getCompletionPercentage(player);

            ChatUtil.sendMessage(player, "§d§l§m        §r §d§lMILESTONE PROGRESS §r§d§l§m        ");
            ChatUtil.sendMessage(player, "");

            ChatUtil.sendMessage(player, "§f§lOverall Progress:");
            ChatUtil.sendMessage(player, "§7Completed: §a" + completed + "§7/§a" + total + " §7milestones");
            ChatUtil.sendMessage(player, "§7Completion Rate: §a" + String.format("%.1f", percentage) + "%");
            ChatUtil.sendMessage(player, "");

            // Show progress bar
            String progressBar = createProgressBar(percentage);
            ChatUtil.sendMessage(player, "§7Progress: " + progressBar + " §a" + String.format("%.1f", percentage) + "%");
            ChatUtil.sendMessage(player, "");

            ChatUtil.sendMessage(player, "§7Use §d/milestones §7to see detailed progress for each milestone!");
            ChatUtil.sendMessage(player, "§7Use §d/milestones completed §7to see your completed milestones!");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§d§l§m                                                ");
        } catch (Exception e) {
            ChatUtil.sendMessage(player, "§c§lERROR");
            ChatUtil.sendMessage(player, "§7Could not load milestone progress.");
        }
    }

    private static void showCompletedMilestones(Object player) {
        try {
            var milestoneManager = MythicPrison.getInstance().getMilestoneManager();
            var completedMilestones = milestoneManager.getCompletedMilestones(player);

            ChatUtil.sendMessage(player, "§d§l§m        §r §d§lCOMPLETED MILESTONES §r§d§l§m        ");
            ChatUtil.sendMessage(player, "");

            if (completedMilestones.isEmpty()) {
                ChatUtil.sendMessage(player, "§7You haven't completed any milestones yet!");
                ChatUtil.sendMessage(player, "§7Keep playing to unlock your first milestone!");
            } else {
                ChatUtil.sendMessage(player, "§a§lYou have completed " + completedMilestones.size() + " milestone(s):");
                ChatUtil.sendMessage(player, "");

                int count = 1;
                for (var milestone : completedMilestones) {
                    ChatUtil.sendMessage(player, "§a" + count + ". §f§l" + milestone.getName());
                    ChatUtil.sendMessage(player, "   §7" + milestone.getDescription());

                    // Show rewards received
                    StringBuilder rewards = new StringBuilder("   §7Rewards: ");
                    boolean first = true;
                    for (var reward : milestone.getRewards().entrySet()) {
                        if (!first) rewards.append(", ");
                        String color = MythicPrison.getInstance().getCurrencyManager().getCurrencyColor(reward.getKey());
                        rewards.append(color).append(ChatUtil.formatMoney(reward.getValue())).append(" ").append(reward.getKey());
                        first = false;
                    }
                    ChatUtil.sendMessage(player, rewards.toString());
                    ChatUtil.sendMessage(player, "");
                    count++;
                }
            }

            ChatUtil.sendMessage(player, "§d§l§m                                                ");
        } catch (Exception e) {
            ChatUtil.sendMessage(player, "§c§lERROR");
            ChatUtil.sendMessage(player, "§7Could not load completed milestones.");
        }
    }

    private static String createProgressBar(double percentage) {
        int bars = 20;
        int filled = (int) (percentage / 100 * bars);

        StringBuilder sb = new StringBuilder("§8[");
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                sb.append("§a■");
            } else {
                sb.append("§7■");
            }
        }
        sb.append("§8]");
        return sb.toString();
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new MilestoneCommand());
    }
}