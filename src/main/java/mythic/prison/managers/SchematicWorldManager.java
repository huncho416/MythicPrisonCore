package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SchematicWorldManager {

    private final Map<String, SchematicWorld> worlds = new HashMap<>();
    private final Map<UUID, String> playerWorlds = new HashMap<>();
    private final File schematicsFolder;

    public SchematicWorldManager() {
        this.schematicsFolder = new File(MythicPrison.getInstance().getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        setupEventListeners();
        loadDefaultSchematics();
    }

    private void setupEventListeners() {
        EventNode<Event> moveNode = EventNode.all("player-move");
        moveNode.addListener(PlayerMoveEvent.class, this::onPlayerMove);
        MinecraftServer.getGlobalEventHandler().addChild(moveNode);
    }

    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String worldName = playerWorlds.get(player.getUuid());

        if (worldName != null) {
            SchematicWorld world = worlds.get(worldName);
            if (world != null && !world.isWithinBounds(event.getNewPosition())) {
                // Only restrict movement in non-spawn worlds AND only if they're actually outside bounds
                if (!"spawn".equals(worldName) && worldName.startsWith("mine_")) {
                    // Check if player is significantly outside the mine area (not just 1-2 blocks)
                    Pos currentPos = event.getPlayer().getPosition();
                    Pos newPos = event.getNewPosition();

                    // Allow some tolerance for spawn area and glowstone blocks
                    double tolerance = 5.0; // 5 block tolerance
                    boolean significantlyOutside =
                            newPos.x() < world.getMinX() - tolerance || newPos.x() > world.getMaxX() + tolerance ||
                                    newPos.z() < world.getMinZ() - tolerance || newPos.z() > world.getMaxZ() + tolerance ||
                                    newPos.y() < world.getMinY() - tolerance || newPos.y() > world.getMaxY() + tolerance;

                    if (significantlyOutside) {
                        // Player is trying to leave the mine area significantly
                        Pos safePosition = world.getSafePosition(currentPos);
                        event.setNewPosition(safePosition);

                        // Only send warning message occasionally to avoid spam
                        if (System.currentTimeMillis() % 3000 < 100) { // Every 3 seconds max
                            player.sendMessage("§c§lYou cannot leave this area!");
                        }
                    }
                    // If they're just slightly outside (like in glowstone), allow the movement
                }
                // If it's spawn world, do nothing - allow free movement
            }
        }
    }

    public CompletableFuture<Instance> createSchematicWorld(String worldName, String schematicName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File schematicFile = new File(schematicsFolder, schematicName + ".schem");
                if (!schematicFile.exists()) {
                    System.err.println("Schematic file not found: " + schematicFile.getPath());
                    return null;
                }

                // Create new instance
                InstanceContainer instance = MinecraftServer.getInstanceManager()
                        .createInstanceContainer(DimensionType.OVERWORLD);

                // Set up proper lighting for void worlds
                setupWorldLighting(instance);

                // Load schematic into instance
                SchematicWorld schematicWorld = loadSchematic(instance, schematicFile);
                if (schematicWorld != null) {
                    worlds.put(worldName, schematicWorld);
                    schematicWorld.setInstance(instance);

                    // Set up world borders/boundaries
                    setupWorldBoundaries(instance, schematicWorld);
                }

                return instance;
            } catch (Exception e) {
                System.err.println("Failed to create schematic world: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    private SchematicWorld loadSchematic(InstanceContainer instance, File schematicFile) {
        try {
            // This is a simplified version - you'll need to implement actual schematic loading
            // using WorldEdit/FAWE or NBT parsing
            SchematicData data = parseSchematic(schematicFile);

            if (data != null) {
                // Place blocks from schematic
                for (SchematicBlock block : data.getBlocks()) {
                    instance.setBlock(block.x, block.y, block.z, block.block);
                }

                return new SchematicWorld(
                        data.getMinX(), data.getMinY(), data.getMinZ(),
                        data.getMaxX(), data.getMaxY(), data.getMaxZ(),
                        data.getSpawnPoint()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to load schematic: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private SchematicData parseSchematic(File schematicFile) {
        try {
            // Check if we're dealing with default schematics
            String fileName = schematicFile.getName().toLowerCase();

            if (fileName.contains("spawn")) {
                System.out.println("[SchematicWorldManager] Creating default spawn schematic");
                return createDefaultSpawnSchematic();
            } else if (fileName.contains("mine")) {
                System.out.println("[SchematicWorldManager] Creating default mine schematic");
                return createDefaultMineSchematic();
            }

            // If it's an actual schematic file, try to parse it
            if (schematicFile.exists()) {
                // TODO: Implement actual schematic file parsing here
                System.out.println("[SchematicWorldManager] Schematic file parsing not yet implemented, using default mine");
                return createDefaultMineSchematic();
            } else {
                System.out.println("[SchematicWorldManager] Schematic file not found: " + schematicFile.getName() + ", using default mine");
                return createDefaultMineSchematic();
            }

        } catch (Exception e) {
            System.err.println("[SchematicWorldManager] Error parsing schematic: " + e.getMessage());
            return createDefaultMineSchematic();
        }
    }

    private SchematicData createDefaultSpawnSchematic() {
        SchematicData data = new SchematicData();

        // Create a simple spawn platform
        int sizeX = 20, sizeY = 5, sizeZ = 20;

        data.setDimensions(-sizeX / 2, 0, -sizeZ / 2, sizeX / 2, sizeY, sizeZ / 2);
        // Set spawn point on the platform, not inside blocks
        data.setSpawnPoint(new Pos(0, 2, 0)); // 2 blocks above the platform

        // Create a simple spawn platform with lighting
        for (int x = -sizeX / 2; x <= sizeX / 2; x++) {
            for (int z = -sizeZ / 2; z <= sizeZ / 2; z++) {
                // Ground level platform
                data.addBlock(x, 0, z, Block.GRASS_BLOCK);

                // Small border around the platform
                if (x == -sizeX / 2 || x == sizeX / 2 || z == -sizeZ / 2 || z == sizeZ / 2) {
                    data.addBlock(x, 1, z, Block.STONE_BRICKS);
                }
            }
        }

        // Add dense lighting throughout the spawn area for full brightness
        for (int x = -sizeX / 2; x <= sizeX / 2; x += 3) {
            for (int z = -sizeZ / 2; z <= sizeZ / 2; z += 3) {
                // Place light sources every 3 blocks
                data.addBlock(x, 1, z, Block.COBBLESTONE);
                data.addBlock(x, 2, z, Block.GLOWSTONE);
            }
        }

        // Add perimeter lighting
        for (int x = -sizeX / 2; x <= sizeX / 2; x += 2) {
            data.addBlock(x, 3, -sizeZ / 2, Block.GLOWSTONE);
            data.addBlock(x, 3, sizeZ / 2, Block.GLOWSTONE);
        }
        for (int z = -sizeZ / 2; z <= sizeZ / 2; z += 2) {
            data.addBlock(-sizeX / 2, 3, z, Block.GLOWSTONE);
            data.addBlock(sizeX / 2, 3, z, Block.GLOWSTONE);
        }

        // Remove the central beacon that was causing problems
        // Just use a simple central platform
        data.addBlock(0, 1, 0, Block.STONE_BRICKS);

        return data;
    }

    private SchematicData createDefaultMineSchematic() {
        SchematicData data = new SchematicData();

        // Create a simple 50x50x20 mine area
        int sizeX = 50, sizeY = 20, sizeZ = 50;

        data.setDimensions(-sizeX / 2, 0, -sizeZ / 2, sizeX / 2, sizeY, sizeZ / 2);
        // Set spawn point safely in the middle of the platform, well above any blocks
        data.setSpawnPoint(new Pos(0, sizeY + 3, 0)); // 3 blocks above the platform for safety

        // Create walls and floor with proper lighting
        for (int x = -sizeX / 2; x <= sizeX / 2; x++) {
            for (int z = -sizeZ / 2; z <= sizeZ / 2; z++) {
                for (int y = 0; y <= sizeY; y++) {
                    Block block;

                    // Floor - bedrock
                    if (y == 0) {
                        block = Block.BEDROCK;
                    }
                    // Walls - bedrock
                    else if (x == -sizeX / 2 || x == sizeX / 2 || z == -sizeZ / 2 || z == sizeZ / 2) {
                        block = Block.BEDROCK;
                    }
                    // Interior - fill with stone that can be mined
                    else {
                        block = Block.STONE;
                    }

                    data.addBlock(x, y, z, block);
                }
            }
        }

        // Add comprehensive lighting system throughout the mine
        // Dense ceiling lighting every 5 blocks for maximum brightness
        for (int x = -sizeX / 2 + 3; x <= sizeX / 2 - 3; x += 5) {
            for (int z = -sizeZ / 2 + 3; z <= sizeZ / 2 - 3; z += 5) {
                // Replace stone with glowstone for ceiling lighting
                data.addBlock(x, sizeY - 1, z, Block.GLOWSTONE);
            }
        }

        // Add wall lighting every 4 blocks
        for (int i = -sizeX / 2 + 2; i <= sizeX / 2 - 2; i += 4) {
            // Top and bottom walls at multiple heights
            data.addBlock(i, sizeY - 3, -sizeZ / 2, Block.GLOWSTONE);
            data.addBlock(i, sizeY - 3, sizeZ / 2, Block.GLOWSTONE);
            data.addBlock(i, sizeY - 7, -sizeZ / 2, Block.GLOWSTONE);
            data.addBlock(i, sizeY - 7, sizeZ / 2, Block.GLOWSTONE);
        }

        for (int i = -sizeZ / 2 + 2; i <= sizeZ / 2 - 2; i += 4) {
            // Left and right walls at multiple heights
            data.addBlock(-sizeX / 2, sizeY - 3, i, Block.GLOWSTONE);
            data.addBlock(sizeX / 2, sizeY - 3, i, Block.GLOWSTONE);
            data.addBlock(-sizeX / 2, sizeY - 7, i, Block.GLOWSTONE);
            data.addBlock(sizeX / 2, sizeY - 7, i, Block.GLOWSTONE);
        }

        // Add mid-level lighting pillars throughout the mine (but not near spawn)
        for (int x = -sizeX / 2 + 8; x <= sizeX / 2 - 8; x += 8) {
            for (int z = -sizeZ / 2 + 8; z <= sizeZ / 2 - 8; z += 8) {
                // Skip the spawn area to prevent interference
                if (Math.abs(x) > 5 || Math.abs(z) > 5) {
                    // Create lighting pillars from floor to near ceiling
                    for (int y = 1; y <= sizeY - 3; y += 4) {
                        data.addBlock(x, y, z, Block.GLOWSTONE);
                    }
                }
            }
        }

        // Create safe spawn platform - keep it simple and clear
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                data.addBlock(x, sizeY, z, Block.STONE_BRICKS);
            }
        }

        // No glowstone directly at spawn level to prevent getting stuck
        // Just corner lighting well away from spawn point
        data.addBlock(-4, sizeY + 1, -4, Block.GLOWSTONE);
        data.addBlock(4, sizeY + 1, -4, Block.GLOWSTONE);
        data.addBlock(-4, sizeY + 1, 4, Block.GLOWSTONE);
        data.addBlock(4, sizeY + 1, 4, Block.GLOWSTONE);

        // Add extra floor lighting in a grid pattern (away from spawn area)
        for (int x = -sizeX / 2 + 5; x <= sizeX / 2 - 5; x += 10) {
            for (int z = -sizeZ / 2 + 5; z <= sizeZ / 2 - 5; z += 10) {
                // Skip near spawn area
                if (Math.abs(x) > 8 || Math.abs(z) > 8) {
                    data.addBlock(x, 1, z, Block.GLOWSTONE);
                }
            }
        }

        return data;
    }

    public void setupWorldBoundaries(InstanceContainer instance, SchematicWorld world) {
        // Don't set up physical barriers anymore - rely only on movement detection
        // This prevents players from getting stuck in blocks
        String worldName = getWorldNameByInstance(instance);
        if ("spawn".equals(worldName)) {
            return; // Skip boundary setup for spawn
        }

        // Remove the physical barrier setup to prevent players getting stuck in blocks
        // The movement event handler will handle boundary enforcement
    }

    /**
     * Helper method to get world name by instance
     */
    public String getWorldNameByInstance(Instance instance) {
        for (Map.Entry<String, SchematicWorld> entry : worlds.entrySet()) {
            if (entry.getValue().getInstance() == instance) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void teleportPlayerToWorld(Player player, String worldName) {
        SchematicWorld world = worlds.get(worldName);
        if (world != null && world.getInstance() != null) {
            playerWorlds.put(player.getUuid(), worldName);

            // Check if player is already in the target instance
            if (player.getInstance() == world.getInstance()) {
                // Player is already in the target instance, just teleport to the spawn point
                player.teleport(world.getSpawnPoint());
            } else {
                // Player is in a different instance, set the new instance
                player.setInstance(world.getInstance(), world.getSpawnPoint());
            }
        }
    }

    public boolean isPlayerInSchematicWorld(Player player) {
        return playerWorlds.containsKey(player.getUuid());
    }

    public String getPlayerWorld(Player player) {
        return playerWorlds.get(player.getUuid());
    }

    /**
     * Get a schematic world by name
     *
     * @param worldName The name of the world to retrieve
     * @return The SchematicWorld or null if not found
     */
    public SchematicWorld getWorld(String worldName) {
        return worlds.get(worldName);
    }

    /**
     * Get the spawn position for a specific world
     *
     * @param worldName The name of the world
     * @return The spawn position or null if world not found
     */
    public Pos getWorldSpawnPosition(String worldName) {
        SchematicWorld world = worlds.get(worldName);
        return world != null ? world.getSpawnPoint() : null;
    }

    /**
     * Check if a world exists
     *
     * @param worldName The name of the world to check
     * @return true if the world exists, false otherwise
     */
    public boolean worldExists(String worldName) {
        return worlds.containsKey(worldName);
    }

    private void loadDefaultSchematics() {
        // Create default worlds
        createSchematicWorld("spawn", "spawn").thenAccept(instance -> {
            if (instance != null) {
                System.out.println("Loaded spawn schematic world");
            }
        });

        createSchematicWorld("mine", "mine").thenAccept(instance -> {
            if (instance != null) {
                System.out.println("Loaded mine schematic world");
            }
        });
    }

    /**
     * Track a player in a world without teleporting them
     */
    public void trackPlayerInWorld(Player player, String worldName) {
        playerWorlds.put(player.getUuid(), worldName);
        // Remove this debug line:
        // System.out.println("[DEBUG] Tracking player " + player.getUsername() + " in world: " + worldName);
    }

    /**
     * Update player world tracking based on their current instance
     */
    public void updatePlayerWorldTracking(Player player) {
        Instance currentInstance = player.getInstance();
        if (currentInstance != null) {
            String actualWorldName = getWorldNameByInstance(currentInstance);
            if (actualWorldName != null) {
                // Only update if the world name matches what we expect
                String expectedWorldName = playerWorlds.get(player.getUuid());
                if (expectedWorldName == null || actualWorldName.equals(expectedWorldName)) {
                    playerWorlds.put(player.getUuid(), actualWorldName);
                }
            }
        }
    }

    /**
     * Remove a schematic world and clean up its resources
     *
     * @param worldName The name of the world to remove
     * @return true if the world was removed, false if it didn't exist
     */
    public boolean removeWorld(String worldName) {
        SchematicWorld world = worlds.get(worldName);
        if (world == null) {
            return false;
        }

        // Remove all players from the world before removing it
        Instance instance = world.getInstance();
        if (instance != null) {
            // Move all players out of this world to spawn or another safe location
            var playersInWorld = instance.getPlayers();
            for (Player player : playersInWorld) {
                // Remove player tracking for this world
                playerWorlds.remove(player.getUuid());

                // Move player to spawn world or main world
                SchematicWorld spawnWorld = worlds.get("spawn");
                if (spawnWorld != null && spawnWorld.getInstance() != null) {
                    player.setInstance(spawnWorld.getInstance(), spawnWorld.getSpawnPoint());
                } else {
                    // Fallback to server's default spawn if spawn world is not available
                    player.setInstance(MinecraftServer.getInstanceManager().getInstances().iterator().next());
                }
            }

            // Unregister the instance
            MinecraftServer.getInstanceManager().unregisterInstance(instance);
        }

        // Remove from tracking maps
        worlds.remove(worldName);

        // Remove any player world tracking for this world
        playerWorlds.entrySet().removeIf(entry -> worldName.equals(entry.getValue()));

        return true;
    }

    /**
     * Get all loaded schematic worlds
     *
     * @return A map of world names to SchematicWorld objects
     */
    public Map<String, SchematicWorld> getAllWorlds() {
        return new HashMap<>(worlds);
    }

    private void setupWorldLighting(InstanceContainer instance) {
        // Set the world to always be day (light level 15)
        instance.setTimeRate(0);
        instance.setTime(6000); // Noon

        // Ensure the world has proper ambient lighting
        // This prevents the void from being completely dark
    }

    // Inner classes for data structures
    // Change the SchematicWorld class from private to public
    public static class SchematicWorld {
        private final int minX, minY, minZ;
        private final int maxX, maxY, maxZ;
        private final Pos spawnPoint;
        private Instance instance;

        public SchematicWorld(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Pos spawnPoint) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.spawnPoint = spawnPoint != null ? spawnPoint : new Pos(0, 5, 0);
        }

        public boolean isWithinBounds(Pos position) {
            return position.x() >= minX && position.x() <= maxX &&
                    position.y() >= minY && position.y() <= maxY &&
                    position.z() >= minZ && position.z() <= maxZ;
        }

        public Pos getSafePosition(Pos currentPos) {
            double x = Math.max(minX, Math.min(maxX, currentPos.x()));
            double y = Math.max(minY, Math.min(maxY, currentPos.y()));
            double z = Math.max(minZ, Math.min(maxZ, currentPos.z()));
            return new Pos(x, y, z);
        }

        // Getters
        public int getMinX() {
            return minX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMaxZ() {
            return maxZ;
        }

        public Pos getSpawnPoint() {
            return spawnPoint;
        }

        public Instance getInstance() {
            return instance;
        }

        public void setInstance(Instance instance) {
            this.instance = instance;
        }
    }

    private static class SchematicData {
        private int minX, minY, minZ, maxX, maxY, maxZ;
        private Pos spawnPoint;
        private final Map<String, SchematicBlock> blocks = new HashMap<>();

        public void setDimensions(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public void addBlock(int x, int y, int z, Block block) {
            blocks.put(x + "," + y + "," + z, new SchematicBlock(x, y, z, block));
        }

        public void setSpawnPoint(Pos spawnPoint) {
            this.spawnPoint = spawnPoint;
        }

        // Getters
        public int getMinX() {
            return minX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMaxZ() {
            return maxZ;
        }

        public Pos getSpawnPoint() {
            return spawnPoint;
        }

        public Iterable<SchematicBlock> getBlocks() {
            return blocks.values();
        }
    }

    private static class SchematicBlock {
        final int x, y, z;
        final Block block;

        public SchematicBlock(int x, int y, int z, Block block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
        }
    }

    public void registerWorld(String worldName, SchematicWorld world) {
        worlds.put(worldName, world);
    }
}