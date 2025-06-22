package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class VisitCommand extends Command {

    public VisitCommand() {
        super("visit", "warp");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            ChatUtil.sendError(player, "Usage: /visit <location>");
            ChatUtil.sendMessage(player, "§7Available locations:");
            ChatUtil.sendMessage(player, "§7- §dspawn §7- Return to spawn");
            ChatUtil.sendMessage(player, "§7Use §d/mine go §7to go to your mine");
            ChatUtil.sendMessage(player, "§7Use §d/mine list §7to see all available mines");
        });

        // Add location argument
        ArgumentString locationArg = ArgumentType.String("location");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String location = context.get(locationArg);
            executeVisit(player, location);
        }, locationArg);
    }

    private static void executeVisit(Player player, String location) {
        switch (location.toLowerCase()) {
            case "spawn" -> {
                player.teleport(player.getRespawnPoint());
                ChatUtil.sendSuccess(player, "Teleported to spawn!");
            }
            case "mine" -> {
                // Try to teleport to main mine through MineManager
                try {
                    MythicPrison.getInstance().getMineManager().teleportToMine(player, "mine");
                    ChatUtil.sendSuccess(player, "Teleported to mine!");
                } catch (Exception e) {
                    ChatUtil.sendError(player, "Mine not found! Use /mine list to see available mines.");
                }
            }
            default -> {
                // Try to teleport through MineManager for other locations
                try {
                    MythicPrison.getInstance().getMineManager().teleportToMine(player, location);
                    ChatUtil.sendSuccess(player, "Teleported to " + location + "!");
                } catch (Exception e) {
                    ChatUtil.sendError(player, "Location '" + location + "' not found!");
                    ChatUtil.sendMessage(player, "§7Available locations: spawn, mine");
                    ChatUtil.sendMessage(player, "§7Use §d/mine list §7for all mines");
                }
            }
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new VisitCommand());
    }
}