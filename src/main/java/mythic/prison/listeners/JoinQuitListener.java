package mythic.prison.listeners;

import mythic.prison.MythicPrison;
import mythic.prison.managers.PickaxeManager;
import mythic.prison.managers.RankingManager;
import mythic.prison.utils.ChatUtil; // Add this import
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class JoinQuitListener {
    
    // Track players who have already received the welcome message this session
    private static final Set<String> welcomedPlayers = new HashSet<>();

    // Safe spawn position - Fixed to normal ground level
    private static final Pos SAFE_SPAWN_POS = new Pos(0, 64, 0, 0, 0);

    private void initializePlayerProfile(Player player) {
    try {
        // Get or create player profile
        var profileManager = MythicPrison.getInstance().getProfileManager();
        if (profileManager != null) {
            // This will create a profile if it doesn't exist
            var profile = profileManager.getProfile(player.getUuid().toString());
            if (profile != null) {
                // Update username and last seen
                profile.setUsername(player.getUsername());
                profile.setLastSeen(System.currentTimeMillis());
                profileManager.saveProfile(profile);
            }
        }
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error initializing player profile: " + e.getMessage());
    }
}

private void ensureDefaultMultipliers(Player player) {
    try {
        var multiplierManager = MythicPrison.getInstance().getMultiplierManager();
        if (multiplierManager != null) {
            // Ensure player has default multipliers (1.0x for all types)
            String[] defaultTypes = {"money", "tokens", "souls", "beacons", "experience", "gems"};
            for (String type : defaultTypes) {
                if (multiplierManager.getMultiplier(player, type) < 1.0) {
                    multiplierManager.setMultiplier(player, type, 1.0, 0); // Permanent 1.0x multiplier
                }
            }
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

private void ensureSafeSpawnPosition(Player player) {
    try {
        // Get the main instance
        Instance mainInstance = MythicPrison.getInstance().getMainInstance();
        if (mainInstance != null) {
            // Check if player's current position is safe
            Pos currentPos = player.getPosition();
            
            // If player is below a certain Y level or in an unsafe position, teleport to safe spawn
            if (currentPos.y() < 60 || !isPositionSafe(mainInstance, currentPos)) {
                player.teleport(SAFE_SPAWN_POS);
                System.out.println("[JoinQuitListener] Moved player " + player.getUsername() + " to safe spawn position");
            }
        }
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error ensuring safe spawn position: " + e.getMessage());
        // Fallback: teleport to safe spawn if there's any error
        try {
            player.teleport(SAFE_SPAWN_POS);
        } catch (Exception fallbackError) {
            System.err.println("[JoinQuitListener] Failed to teleport to safe spawn: " + fallbackError.getMessage());
        }
    }
}

private boolean isPositionSafe(Instance instance, Pos position) {
    try {
        // Check if the position has solid ground and no blocks above player's head
        Pos groundPos = new Pos(position.x(), position.y() - 1, position.z());
        Pos headPos = new Pos(position.x(), position.y() + 1, position.z());
        
        Block groundBlock = instance.getBlock(groundPos);
        Block headBlock = instance.getBlock(headPos);
        
        // Position is safe if there's solid ground and no block above head
        return groundBlock.isSolid() && !headBlock.isSolid();
    } catch (Exception e) {
        // If we can't check, assume it's not safe
        return false;
    }
}

    // ... existing methods ...

    public void onJoin(PlayerSpawnEvent event) {
        Player player = event.getPlayer();
        
        try {
            // Initialize all player systems
            initializePlayerProfile(player);
            ensureDefaultMultipliers(player);
            createPlayerScoreboard(player);

            // Ensure player has their pickaxe
            PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
            if (pickaxeManager != null) {
                pickaxeManager.forceGivePickaxe(player);
            }

            // Update player prefix on join
            RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
            if (rankingManager != null) {
                rankingManager.updatePlayerPrefix(player);
            }

            // Safe spawn positioning
            ensureSafeSpawnPosition(player);

            // Welcome message (only once per session)
            String playerName = player.getUsername();
            if (!welcomedPlayers.contains(playerName)) {
                welcomedPlayers.add(playerName);
                
                // Send welcome message after a short delay to ensure everything is loaded
                MythicPrison.getInstance().getScheduler().schedule(() -> {
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
                }, 1, TimeUnit.SECONDS);
            }

            System.out.println("[Join] Player " + player.getUsername() + " joined the game");
            
        } catch (Exception e) {
            System.err.println("[MythicPrison] Error handling player join: " + e.getMessage());
            e.printStackTrace();
        }
    }

public void onPlayerConfiguration(AsyncPlayerConfigurationEvent event) {
    Player player = event.getPlayer();
    
    try {
        // Set the spawning instance for the player
        Instance mainInstance = MythicPrison.getInstance().getMainInstance();
        if (mainInstance != null) {
            event.setSpawningInstance(mainInstance);
        } else {
            System.err.println("[JoinQuitListener] Main instance is null! Cannot set spawning instance for player: " + player.getUsername());
        }
        
        System.out.println("[Configuration] Player " + player.getUsername() + " is being configured");
        
    } catch (Exception e) {
        System.err.println("[MythicPrison] Error handling player configuration: " + e.getMessage());
        e.printStackTrace();
    }
}
public void onQuit(PlayerDisconnectEvent event) {
    Player player = event.getPlayer();
    
    try {
        // Remove player from welcomed players set to allow welcome message on next join
        welcomedPlayers.remove(player.getUsername());
        
        // Save player data before they leave
        savePlayerData(player);
        
        // Remove passive effects if they have any
        removePassiveEffects(player);
        
        // Clean up player scoreboard
        cleanupPlayerScoreboard(player);
        
        System.out.println("[Quit] Player " + player.getUsername() + " left the game");
        
    } catch (Exception e) {
        System.err.println("[MythicPrison] Error handling player quit: " + e.getMessage());
        e.printStackTrace();
    }
}

private void savePlayerData(Player player) {
    try {
        // Save player profile
        var profileManager = MythicPrison.getInstance().getProfileManager();
        if (profileManager != null) {
            var profile = profileManager.getProfile(player.getUuid().toString());
            if (profile != null) {
                profile.setLastSeen(System.currentTimeMillis());
                profileManager.saveProfile(profile);
            }
        }
        
        // Save any other player data as needed
        // You can add more save operations here
        
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error saving player data: " + e.getMessage());
    }
}

private void removePassiveEffects(Player player) {
    try {
        var effectsManager = MythicPrison.getInstance().getPickaxeEffectsManager();
        if (effectsManager != null) {
            effectsManager.removePassiveEffects(player);
        }
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error removing passive effects: " + e.getMessage());
    }
}

private void cleanupPlayerScoreboard(Player player) {
    try {
        var scoreboardManager = MythicPrison.getInstance().getScoreboardManager();
        if (scoreboardManager != null) {
            scoreboardManager.removeScoreboard(player);
        }
    } catch (Exception e) {
        System.err.println("[JoinQuitListener] Error cleaning up scoreboard: " + e.getMessage());
    }
}
}