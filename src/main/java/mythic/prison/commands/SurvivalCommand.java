package mythic.prison.commands;

import mythic.prison.utils.ChatUtil;
import mythic.prison.player.Profile;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class SurvivalCommand extends Command {

    public SurvivalCommand() {
        super("gms", "survival");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;

            // Check permission
            if (!hasPermission(player, "mythicprison.gamemode") && !hasPermission(player, "mythicprison.admin")) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            executeSurvival(player);
        });

        // Add target player argument for admins
        ArgumentString targetArg = ArgumentType.String("target");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                // Console execution
                String targetName = context.get(targetArg);
                executeSurvivalConsole(targetName);
                return;
            }

            Player player = (Player) sender;

            // Check admin permission for changing other players' gamemode
            if (!hasPermission(player, "mythicprison.gamemode.others") && !hasPermission(player, "mythicprison.admin")) {
                ChatUtil.sendError(player, "You don't have permission to change other players' gamemode!");
                return;
            }

            String targetName = context.get(targetArg);
            executeSurvivalOther(player, targetName);
        }, targetArg);
    }

    private void executeSurvival(Player player) {
        if (player.getGameMode() == GameMode.SURVIVAL) {
            ChatUtil.sendMessage(player, "§7You are already in §eSurvival §7mode!");
            return;
        }

        player.setGameMode(GameMode.SURVIVAL);
        ChatUtil.sendSuccess(player, "Gamemode changed to §eSurvival§a!");

        System.out.println("[GameMode] " + player.getUsername() + " changed gamemode to Survival");
    }

    private void executeSurvivalOther(Player admin, String targetName) {
        Player target = findPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(admin, "Player '" + targetName + "' not found!");
            return;
        }

        if (target.getGameMode() == GameMode.SURVIVAL) {
            ChatUtil.sendMessage(admin, "§7" + target.getUsername() + " is already in §eSurvival §7mode!");
            return;
        }

        target.setGameMode(GameMode.SURVIVAL);

        // Notify admin
        ChatUtil.sendSuccess(admin, "Set " + target.getUsername() + "'s gamemode to §eSurvival§a!");

        // Notify target
        ChatUtil.sendMessage(target, "§7Your gamemode has been changed to §eSurvival §7by " + admin.getUsername());

        System.out.println("[GameMode] " + admin.getUsername() + " changed " + target.getUsername() + "'s gamemode to Survival");
    }

    private void executeSurvivalConsole(String targetName) {
        Player target = findPlayerByName(targetName);
        if (target == null) {
            System.err.println("[GameMode] Player '" + targetName + "' not found!");
            return;
        }

        if (target.getGameMode() == GameMode.SURVIVAL) {
            System.out.println("[GameMode] " + target.getUsername() + " is already in Survival mode!");
            return;
        }

        target.setGameMode(GameMode.SURVIVAL);

        // Notify target
        ChatUtil.sendMessage(target, "§7Your gamemode has been changed to §eSurvival §7by console");

        System.out.println("[GameMode] Console changed " + target.getUsername() + "'s gamemode to Survival");
    }

    private boolean hasPermission(Player player, String permission) {
        try {
            // Load the permission profile from database using the Profile class
            Profile profile = Profile.loadAsync(player.getUuid().toString()).join();

            if (profile != null) {
                return profile.hasPermission(permission);
            }

            // If no profile found, check if it's a basic permission we want to allow by default
            if (permission.equals("mythicprison.gamemode")) {
                return true; // Allow basic gamemode for all players by default
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error checking permission for " + player.getUsername() + ": " + e.getMessage());
            // Allow basic gamemode permission on error, deny admin permissions
            return permission.equals("mythicprison.gamemode");
        }
    }

    private Player findPlayerByName(String name) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equalsIgnoreCase(name)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    public static void register() {
        MinecraftServer.getCommandManager().register(new SurvivalCommand());
    }
}