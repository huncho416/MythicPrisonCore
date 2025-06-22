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
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WorldManager {

    private final Map<String, InstanceContainer> worlds = new HashMap<>();
    private final InstanceManager instanceManager;
    private final Gson gson = new Gson();
    private final File spawnConfigFile;

    // World spawn positions - these will be loaded from config or defaults
    private Pos spawnWorldSpawn = new Pos(0, 64, 0); // Default spawn position
    private Pos mineWorldSpawn = new Pos(0, 64, 0);   // Default mine position

    public WorldManager() {
        this.instanceManager = MinecraftServer.getInstanceManager();
        this.spawnConfigFile = new File("spawn-config.json");
        loadSpawnConfig();
    }

    /**
     * Load spawn positions from config file
     */
    private void loadSpawnConfig() {
        try {
            if (spawnConfigFile.exists()) {
                JsonObject config = gson.fromJson(new FileReader(spawnConfigFile), JsonObject.class);
                
                if (config.has("spawn")) {
                    JsonObject spawnData = config.getAsJsonObject("spawn");
                    spawnWorldSpawn = new Pos(
                        spawnData.get("x").getAsDouble(),
                        spawnData.get("y").getAsDouble(),
                        spawnData.get("z").getAsDouble(),
                        spawnData.has("yaw") ? spawnData.get("yaw").getAsFloat() : 0f,
                        spawnData.has("pitch") ? spawnData.get("pitch").getAsFloat() : 0f
                    );
                }
                
                if (config.has("mine")) {
                    JsonObject mineData = config.getAsJsonObject("mine");
                    mineWorldSpawn = new Pos(
                        mineData.get("x").getAsDouble(),
                        mineData.get("y").getAsDouble(),
                        mineData.get("z").getAsDouble(),
                        mineData.has("yaw") ? mineData.get("yaw").getAsFloat() : 0f,
                        mineData.has("pitch") ? mineData.get("pitch").getAsFloat() : 0f
                    );
                }
                
                System.out.println("[WorldManager] Loaded spawn positions from config");
            } else {
                // Create default config file
                saveSpawnConfig();
                System.out.println("[WorldManager] Created default spawn config");
            }
        } catch (Exception e) {
            System.err.println("[WorldManager] Error loading spawn config: " + e.getMessage());
        }
    }

    /**
     * Save spawn positions to config file
     */
    private void saveSpawnConfig() {
        try {
            JsonObject config = new JsonObject();
            
            // Save spawn world position
            JsonObject spawnData = new JsonObject();
            spawnData.addProperty("x", spawnWorldSpawn.x());
            spawnData.addProperty("y", spawnWorldSpawn.y());
            spawnData.addProperty("z", spawnWorldSpawn.z());
            spawnData.addProperty("yaw", spawnWorldSpawn.yaw());
            spawnData.addProperty("pitch", spawnWorldSpawn.pitch());
            config.add("spawn", spawnData);
            
            // Save mine world position
            JsonObject mineData = new JsonObject();
            mineData.addProperty("x", mineWorldSpawn.x());
            mineData.addProperty("y", mineWorldSpawn.y());
            mineData.addProperty("z", mineWorldSpawn.z());
            mineData.addProperty("yaw", mineWorldSpawn.yaw());
            mineData.addProperty("pitch", mineWorldSpawn.pitch());
            config.add("mine", mineData);
            
            try (FileWriter writer = new FileWriter(spawnConfigFile)) {
                gson.toJson(config, writer);
            }
            
        } catch (IOException e) {
            System.err.println("[WorldManager] Error saving spawn config: " + e.getMessage());
        }
    }

    /**
     * Set the spawn position for a specific world
     * @param worldName The world name ("spawn" or "mine")
     * @param position The new spawn position
     * @return true if successful, false otherwise
     */
    public boolean setSpawnPosition(String worldName, Pos position) {
        worldName = worldName.toLowerCase();
        
        switch (worldName) {
            case "spawn":
                spawnWorldSpawn = position;
                break;
            case "mine":
                mineWorldSpawn = position;
                break;
            default:
                return false;
        }
        
        // Save to config file
        saveSpawnConfig();
        System.out.println("[WorldManager] Updated " + worldName + " spawn to: " + position);
        return true;
    }

    /**
     * Get the spawn position for a specific world
     * @param worldName The world name
     * @return The spawn position or null if world not found
     */
    public Pos getSpawnPosition(String worldName) {
        switch (worldName.toLowerCase()) {
            case "spawn":
                return spawnWorldSpawn;
            case "mine":
                return mineWorldSpawn;
            default:
                return null;
        }
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