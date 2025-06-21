package mythic.prison.commands;

import mythic.prison.utils.ChatUtil;
import mythic.prison.player.Profile;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeCommand extends Command {

    public GameModeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;

            // Check permission (you can adjust this based on your permission system)
            if (!hasPermission(player, "mythicprison.gamemode") && !hasPermission(player, "mythicprison.admin")) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            ChatUtil.sendMessage(player, "§7Usage: /gamemode <creative|survival|c|s>");
            ChatUtil.sendMessage(player, "§7Current gamemode: §e" + getGameModeName(player.getGameMode()));
        });

        // Add gamemode argument
        ArgumentString gamemodeArg = ArgumentType.String("gamemode");
        addSyntax((sender, context) -> {
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

            String gamemode = context.get(gamemodeArg);
            executeGameMode(player, gamemode);
        }, gamemodeArg);

        // Add target player argument for admins
        ArgumentString targetArg = ArgumentType.String("target");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                // Console execution
                String gamemode = context.get(gamemodeArg);
                String targetName = context.get(targetArg);
                executeGameModeConsole(gamemode, targetName);
                return;
            }

            Player player = (Player) sender;

            // Check admin permission for changing other players' gamemode
            if (!hasPermission(player, "mythicprison.gamemode.others") && !hasPermission(player, "mythicprison.admin")) {
                ChatUtil.sendError(player, "You don't have permission to change other players' gamemode!");
                return;
            }

            String gamemode = context.get(gamemodeArg);
            String targetName = context.get(targetArg);
            executeGameModeOther(player, gamemode, targetName);
        }, gamemodeArg, targetArg);
    }

    private void executeGameMode(Player player, String gamemodeInput) {
        GameMode gameMode = parseGameMode(gamemodeInput);

        if (gameMode == null) {
            ChatUtil.sendError(player, "Invalid gamemode! Use: creative, survival, c, or s");
            return;
        }

        if (player.getGameMode() == gameMode) {
            ChatUtil.sendMessage(player, "§7You are already in §e" + getGameModeName(gameMode) + " §7mode!");
            return;
        }

        player.setGameMode(gameMode);
        ChatUtil.sendSuccess(player, "Gamemode changed to §e" + getGameModeName(gameMode) + "§a!");

        System.out.println("[GameMode] " + player.getUsername() + " changed gamemode to " + getGameModeName(gameMode));
    }

    private void executeGameModeOther(Player admin, String gamemodeInput, String targetName) {
        GameMode gameMode = parseGameMode(gamemodeInput);

        if (gameMode == null) {
            ChatUtil.sendError(admin, "Invalid gamemode! Use: creative, survival, c, or s");
            return;
        }

        Player target = findPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(admin, "Player '" + targetName + "' not found!");
            return;
        }

        if (target.getGameMode() == gameMode) {
            ChatUtil.sendMessage(admin, "§7" + target.getUsername() + " is already in §e" + getGameModeName(gameMode) + " §7mode!");
            return;
        }

        target.setGameMode(gameMode);

        // Notify admin
        ChatUtil.sendSuccess(admin, "Set " + target.getUsername() + "'s gamemode to §e" + getGameModeName(gameMode) + "§a!");

        // Notify target
        ChatUtil.sendMessage(target, "§7Your gamemode has been changed to §e" + getGameModeName(gameMode) + " §7by " + admin.getUsername());

        System.out.println("[GameMode] " + admin.getUsername() + " changed " + target.getUsername() + "'s gamemode to " + getGameModeName(gameMode));
    }

    private void executeGameModeConsole(String gamemodeInput, String targetName) {
        GameMode gameMode = parseGameMode(gamemodeInput);

        if (gameMode == null) {
            System.err.println("[GameMode] Invalid gamemode: " + gamemodeInput);
            return;
        }

        Player target = findPlayerByName(targetName);
        if (target == null) {
            System.err.println("[GameMode] Player '" + targetName + "' not found!");
            return;
        }

        if (target.getGameMode() == gameMode) {
            System.out.println("[GameMode] " + target.getUsername() + " is already in " + getGameModeName(gameMode) + " mode!");
            return;
        }

        target.setGameMode(gameMode);

        // Notify target
        ChatUtil.sendMessage(target, "§7Your gamemode has been changed to §e" + getGameModeName(gameMode) + " §7by console");

        System.out.println("[GameMode] Console changed " + target.getUsername() + "'s gamemode to " + getGameModeName(gameMode));
    }

    private GameMode parseGameMode(String input) {
        return switch (input.toLowerCase()) {
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private String getGameModeName(GameMode gameMode) {
        return switch (gameMode) {
            case CREATIVE -> "Creative";
            case SURVIVAL -> "Survival";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
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
        MinecraftServer.getCommandManager().register(new GameModeCommand());
    }
}