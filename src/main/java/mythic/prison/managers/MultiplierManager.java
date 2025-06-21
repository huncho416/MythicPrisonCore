package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public void setMultiplier(Player player, String type, double multiplier, long durationMs) {
        String uuid = player.getUuid().toString();
        
        // Update PlayerProfile
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        if (profile != null) {
            profile.setMultiplier(type, multiplier, durationMs);
        }
        
        // Update local storage
        playerMultipliers.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(type, multiplier);
        
        if (durationMs > 0) {
            multiplierExpiry.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(type, System.currentTimeMillis() + durationMs);
        } else {
            Map<String, Long> expiry = multiplierExpiry.get(uuid);
            if (expiry != null) {
                expiry.remove(type);
            }
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
        String uuid = player.getUuid().toString();
        
        // Update PlayerProfile
        ProfileManager profileManager = MythicPrison.getInstance().getProfileManager();
        PlayerProfile profile = profileManager.getProfile(player);
        if (profile != null) {
            profile.removeMultiplier(type);
        }
        
        // Update local storage
        Map<String, Double> multipliers = playerMultipliers.get(uuid);
        if (multipliers != null) {
            multipliers.remove(type);
        }
        
        Map<String, Long> expiry = multiplierExpiry.get(uuid);
        if (expiry != null) {
            expiry.remove(type);
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
}