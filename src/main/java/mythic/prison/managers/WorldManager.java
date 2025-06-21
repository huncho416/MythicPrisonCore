package mythic.prison.managers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.world.DimensionType;

import java.util.HashMap;
import java.util.Map;

public class WorldManager {

    private final Map<String, InstanceContainer> worlds = new HashMap<>();
    private final InstanceManager instanceManager;

    // World spawn positions
    private final Pos spawnWorldSpawn = new Pos(0, 100, 0); // Adjust coordinates as needed
    private final Pos mineWorldSpawn = new Pos(0, 64, 0);   // Adjust coordinates as needed

    public WorldManager() {
        this.instanceManager = MinecraftServer.getInstanceManager();
    }

    public void loadWorlds() {
        try {
            System.out.println("[WorldManager] Starting world loading...");
        
            // Use absolute paths for testing
            String spawnPath = System.getProperty("user.dir") + "/worlds/spawn";
            String minePath = System.getProperty("user.dir") + "/worlds/mine";
        
            System.out.println("[WorldManager] Looking for spawn world at: " + spawnPath);
            System.out.println("[WorldManager] Looking for mine world at: " + minePath);
        
            java.io.File spawnDir = new java.io.File(spawnPath);
            java.io.File mineDir = new java.io.File(minePath);
        
            // Load spawn world
            if (spawnDir.exists()) {
                InstanceContainer spawnWorld = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
                spawnWorld.setChunkLoader(new AnvilLoader(spawnPath));
                worlds.put("spawn", spawnWorld);
                System.out.println("[WorldManager] Spawn world loaded!");
            }

        // Load mine world  
        if (mineDir.exists()) {
            InstanceContainer mineWorld = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
            mineWorld.setChunkLoader(new AnvilLoader(minePath));
            worlds.put("mine", mineWorld);
            System.out.println("[WorldManager] Mine world loaded!");
        }

        System.out.println("[WorldManager] Loaded " + worlds.size() + " worlds");

    } catch (Exception e) {
        System.err.println("[WorldManager] Error loading worlds: " + e.getMessage());
        e.printStackTrace();
        createFallbackWorlds();
    }
}

    private void createFallbackWorlds() {
        try {
            // Create empty spawn world as fallback
            InstanceContainer spawnWorld = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
            spawnWorld.setGenerator(unit -> {
                unit.modifier().fillHeight(0, 1, net.minestom.server.instance.block.Block.BEDROCK);
                unit.modifier().fillHeight(1, 4, net.minestom.server.instance.block.Block.STONE);
                unit.modifier().fillHeight(4, 5, net.minestom.server.instance.block.Block.GRASS_BLOCK);
            });
            worlds.put("spawn", spawnWorld);

            // Create empty mine world as fallback
            InstanceContainer mineWorld = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
            mineWorld.setGenerator(unit -> {
                unit.modifier().fillHeight(0, 1, net.minestom.server.instance.block.Block.BEDROCK);
                unit.modifier().fillHeight(1, 64, net.minestom.server.instance.block.Block.STONE);
            });
            worlds.put("mine", mineWorld);

            System.out.println("[WorldManager] Created fallback worlds");

        } catch (Exception e) {
            System.err.println("[WorldManager] Failed to create fallback worlds: " + e.getMessage());
        }
    }

    public InstanceContainer getWorld(String worldName) {
        return worlds.get(worldName.toLowerCase());
    }

    public InstanceContainer getSpawnWorld() {
        return getWorld("spawn");
    }

    public InstanceContainer getMineWorld() {
        return getWorld("mine");
    }

    public void teleportToSpawn(Player player) {
        InstanceContainer spawnWorld = getSpawnWorld();
        if (spawnWorld != null) {
            // Check if player is already in the spawn world
            if (player.getInstance() == spawnWorld) {
                // Player is already in spawn world, just teleport to spawn position
                player.teleport(spawnWorldSpawn);
                player.sendMessage("§aTeleported to spawn!");
            } else {
                // Player is in a different world, change instance
                player.setInstance(spawnWorld, spawnWorldSpawn);
                player.sendMessage("§aTeleported to spawn!");
            }
        } else {
            player.sendMessage("§cSpawn world not available!");
        }
    }

    public void teleportToMine(Player player) {
        InstanceContainer mineWorld = getMineWorld();
        if (mineWorld != null) {
            // Check if player is already in the mine world
            if (player.getInstance() == mineWorld) {
                // Player is already in mine world, just teleport to mine position
                player.teleport(mineWorldSpawn);
                player.sendMessage("§aTeleported to mine!");
            } else {
                // Player is in a different world, change instance
                player.setInstance(mineWorld, mineWorldSpawn);
                player.sendMessage("§aTeleported to mine!");
            }
        } else {
            player.sendMessage("§cMine world not available!");
        }
    }

    public boolean isInSpawn(Player player) {
        return player.getInstance() == getSpawnWorld();
    }

    public boolean isInMine(Player player) {
        return player.getInstance() == getMineWorld();
    }

    public Map<String, InstanceContainer> getAllWorlds() {
        return new HashMap<>(worlds);
    }

    public void shutdown() {
        for (InstanceContainer world : worlds.values()) {
            try {
                world.saveChunksToStorage();
            } catch (Exception e) {
                System.err.println("[WorldManager] Error saving world chunks: " + e.getMessage());
            }
        }
    }
}