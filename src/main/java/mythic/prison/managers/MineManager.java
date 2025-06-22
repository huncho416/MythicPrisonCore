package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.mine.PrivateMine;
import mythic.prison.managers.SchematicWorldManager.SchematicWorld;
import net.minestom.server.entity.Player;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.coordinate.Pos;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MineManager {

    private final Map<String, PrivateMine> privateMines = new ConcurrentHashMap<>();
    private final Map<String, String> playerMines = new ConcurrentHashMap<>();

    public void initializePlayer(Player player) {
        // Automatically create a mine for new players
        if (!hasPlayerMine(player)) {
            createPrivateMine(player);
        }
    }

    public void initializePlayer(Object player) {
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public boolean hasPlayerMine(Player player) {
        String playerUUID = player.getUuid().toString();
        return playerMines.containsKey(playerUUID) && 
               privateMines.containsKey(playerMines.get(playerUUID));
    }

    public boolean hasPlayerMine(Object player) {
        if (player instanceof Player p) {
            return hasPlayerMine(p);
        }
        return false;
    }

    public PrivateMine createPrivateMine(Player player) {
        String playerUUID = player.getUuid().toString();
        
        if (playerMines.containsKey(playerUUID)) {
            return privateMines.get(playerMines.get(playerUUID));
        }

        PrivateMine mine = new PrivateMine(playerUUID, player.getUsername());
        String mineId = mine.getId();
        String worldName = mine.getWorldName();

        // Create the schematic world for this mine
        SchematicWorldManager schematicWorldManager = MythicPrison.getInstance().getSchematicWorldManager();
        schematicWorldManager.createSchematicWorld(worldName, "mine").thenAccept(instance -> {
            if (instance != null) {
                mine.setMineInstance(instance);
                System.out.println("[MineManager] Created mine world: " + worldName + " for player: " + player.getUsername());
            } else {
                System.err.println("[MineManager] Failed to create mine world for player: " + player.getUsername());
            }
        });

        privateMines.put(mineId, mine);
        playerMines.put(playerUUID, mineId);

        return mine;
    }

    public PrivateMine getPlayerMine(Player player) {
        String playerUUID = player.getUuid().toString();
        String mineId = playerMines.get(playerUUID);
        return mineId != null ? privateMines.get(mineId) : null;
    }

    public PrivateMine getPlayerMine(Object player) {
        if (player instanceof Player p) {
            return getPlayerMine(p);
        }
        return null;
    }

    public boolean upgradeSize(Player player) {
        PrivateMine mine = getPlayerMine(player);
        if (mine == null) {
            return false;
        }
        
        // Check if player has enough money
        double cost = mine.getSizeUpgradeCost();
        var currencyManager = MythicPrison.getInstance().getCurrencyManager();
        
        if (currencyManager.getBalance(player, "money") >= cost) {
            if (mine.upgradeSize()) {
                currencyManager.removeBalance(player, "money", cost);
                return true;
            }
        }
        
        return false;
    }

    public boolean upgradeBeacons(Player player) {
        PrivateMine mine = getPlayerMine(player);
        if (mine == null) {
            return false;
        }
        
        // Check if player has enough money
        double cost = mine.getBeaconUpgradeCost();
        var currencyManager = MythicPrison.getInstance().getCurrencyManager();
        
        if (currencyManager.getBalance(player, "money") >= cost) {
            if (mine.upgradeBeacons()) {
                currencyManager.removeBalance(player, "money", cost);
                return true;
            }
        }
        
        return false;
    }

    public void teleportToMine(Player player) {
    try {
        PrivateMine mine = getPlayerMine(player);
        
        if (mine != null) {
            mine.teleportPlayer(player);
        } else {
            // If no private mine exists, create one first
            createPrivateMine(player);
            
            // Schedule teleportation after mine creation
            MythicPrison.getInstance().getScheduler().schedule(() -> {
                PrivateMine newMine = getPlayerMine(player);
                if (newMine != null) {
                    newMine.teleportPlayer(player);
                } else {
                    player.sendMessage("§cFailed to create your mine! Please try again.");
                }
            }, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    } catch (Exception e) {
        System.err.println("[MineManager] Error teleporting player to mine: " + e.getMessage());
        player.sendMessage("§cError teleporting to your mine! Please try again.");
        e.printStackTrace();
    }
}

    public void teleportToMine(Player player, String targetPlayerName) {
        Player targetPlayer = findPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            return;
        }
        
        PrivateMine targetMine = getPlayerMine(targetPlayer);
        if (targetMine != null && targetMine.canPlayerAccess(player.getUuid().toString())) {
            targetMine.teleportPlayer(player);
        }
    }

    public Collection<PrivateMine> getAllMines() {
        return privateMines.values();
    }

    public List<PrivateMine> getPublicMines() {
        return privateMines.values().stream()
                .filter(PrivateMine::isPublic)
                .collect(Collectors.toList());
    }

    public PrivateMine getMineById(String id) {
        return privateMines.get(id);
    }

    private Player findPlayerByName(String playerName) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equalsIgnoreCase(playerName)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    public void onBlockBreak(Player player, String blockType) {
        PrivateMine mine = getPlayerMine(player);
        if (mine != null) {
            // Handle block break in the player's mine
            mine.onBlockBreak(blockType);
            
            // Apply mine-specific multipliers
            double mineMultiplier = mine.getMultiplier();
            
            // You can add additional mine-specific logic here
            // For example, beacon effects, special drops, etc.
        }
    }

private void executeGo(Player player) {
    System.out.println("[MineCommand] NUCLEAR OPTION - Creating brand new mine instance");
    
    try {
        // Create a completely new instance directly
        net.minestom.server.instance.InstanceManager instanceManager = net.minestom.server.MinecraftServer.getInstanceManager();
        net.minestom.server.instance.InstanceContainer newMineInstance = instanceManager.createInstanceContainer();
        
        // Set up a simple flat world generator for testing
        newMineInstance.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 1, net.minestom.server.instance.block.Block.BEDROCK);
            unit.modifier().fillHeight(1, 60, net.minestom.server.instance.block.Block.STONE);
            unit.modifier().fillHeight(60, 65, net.minestom.server.instance.block.Block.COAL_ORE);
            unit.modifier().fillHeight(65, 66, net.minestom.server.instance.block.Block.GRASS_BLOCK);
        });
        
        // Create spawn platform at mine coordinates
        Pos mineSpawn = new Pos(0, 67, 0);
        
        System.out.println("[MineCommand] Created new mine instance: " + newMineInstance.hashCode());
        System.out.println("[MineCommand] Teleporting to: " + mineSpawn);
        
        // DIRECT teleportation bypassing ALL managers
        player.setInstance(newMineInstance, mineSpawn);
        
        player.sendMessage("§a[NUCLEAR] Created and teleported to brand new mine instance!");
        player.sendMessage("§7Instance ID: " + newMineInstance.hashCode());
        
        // Verify the teleportation worked
        MythicPrison.getInstance().getScheduler().schedule(() -> {
            if (player.getInstance() == newMineInstance) {
                player.sendMessage("§a✅ SUCCESS! You are in the new mine instance!");
                System.out.println("[MineCommand] SUCCESS: Player is in new instance");
            } else {
                player.sendMessage("§c❌ FAILED! You are not in the new mine instance!");
                System.out.println("[MineCommand] FAILED: Player is in: " + player.getInstance().getClass().getSimpleName());
            }
        }, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        
    } catch (Exception e) {
        System.err.println("[MineCommand] Nuclear option failed: " + e.getMessage());
        e.printStackTrace();
        player.sendMessage("§cNuclear option failed: " + e.getMessage());
    }
}
}