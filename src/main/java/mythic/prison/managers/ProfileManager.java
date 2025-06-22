package mythic.prison.managers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import net.minestom.server.entity.Player;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager {
    private final Map<String, PlayerProfile> playerProfiles = new ConcurrentHashMap<>();
    private final Map<String, Long> playerJoinTimes = new ConcurrentHashMap<>();
    private MongoCollection<Document> profileCollection;

    public ProfileManager() {
        // Initialize MongoDB collection when manager is created
        CompletableFuture.runAsync(() -> {
            try {
                if (MythicPrison.getInstance().getMongoManager() != null) {
                    this.profileCollection = MythicPrison.getInstance().getMongoManager()
                            .getDatabase().getCollection("player_profiles");
                    System.out.println("[ProfileManager] Connected to MongoDB collection 'player_profiles'");
                }
            } catch (Exception e) {
                System.err.println("[ProfileManager] Failed to initialize MongoDB: " + e.getMessage());
            }
        });
    }

    public void initializePlayer(Player player) {
        String uuid = player.getUuid().toString();
        playerJoinTimes.put(uuid, System.currentTimeMillis());
        
        loadProfileFromDatabase(uuid).thenAccept(profile -> {
            if (profile == null) {
                profile = new PlayerProfile(uuid);
                System.out.println("[ProfileManager] Created new profile for " + player.getUsername());
            } else {
                System.out.println("[ProfileManager] Loaded existing profile for " + player.getUsername());
            }
            
            profile.setUsername(player.getUsername());
            profile.setLastSeen(System.currentTimeMillis());
            playerProfiles.put(uuid, profile);
            
            // Ensure default multipliers are set
            ensureDefaultMultipliers(player);
            
            // Sync multipliers with MultiplierManager
            syncMultipliers(player);
            
            // Save immediately to update last seen and username
            saveProfileToDatabase(profile);
        });

        // Initialize friends system
        MythicPrison.getInstance().getFriendsManager().initializePlayer(player);
    }

    public void removePlayer(Player player) {
        String uuid = player.getUuid().toString();
        
        // Calculate playtime for this session
        Long joinTime = playerJoinTimes.remove(uuid);
        if (joinTime != null) {
            long sessionTime = System.currentTimeMillis() - joinTime;
            PlayerProfile profile = playerProfiles.get(uuid);
            if (profile != null) {
                profile.addPlaytime(sessionTime);
            }
        }
        
        // Save profile before removing from memory
        PlayerProfile profile = playerProfiles.remove(uuid);
        if (profile != null) {
            profile.setLastSeen(System.currentTimeMillis());
            saveProfileToDatabase(profile);
            System.out.println("[ProfileManager] Saved and removed profile for " + player.getUsername());
        }

        // Cleanup friends system
        MythicPrison.getInstance().getFriendsManager().removePlayer(player);
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUuid().toString());
    }

    public PlayerProfile getProfile(String uuid) {
        // Get from playerProfiles cache first (not profileCache)
        PlayerProfile profile = playerProfiles.get(uuid);
        if (profile != null) {
            return profile;
        }
        
        // Load from database synchronously (since this is called in many places that expect immediate results)
        try {
            CompletableFuture<PlayerProfile> future = loadProfileFromDatabase(uuid);
            profile = future.get(); // Wait for database result
            
            if (profile == null) {
                // Create new profile if doesn't exist
                profile = new PlayerProfile(uuid);
                saveProfileToDatabase(profile);
            }
            
            // Cache it in playerProfiles
            playerProfiles.put(uuid, profile);
            return profile;
            
        } catch (Exception e) {
            System.err.println("[ProfileManager] Error loading profile for " + uuid + ": " + e.getMessage());
            
            // Create emergency profile
            profile = new PlayerProfile(uuid);
            playerProfiles.put(uuid, profile);
            return profile;
        }
    }

    // Database operations
    private CompletableFuture<PlayerProfile> loadProfileFromDatabase(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (profileCollection == null) return null;
                
                Document doc = profileCollection.find(Filters.eq("uuid", uuid)).first();
                if (doc != null) {
                    return documentToProfile(doc);
                }
            } catch (Exception e) {
                System.err.println("[ProfileManager] Error loading profile for " + uuid + ": " + e.getMessage());
            }
            return null;
        });
    }

    private void saveProfileToDatabase(PlayerProfile profile) {
        if (profileCollection == null) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                Document doc = profileToDocument(profile);
                profileCollection.replaceOne(
                    Filters.eq("uuid", profile.getUuid()),
                    doc,
                    new ReplaceOptions().upsert(true)
                );
            } catch (Exception e) {
                System.err.println("[ProfileManager] Error saving profile for " + profile.getUuid() + ": " + e.getMessage());
            }
        });
    }

    private PlayerProfile documentToProfile(Document doc) {
        PlayerProfile profile = new PlayerProfile(doc.getString("uuid"));
        
        // Basic info
        profile.setUsername(doc.getString("username"));
        profile.setLastSeen(doc.getLong("lastSeen") != null ? doc.getLong("lastSeen") : System.currentTimeMillis());
        
        // Currencies
        Document currenciesDoc = doc.get("currencies", Document.class);
        if (currenciesDoc != null) {
            for (Map.Entry<String, Object> entry : currenciesDoc.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    profile.setBalance(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                }
            }
        }
        
        // Progression
        profile.setCurrentRank(doc.getString("currentRank") != null ? doc.getString("currentRank") : "A");
        profile.setPrestige(doc.getInteger("prestige") != null ? doc.getInteger("prestige") : 0);
        profile.setRebirth(doc.getInteger("rebirth") != null ? doc.getInteger("rebirth") : 0);
        profile.setTotalPlaytime(doc.getLong("totalPlaytime") != null ? doc.getLong("totalPlaytime") : 0);
        
        // Stats
        profile.setBlocksMined(doc.getLong("blocksMined") != null ? doc.getLong("blocksMined") : 0);
        profile.setMonstersKilled(doc.getLong("monstersKilled") != null ? doc.getLong("monstersKilled") : 0);
        profile.setTotalMoneyEarned(doc.getDouble("totalMoneyEarned") != null ? doc.getDouble("totalMoneyEarned") : 0.0);
        profile.setCommandsUsed(doc.getLong("commandsUsed") != null ? doc.getLong("commandsUsed") : 0);
        profile.setDeathCount(doc.getLong("deathCount") != null ? doc.getLong("deathCount") : 0);
        
        // Multipliers
        Document multipliersDoc = doc.get("multipliers", Document.class);
        Document multiplierExpiryDoc = doc.get("multiplierExpiry", Document.class);
        if (multipliersDoc != null) {
            for (Map.Entry<String, Object> entry : multipliersDoc.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    double multiplier = ((Number) entry.getValue()).doubleValue();
                    long expiry = 0;
                    if (multiplierExpiryDoc != null && multiplierExpiryDoc.get(entry.getKey()) instanceof Number) {
                        expiry = ((Number) multiplierExpiryDoc.get(entry.getKey())).longValue();
                    }
                    profile.setMultiplier(entry.getKey(), multiplier, expiry - System.currentTimeMillis());
                }
            }
        }
        
        // Settings
        profile.setAutoSellEnabled(doc.getBoolean("autoSellEnabled") != null ? doc.getBoolean("autoSellEnabled") : false);
        profile.setPvpEnabled(doc.getBoolean("pvpEnabled") != null ? doc.getBoolean("pvpEnabled") : true);
        profile.setChatEnabled(doc.getBoolean("chatEnabled") != null ? doc.getBoolean("chatEnabled") : true);
        profile.setLanguage(doc.getString("language") != null ? doc.getString("language") : "en");
        
        // Social
        profile.setGangId(doc.getString("gangId"));
        profile.setGangRank(doc.getString("gangRank"));
        
        // Custom data
        Document customDataDoc = doc.get("customData", Document.class);
        if (customDataDoc != null) {
            for (Map.Entry<String, Object> entry : customDataDoc.entrySet()) {
                profile.setCustomData(entry.getKey(), entry.getValue());
            }
        }
        
        return profile;
    }

    private Document profileToDocument(PlayerProfile profile) {
        Document doc = new Document("uuid", profile.getUuid())
            .append("username", profile.getUsername())
            .append("lastSeen", profile.getLastSeen())
            .append("currencies", new Document(profile.getCurrencies()))
            .append("currentRank", profile.getCurrentRank())
            .append("prestige", profile.getPrestige())
            .append("rebirth", profile.getRebirth())
            .append("totalPlaytime", profile.getTotalPlaytime())
            .append("blocksMined", profile.getBlocksMined())
            .append("monstersKilled", profile.getMonstersKilled())
            .append("totalMoneyEarned", profile.getTotalMoneyEarned())
            .append("commandsUsed", profile.getCommandsUsed())
            .append("deathCount", profile.getDeathCount())
            .append("multipliers", new Document(profile.getMultipliers()))
            .append("multiplierExpiry", new Document(profile.getMultiplierExpiry()))
            .append("autoSellEnabled", profile.isAutoSellEnabled())
            .append("pvpEnabled", profile.isPvpEnabled())
            .append("chatEnabled", profile.isChatEnabled())
            .append("language", profile.getLanguage())
            .append("gangId", profile.getGangId())
            .append("gangRank", profile.getGangRank())
            .append("customData", new Document(profile.getCustomData()));
        
        return doc;
    }

    public CompletableFuture<Void> saveAllProfiles() {
        return CompletableFuture.runAsync(() -> {
            System.out.println("[ProfileManager] Saving all profiles...");
            for (PlayerProfile profile : playerProfiles.values()) {
                saveProfileToDatabase(profile);
            }
            System.out.println("[ProfileManager] All profiles saved!");
        });
    }

    // Utility methods for other managers
    public CompletableFuture<PlayerProfile> getOfflineProfile(String uuid) {
        PlayerProfile cachedProfile = playerProfiles.get(uuid);
        if (cachedProfile != null) {
            return CompletableFuture.completedFuture(cachedProfile);
        }
        return loadProfileFromDatabase(uuid);
    }

    public void saveProfile(PlayerProfile profile) {
        saveProfileToDatabase(profile);
    }

    // Add this method to ProfileManager class if it doesn't exist:
    public void ensureDefaultMultipliers(Player player) {
        PlayerProfile profile = getProfile(player);
        if (profile != null) {
            String[] defaultTypes = {"money", "tokens", "souls", "beacons", "experience", "gems"};
            for (String type : defaultTypes) {
                if (profile.getMultiplier(type) == 1.0 && !profile.getMultipliers().containsKey(type)) {
                    profile.setMultiplier(type, 1.0, 0); // Set explicit 1.0x multiplier
                }
            }
        }
    }

    // Add this method to sync multipliers
    public void syncMultipliers(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                PlayerProfile profile = getProfile(player);
                if (profile != null) {
                    MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
                    if (multiplierManager != null) {
                        // Copy multipliers from PlayerProfile to MultiplierManager
                        Map<String, Double> multipliers = profile.getMultipliers();
                        Map<String, Long> expiry = profile.getMultiplierExpiry();
                        
                        String playerUUID = player.getUuid().toString();
                        
                        if (multipliers != null && !multipliers.isEmpty()) {
                            for (Map.Entry<String, Double> entry : multipliers.entrySet()) {
                                String type = entry.getKey();
                                double value = entry.getValue();
                                long duration = 0;
                                
                                if (expiry != null && expiry.containsKey(type)) {
                                    long expiryTime = expiry.get(type);
                                    duration = Math.max(0, expiryTime - System.currentTimeMillis());
                                }
                                
                                multiplierManager.setMultiplier(player, type, value, duration);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[ProfileManager] Error syncing multipliers for " + player.getUsername() + ": " + e.getMessage());
            }
        });
    }
}