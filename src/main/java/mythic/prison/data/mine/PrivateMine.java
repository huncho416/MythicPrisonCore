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
                // Try to get the instance from SchematicWorldManager
                SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();
                if (schematicManager != null) {
                    var world = schematicManager.getWorld(worldName);
                    if (world != null) {
                        mineInstance = world.getInstance();
                    }
                }
            }

            if (mineInstance != null) {
                // Check if player is already in this mine instance
                if (player.getInstance() == mineInstance) {
                    player.sendMessage("§eYou are already in your mine!");
                    return;
                }

                // Get spawn point from schematic world or use default
                Pos spawnPoint = new Pos(0, 21, 0); // Default spawn point above the mine
                
                SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();
                if (schematicManager != null) {
                    var world = schematicManager.getWorld(worldName);
                    if (world != null) {
                        spawnPoint = world.getSpawnPoint();
                    }
                }
                
                player.setInstance(mineInstance, spawnPoint);
                
                // Track player in the world
                if (schematicManager != null) {
                    schematicManager.trackPlayerInWorld(player, worldName);
                }
                
                player.sendMessage("§aTeleported to " + mineName + "!");
            } else {
                player.sendMessage("§cYour mine instance is not available! Please try again in a moment.");
                System.err.println("Mine instance is null for player: " + ownerName);
            }
            
        } catch (Exception e) {
            System.err.println("Error teleporting player " + player.getUsername() + " to mine: " + e.getMessage());
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
        
        System.out.println("Block broken in " + mineName + ": " + blockType + " (Multiplier: " + finalMultiplier + ")");
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