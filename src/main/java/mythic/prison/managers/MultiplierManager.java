package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import mythic.prison.player.Profile;

public class MultiplierManager {
    
    private final Map<String, Map<String, Double>> playerMultipliers = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> multiplierExpiry = new ConcurrentHashMap<>();

    public double getMultiplier(Player player, String type) {
        String uuid = player.getUuid().toString();
        
        // First check PlayerProfile
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        if (profile != null) {
            double profileMultiplier = profile.getMultiplier(type);
            if (profileMultiplier > 1.0) {
                return profileMultiplier;
            }
        }
        
        // Then check local storage
        Map<String, Double> multipliers = playerMultipliers.get(uuid);
        if (multipliers != null) {
            // Check if multiplier has expired
            Map<String, Long> expiry = multiplierExpiry.get(uuid);
            if (expiry != null && expiry.containsKey(type)) {
                long expiryTime = expiry.get(type);
                if (System.currentTimeMillis() > expiryTime) {
                    // Multiplier expired, remove it
                    multipliers.remove(type);
                    expiry.remove(type);
                    return 1.0;
                }
            }
            
            return multipliers.getOrDefault(type, 1.0);
        }
        
        return 1.0;
    }

    public double getMultiplier(Object player, String type) {
        if (player instanceof Player p) {
            return getMultiplier(p, type);
        }
        return 1.0;
    }

    public void setMultiplier(Player player, String type, double amount, long durationMs) {
        try {
            // Update in-memory storage
            String playerUUID = player.getUuid().toString();
            playerMultipliers.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(type, amount);
        
            if (durationMs > 0) {
                multiplierExpiry.computeIfAbsent(playerUUID, k -> new HashMap<>())
                    .put(type, System.currentTimeMillis() + durationMs);
            } else {
                Map<String, Long> expiry = multiplierExpiry.get(playerUUID);
                if (expiry != null) {
                    expiry.remove(type);
                }
            }
        
            // Save to database asynchronously
            saveMultipliersToDatabase(player);
        
        } catch (Exception e) {
            System.err.println("[MultiplierManager] Error setting multiplier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMultiplier(Object player, String type, double multiplier) {
        if (player instanceof Player p) {
            setMultiplier(p, type, multiplier, 0);
        }
    }

    public void addMultiplier(Player player, String type, double amount, long durationMs) {
        double current = getMultiplier(player, type);
        setMultiplier(player, type, current + amount, durationMs);
    }

    public void addMultiplier(Object player, String type, double amount) {
        if (player instanceof Player p) {
            addMultiplier(p, type, amount, 0);
        }
    }

    public void removeMultiplier(Player player, String type) {
        try {
            String playerUUID = player.getUuid().toString();
        
            // Remove from in-memory storage
            Map<String, Double> multipliers = playerMultipliers.get(playerUUID);
            if (multipliers != null) {
                multipliers.remove(type);
            }
        
            Map<String, Long> expiry = multiplierExpiry.get(playerUUID);
            if (expiry != null) {
                expiry.remove(type);
            }
        
            // Save to database asynchronously
            saveMultipliersToDatabase(player);
        
        } catch (Exception e) {
            System.err.println("[MultiplierManager] Error removing multiplier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getTotalMultiplier(Player player, String type) {
        double base = getMultiplier(player, type);
        double prestige = getPrestigeMultiplier(player, type);
        double rebirth = getRebirthMultiplier(player, type);
        double universal = getUniversalMultiplier(player, type);
        
        return base * prestige * rebirth * universal;
    }

    public double getEnchantMultiplier(Player player) {
        return getMultiplier(player, "enchant");
    }

    private double getUniversalMultiplier(Player player, String type) {
        // Universal multiplier only applies to currency types
        if (isCurrencyType(type)) {
            return getMultiplier(player, "universal");
        }
        return 1.0;
    }

    private boolean isCurrencyType(String type) {
        return switch (type.toLowerCase()) {
            case "money", "tokens", "souls", "beacons", "experience", "gems" -> true;
            default -> false;
        };
    }

    private double getPrestigeMultiplier(Player player, String type) {
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        int prestige = rankingManager.getPrestige(player);
        
        return switch (type.toLowerCase()) {
            case "money" -> 1.0 + (prestige * 0.1); // 10% per prestige
            case "tokens" -> 1.0 + (prestige * 0.05); // 5% per prestige
            case "souls" -> 1.0 + (prestige * 0.02); // 2% per prestige
            default -> 1.0;
        };
    }

    private double getRebirthMultiplier(Player player, String type) {
        // Placeholder for rebirth system
        return 1.0;
    }

    public Map<String, Double> getAllMultipliers(Player player) {
        Map<String, Double> result = new HashMap<>();
        String uuid = player.getUuid().toString();
        
        // Common multiplier types
        String[] types = {"money", "tokens", "souls", "beacons", "experience"};
        
        for (String type : types) {
            double multiplier = getTotalMultiplier(player, type);
            if (multiplier > 1.0) {
                result.put(type, multiplier);
            }
        }
        
        return result;
    }

    // Add method to load multipliers from database
    public void loadMultipliersFromDatabase(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                Profile profile = Profile.loadAsync(player.getUuid().toString()).join();
                if (profile != null) {
                    String playerUUID = player.getUuid().toString();
                
                    // Load multipliers
                    Map<String, Double> dbMultipliers = profile.getMultipliers();
                    if (dbMultipliers != null && !dbMultipliers.isEmpty()) {
                        playerMultipliers.put(playerUUID, new HashMap<>(dbMultipliers));
                    }
                
                    // Load expiry times
                    Map<String, Long> dbExpiry = profile.getMultiplierExpiry();
                    if (dbExpiry != null && !dbExpiry.isEmpty()) {
                        multiplierExpiry.put(playerUUID, new HashMap<>(dbExpiry));
                    }
                
                    // Clean expired multipliers
                    cleanExpiredMultipliers(player);
                }
            } catch (Exception e) {
                System.err.println("[MultiplierManager] Error loading multipliers for " + player.getUsername() + ": " + e.getMessage());
            }
        });
    }

    // Add method to save multipliers to database
    private void saveMultipliersToDatabase(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                Profile profile = Profile.loadAsync(player.getUuid().toString()).join();
                if (profile != null) {
                    String playerUUID = player.getUuid().toString();
                
                    // Save multipliers
                    Map<String, Double> multipliers = playerMultipliers.get(playerUUID);
                    if (multipliers != null) {
                        profile.setMultipliers(multipliers);
                    }
                
                    // Save expiry times
                    Map<String, Long> expiry = multiplierExpiry.get(playerUUID);
                    if (expiry != null) {
                        profile.setMultiplierExpiry(expiry);
                    }
                
                    // Save to database
                    profile.saveAsync();
                }
            } catch (Exception e) {
                System.err.println("[MultiplierManager] Error saving multipliers for " + player.getUsername() + ": " + e.getMessage());
            }
        });
    }

    // Update the initializePlayer method
    public void initializePlayer(Player player) {
        String playerUUID = player.getUuid().toString();
    
        // Initialize in-memory maps if not present
        playerMultipliers.putIfAbsent(playerUUID, new HashMap<>());
        multiplierExpiry.putIfAbsent(playerUUID, new HashMap<>());
    
        // Load from database
        loadMultipliersFromDatabase(player);
    }

    // Add this method to clean expired multipliers
    private void cleanExpiredMultipliers(Player player) {
        try {
            String playerUUID = player.getUuid().toString();
        
            Map<String, Double> multipliers = playerMultipliers.get(playerUUID);
            Map<String, Long> expiry = multiplierExpiry.get(playerUUID);
        
            if (multipliers != null && expiry != null) {
                long currentTime = System.currentTimeMillis();
            
                // Find expired multipliers
                var expiredKeys = expiry.entrySet().stream()
                    .filter(entry -> entry.getValue() < currentTime)
                    .map(Map.Entry::getKey)
                    .toList();
            
                // Remove expired multipliers
                for (String key : expiredKeys) {
                    multipliers.remove(key);
                    expiry.remove(key);
                }
            
                // Save changes if any multipliers were removed
                if (!expiredKeys.isEmpty()) {
                    saveMultipliersToDatabase(player);
                }
            }
        } catch (Exception e) {
            System.err.println("[MultiplierManager] Error cleaning expired multipliers for " + player.getUsername() + ": " + e.getMessage());
        }
    }
}