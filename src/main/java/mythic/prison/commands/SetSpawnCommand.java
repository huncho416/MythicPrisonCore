package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.WorldManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import mythic.prison.player.Profile;

public class SetSpawnCommand extends Command {
    
    public SetSpawnCommand() {
        super("setspawn");
        
        // Create argument for world type (optional)
        ArgumentWord worldArg = ArgumentType.Word("world").from("spawn", "mine");
        
        // Default executor - sets spawn for current world or main spawn
        setDefaultExecutor(this::executeDefault);
        
        // Executor with world argument
        addSyntax(this::executeWithWorld, worldArg);
    }
    
    private void executeDefault(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return;
        }
        
        // Check permission
        if (!hasPermission(player)) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        // Default to setting spawn world spawn point
        setSpawnPoint(player, "spawn");
    }
    
    private void executeWithWorld(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return;
        }
        
        // Check permission
        if (!hasPermission(player)) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        String worldName = context.get("world");
        setSpawnPoint(player, worldName);
    }
    
    private void setSpawnPoint(Player player, String worldName) {
        WorldManager worldManager = MythicPrison.getInstance().getWorldManager();
        
        if (worldManager == null) {
            player.sendMessage("§cWorldManager not available!");
            return;
        }
        
        // Get player's current position
        Pos currentPos = player.getPosition();
        
        // Set the spawn point
        boolean success = worldManager.setSpawnPosition(worldName, currentPos);
        
        if (success) {
            player.sendMessage("§a§l✓ Spawn point updated!");
            player.sendMessage("§7World: §e" + worldName);
            player.sendMessage("§7Position: §e" + 
                String.format("%.1f, %.1f, %.1f", currentPos.x(), currentPos.y(), currentPos.z()));
            player.sendMessage("§7Rotation: §e" + 
                String.format("%.1f°, %.1f°", currentPos.yaw(), currentPos.pitch()));
            
            // Also notify console
            System.out.println("[SetSpawn] " + player.getUsername() + " set " + worldName + 
                " spawn to: " + currentPos);
        } else {
            player.sendMessage("§cFailed to set spawn point! Invalid world: " + worldName);
            player.sendMessage("§7Available worlds: §espawn§7, §emine");
        }
    }
    
private boolean hasPermission(Player player) {
    try {
        // Load the permission profile from database using the Profile class
        Profile profile = Profile.loadAsync(player.getUuid().toString()).join();

        if (profile != null) {
            return profile.hasPermission("mythicprison.admin") ||
                   profile.hasPermission("mythicprison.setspawn") ||
                   profile.hasPermission("*");
        }

        // If no profile found, deny admin permissions for security
        return false;
    } catch (Exception e) {
        System.err.println("Error checking permission for " + player.getUsername() + ": " + e.getMessage());
        // Deny admin permissions on error for security
        return false;
    }
}
    
private boolean isOp(Player player) {
    try {
        // Load the permission profile from database using the Profile class
        Profile profile = Profile.loadAsync(player.getUuid().toString()).join();
        
        if (profile != null) {
            return profile.hasPermission("*") || profile.hasPermission("mythicprison.admin");
        }
        
        // If no profile found, deny admin permissions for security
        return false;
    } catch (Exception e) {
        System.err.println("Error checking OP permission for " + player.getUsername() + ": " + e.getMessage());
        // Deny admin permissions on error for security
        return false;
    }
}
    
    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new SetSpawnCommand());
    }
}