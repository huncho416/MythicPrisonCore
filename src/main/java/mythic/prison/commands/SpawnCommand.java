package mythic.prison.commands;

import mythic.prison.MythicPrison;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class SpawnCommand extends Command {
    
    public SpawnCommand() {
        super("spawn");
        
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            
            var schematicWorldManager = MythicPrison.getInstance().getSchematicWorldManager();
            if (schematicWorldManager != null) {
                schematicWorldManager.teleportPlayerToWorld(player, "spawn");
            } else {
                player.sendMessage("§cSchematic world manager not available!");
            }
        });
    }
    
    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new SpawnCommand());
    }
}