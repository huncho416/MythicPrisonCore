package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.player.Profile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;

public class FlyCommand extends Command {

    public FlyCommand() {
        super("fly");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            toggleFly(player, player);
        });

        // Add syntax for toggling fly for other players (requires admin permission)
        ArgumentEntity playerArg = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player executor = (Player) sender;
            Player target = (Player) context.get(playerArg).findFirstPlayer(executor);

            if (target == null) {
                ChatUtil.sendError(executor, "Player not found!");
                return;
            }

            if (!hasPermission(executor, "mythic.fly.others")) {
                ChatUtil.sendError(executor, "You don't have permission to toggle fly for other players!");
                return;
            }

            toggleFly(executor, target);
        }, playerArg);
    }

    private void toggleFly(Player executor, Player target) {
        // Check permission
        if (!hasPermission(target, "mythic.fly")) {
            ChatUtil.sendError(executor, (executor == target ? "You don't" : target.getUsername() + " doesn't") + " have permission to use fly!");
            return;
        }

        boolean isFlying = target.isAllowFlying();
        
        if (isFlying) {
            // Disable fly
            target.setAllowFlying(false);
            target.setFlying(false);
            
            if (executor == target) {
                ChatUtil.sendMessage(target, "§cFly disabled!");
            } else {
                ChatUtil.sendMessage(executor, "§cDisabled fly for " + target.getUsername() + "!");
                ChatUtil.sendMessage(target, "§cYour fly has been disabled by " + executor.getUsername() + "!");
            }
        } else {
            // Enable fly
            target.setAllowFlying(true);
            
            if (executor == target) {
                ChatUtil.sendSuccess(target, "Fly enabled! Double jump to start flying!");
            } else {
                ChatUtil.sendSuccess(executor, "Enabled fly for " + target.getUsername() + "!");
                ChatUtil.sendSuccess(target, "Fly has been enabled by " + executor.getUsername() + "! Double jump to start flying!");
            }
        }
    }

    private boolean hasPermission(Player player, String permission) {
        try {
            // Load the permission profile from database using the Profile class
            Profile profile = Profile.loadAsync(player.getUuid().toString()).join();
            
            if (profile != null) {
                return profile.hasPermission(permission);
            }
            
            // If no profile found, check if it's a basic permission we want to allow by default
            if (permission.equals("mythic.fly")) {
                return true; // Allow basic fly for all players by default
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error checking permission for " + player.getUsername() + ": " + e.getMessage());
            // Allow basic fly permission on error, deny admin permissions
            return permission.equals("mythic.fly");
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new FlyCommand());
    }
}