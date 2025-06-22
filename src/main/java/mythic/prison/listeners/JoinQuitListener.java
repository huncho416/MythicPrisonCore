package mythic.prison.listeners;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;

import java.util.HashSet;
import java.util.Set;

public class JoinQuitListener {
    
    // Track players who have already received the welcome message this session
    private static final Set<String> welcomedPlayers = new HashSet<>();

public void onPlayerConfiguration(AsyncPlayerConfigurationEvent event) {
    try {
        Player player = event.getPlayer();
        
        // Set the spawning instance - this is required!
        MythicPrison instance = MythicPrison.getInstance();
        if (instance != null && instance.getMainInstance() != null) {
            event.setSpawningInstance(instance.getMainInstance());
        } else {
            // Fallback to creating a basic instance if main instance is not available
            var fallbackInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
            fallbackInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK));
            event.setSpawningInstance(fallbackInstance);
        }
        
        if (player != null && player.getUsername() != null) {
            // Remove from welcomed set to ensure they get welcome message on spawn
            welcomedPlayers.remove(player.getUsername());
        }
        
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error in player configuration: " + e.getMessage());
        e.printStackTrace();
    }
}

    public void onJoin(PlayerSpawnEvent event) {
    try {
        Player player = event.getPlayer();
        String playerName = player.getUsername();

        if (playerName != null) {
            // Initialize player profile first
            initializePlayerProfile(player);

            // Ensure player has default multipliers
            ensureDefaultMultipliers(player);

            // Create scoreboard for the player
            createPlayerScoreboard(player);

            // Only send welcome message if player hasn't been welcomed this session
            if (!welcomedPlayers.contains(playerName)) {
                // Send welcome message
                ChatUtil.sendMessage(player, "");
                ChatUtil.sendMessage(player, "");
                ChatUtil.sendMessage(player, "                    §5★§d★§f★ §d§lMythic§f§lPvP §5★§d★§f★");
                ChatUtil.sendMessage(player, "              §7You have joined the prison realm.");
                ChatUtil.sendMessage(player, "");
                ChatUtil.sendMessage(player, "                §5§lSTORE §fstore.mythicpvp.net");
                ChatUtil.sendMessage(player, "                §d§lFORUMS §fwww.mythicpvp.net");
                ChatUtil.sendMessage(player, "                §f§lDISCORD §fdiscord.mythicpvp.com");
                ChatUtil.sendMessage(player, "");
                ChatUtil.sendMessage(player, "");
                
                // Mark player as welcomed
                welcomedPlayers.add(playerName);
            }
        }
    } catch (Exception e) {
        // Silently handle errors
    }
}

    public void onQuit(PlayerDisconnectEvent event) {
        try {
            Player player = event.getPlayer();
            String playerName = player.getUsername();

            if (playerName != null) {
                // Remove from welcomed set when player disconnects
                welcomedPlayers.remove(playerName);
                
                // Clean up player scoreboard
                removePlayerScoreboard(player);
                
                // Additional cleanup can go here
                // Save player data, etc.
            }
        } catch (Exception e) {
            // Silently handle errors
        }
    }

private void initializePlayerProfile(Player player) {
    try {
        // Initialize various managers for the player
        MythicPrison instance = MythicPrison.getInstance();
    
        if (instance.getProfileManager() != null) {
            instance.getProfileManager().initializePlayer(player);
        }
    
        if (instance.getBackpackManager() != null) {
            instance.getBackpackManager().initializePlayer(player);
        }
    
        if (instance.getPickaxeManager() != null) {
            instance.getPickaxeManager().initializePlayer(player);
            // Force give pickaxe immediately on join
            instance.getPickaxeManager().forceGivePickaxe(player);
        }
    
        if (instance.getMineManager() != null) {
            instance.getMineManager().initializePlayer(player);
        }
    
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error initializing player profile: " + e.getMessage());
    }
}

    private void ensureDefaultMultipliers(Player player) {
        try {
            var multiplierManager = MythicPrison.getInstance().getMultiplierManager();
            if (multiplierManager != null) {
                // MultiplierManager doesn't have initializePlayer method
                // Default multipliers are handled elsewhere or don't need explicit initialization
            }
        } catch (Exception e) {
            System.err.println("[JoinQuitListener] Error ensuring default multipliers: " + e.getMessage());
        }
    }

    private void createPlayerScoreboard(Player player) {
        try {
            var scoreboardManager = MythicPrison.getInstance().getScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.createScoreboard(player);
            }
        } catch (Exception e) {
            System.err.println("[JoinQuitListener] Error creating player scoreboard: " + e.getMessage());
        }
    }

    private void removePlayerScoreboard(Player player) {
        try {
            var scoreboardManager = MythicPrison.getInstance().getScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.removePlayerScoreboard(player);
            }
        } catch (Exception e) {
            System.err.println("[JoinQuitListener] Error removing player scoreboard: " + e.getMessage());
        }
    }
}