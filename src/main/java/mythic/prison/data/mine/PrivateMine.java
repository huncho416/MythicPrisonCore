package mythic.prison.data.mine;

import mythic.prison.MythicPrison;
import mythic.prison.managers.SchematicWorldManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PrivateMine {
    private String id;
    private String ownerUUID;
    private String ownerName;
    private String mineName;
    private boolean isPublic;
    private double taxRate; // 0.0 to 1.0 (0% to 100%)
    private int sizeLevel;
    private int beaconLevel;
    private String worldName;
    private Instance mineInstance;
    private Set<String> allowedPlayers;
    private double multiplier;

    public PrivateMine(String ownerUUID, String ownerName) {
        this.id = UUID.randomUUID().toString();
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.mineName = ownerName + "'s Mine";
        this.isPublic = false;
        this.taxRate = 0.0;
        this.sizeLevel = 1;
        this.beaconLevel = 0;
        this.worldName = "mine_" + ownerName.toLowerCase();
        this.allowedPlayers = new HashSet<>();
        this.multiplier = 1.0;
    }

    public int getMineSize() {
        return 10 + (sizeLevel * 5); // Base 10x10, +5x5 per level
    }

    public double getBeaconChance() {
        return beaconLevel * 0.01; // 1% per beacon level
    }

    public double getSizeUpgradeCost() {
        return 10000 * Math.pow(1.5, sizeLevel - 1);
    }

    public double getBeaconUpgradeCost() {
        return 5000 * Math.pow(2, beaconLevel);
    }

    public boolean canUpgradeSize() {
        return sizeLevel < 20; // Max level 20
    }

    public boolean canUpgradeBeacons() {
        return beaconLevel < 10; // Max level 10
    }

    public boolean canPlayerAccess(String playerUUID) {
        return isPublic || ownerUUID.equals(playerUUID) || allowedPlayers.contains(playerUUID);
    }

    public boolean upgradeSize() {
        if (canUpgradeSize()) {
            sizeLevel++;
            return true;
        }
        return false;
    }

    public boolean upgradeBeacons() {
        if (canUpgradeBeacons()) {
            beaconLevel++;
            return true;
        }
        return false;
    }

    public void teleportPlayer(Player player) {
        try {
            if (mineInstance == null) {
                createProperMineWorld();
            }
            teleportPlayerToInstance(player);
        } catch (Exception e) {
            System.err.println("Error teleporting player " + player.getUsername() + " to mine: " + e.getMessage());
            player.sendMessage("§cError teleporting to mine! Please try again.");
            e.printStackTrace();
        }
    }

    private void createProperMineWorld() {
        try {
            // Create instance directly with mine generator
            net.minestom.server.instance.InstanceManager instanceManager = net.minestom.server.MinecraftServer.getInstanceManager();
            net.minestom.server.instance.InstanceContainer newMineInstance = instanceManager.createInstanceContainer();
        
            // Set up proper mine world generator
            newMineInstance.setGenerator(unit -> {
                // Create a proper mine layout
                unit.modifier().fillHeight(0, 1, net.minestom.server.instance.block.Block.BEDROCK);
            
                // Mine layers with different ores
                unit.modifier().fillHeight(1, 10, net.minestom.server.instance.block.Block.STONE);
                unit.modifier().fillHeight(10, 15, net.minestom.server.instance.block.Block.COAL_ORE);
                unit.modifier().fillHeight(15, 18, net.minestom.server.instance.block.Block.IRON_ORE);
                unit.modifier().fillHeight(18, 20, net.minestom.server.instance.block.Block.GOLD_ORE);
            
                // Surface layer
                unit.modifier().fillHeight(20, 21, net.minestom.server.instance.block.Block.GRASS_BLOCK);
            
                // Create spawn platform
                int mineSize = getMineSize();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        unit.modifier().setBlock(x, 21, z, net.minestom.server.instance.block.Block.STONE_BRICKS);
                        unit.modifier().setBlock(x, 22, z, net.minestom.server.instance.block.Block.AIR);
                        unit.modifier().setBlock(x, 23, z, net.minestom.server.instance.block.Block.AIR);
                    }
                }
            });
        
            this.mineInstance = newMineInstance;
        
            // Register with SchematicWorldManager for tracking
            SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();
            if (schematicManager != null) {
                // Create a custom SchematicWorld entry with proper constructor parameters
                int mineSize = getMineSize();
                int minX = -mineSize / 2;
                int minZ = -mineSize / 2;
                int maxX = mineSize / 2;
                int maxZ = mineSize / 2;
                int minY = 0;
                int maxY = 50;
                Pos spawnPoint = new Pos(0, 22, 0);
            
                var schematicWorld = new SchematicWorldManager.SchematicWorld(
                    minX, minY, minZ, maxX, maxY, maxZ, spawnPoint
                );
                schematicWorld.setInstance(newMineInstance);
                schematicManager.registerWorld(worldName, schematicWorld);
            }
        } catch (Exception e) {
            System.err.println("[PrivateMine] Failed to create proper mine world: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void teleportPlayerToInstance(Player player) {
        try {
            if (mineInstance == null) {
                player.sendMessage("§cYour mine instance is not available! Please try again in a moment.");
                return;
            }

            // Check if player is already in this mine instance
            if (player.getInstance() == mineInstance) {
                player.sendMessage("§eYou are already in your mine!");
                return;
            }

            // Use proper mine spawn point
            Pos spawnPoint = new Pos(0, 22, 0);
        
            // Teleport the player
            player.setInstance(mineInstance, spawnPoint);
        
            // Track with SchematicWorldManager
            SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();
            if (schematicManager != null) {
                schematicManager.trackPlayerInWorld(player, worldName);
            }
        
            player.sendMessage("§aTeleported to " + mineName + "!");
            player.sendMessage("§7Mine Size: §e" + getMineSize() + "x" + getMineSize());
            player.sendMessage("§7Beacon Level: §e" + beaconLevel);
        
        } catch (Exception e) {
            System.err.println("Error in teleportPlayerToInstance for player " + player.getUsername() + ": " + e.getMessage());
            player.sendMessage("§cError teleporting to mine! Please try again.");
            e.printStackTrace();
        }
    }

    public void onBlockBreak(String blockType) {
        // This method handles when a block is broken in this private mine
        // Apply beacon effects and multipliers
        
        double finalMultiplier = multiplier;
        
        // Apply beacon effects
        if (beaconLevel > 0) {
            finalMultiplier += (beaconLevel * 0.1); // 10% bonus per beacon level
        }
        
        // You could add more functionality here like:
        // - Increment a blocks broken counter
        // - Apply beacon effects
        // - Check for special drops based on mine level
        // - Custom block regeneration
    }

    // Getters and setters
    public String getId() { return id; }
    public String getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public String getMineName() { return mineName; }
    public void setMineName(String mineName) { this.mineName = mineName; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) {
        this.taxRate = Math.max(0.0, Math.min(1.0, taxRate));
    }

    public int getSizeLevel() { return sizeLevel; }
    public void setSizeLevel(int sizeLevel) { this.sizeLevel = sizeLevel; }

    public int getBeaconLevel() { return beaconLevel; }
    public void setBeaconLevel(int beaconLevel) { this.beaconLevel = beaconLevel; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public Instance getMineInstance() { return mineInstance; }
    public void setMineInstance(Instance mineInstance) { this.mineInstance = mineInstance; }

    public Set<String> getAllowedPlayers() { return new HashSet<>(allowedPlayers); }
    public void addAllowedPlayer(String playerUUID) { allowedPlayers.add(playerUUID); }
    public void removeAllowedPlayer(String playerUUID) { allowedPlayers.remove(playerUUID); }

    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
}