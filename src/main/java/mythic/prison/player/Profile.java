// src/main/java/mythic/prison/player/Profile.java
package mythic.prison.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mythic.prison.MythicPrison;
import mythic.prison.data.backpack.Backpack;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mythic.prison.player.Profile;
import java.util.HashSet;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Profile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String uuid;
    private String username;
    private List<String> permissions;
    private List<String> ranks;
    private Map<String, Double> currencies;
    private Map<String, Object> stats;
    private long lastSeen;

    // Add these new fields to the Profile class
    private String currentRank;
    private int prestige;
    private int rebirth;
    private int ascension;

    // Pickaxe data
    private long pickaxeExp;
    private int pickaxeLevel;
    private Map<String, Integer> pickaxeEnchants;

    // Backpack data
    private Map<String, Object> backpackData;

    // Milestone data
    private Set<String> completedMilestones;
    private Map<String, Long> milestoneProgress;

    // Add these fields after the existing fields
    private Map<String, Double> multipliers;
    private Map<String, Long> multiplierExpiry;

    // Constructor updates
    public Profile(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.permissions = new ArrayList<>();
        this.ranks = new ArrayList<>();
        this.currencies = new HashMap<>();
        this.stats = new HashMap<>();
        this.lastSeen = System.currentTimeMillis();
        
        // Initialize new fields
        this.currentRank = "A";
        this.prestige = 0;
        this.rebirth = 0;
        this.ascension = 0;
        this.pickaxeExp = 0;
        this.pickaxeLevel = 1;
        this.pickaxeEnchants = new HashMap<>();
        this.backpackData = new HashMap<>();
        this.completedMilestones = new HashSet<>();
        this.milestoneProgress = new HashMap<>();
        
        // Initialize multipliers
        this.multipliers = new HashMap<>();
        this.multiplierExpiry = new HashMap<>();
        
        initializeDefaultCurrencies();
        initializeDefaultBackpack();
        initializeDefaultMultipliers();
    }

    // Add this new initialization method
    private void initializeDefaultMultipliers() {
        String[] defaultTypes = {"money", "tokens", "souls", "beacons", "experience", "gems", "universal", "enchant"};
        for (String type : defaultTypes) {
            multipliers.put(type, 1.0);
        }
    }

    private void initializeDefaultCurrencies() {
        currencies.put("money", 0.0);
        currencies.put("rubies", 0.0);
        currencies.put("essence", 0.0);
        currencies.put("coins", 0.0);
        currencies.put("tokens", 0.0);
        currencies.put("shards", 0.0);
        currencies.put("credits", 0.0);
        currencies.put("souls", 0.0);
        currencies.put("beacons", 0.0);
        currencies.put("trophies", 0.0);
    }

    private void initializeDefaultBackpack() {
        backpackData.put("maxVolume", 1000);
        backpackData.put("currentVolume", 0);
        backpackData.put("sellMultiplier", 1.0);
        backpackData.put("autoSellEnabled", false);
        backpackData.put("autoSellInterval", 60);
        backpackData.put("blocks", new HashMap<String, Integer>());
    }

    // Getters and Setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public List<String> getRanks() { return ranks; }
    public void setRanks(List<String> ranks) { this.ranks = ranks; }

    public Map<String, Double> getCurrencies() { return currencies; }
    public void setCurrencies(Map<String, Double> currencies) { this.currencies = currencies; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    // Currency methods
    public double getCurrency(String currency) {
        return currencies.getOrDefault(currency, 0.0);
    }

    public void setCurrency(String currency, double amount) {
        currencies.put(currency, amount);
    }

    public void addCurrency(String currency, double amount) {
        currencies.put(currency, getCurrency(currency) + amount);
    }

    public boolean removeCurrency(String currency, double amount) {
        double current = getCurrency(currency);
        if (current >= amount) {
            currencies.put(currency, current - amount);
            return true;
        }
        return false;
    }

    // Permission methods
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    // Rank methods
    public void addRank(String rank) {
        if (!ranks.contains(rank)) {
            ranks.add(rank);
        }
    }

    public void removeRank(String rank) {
        ranks.remove(rank);
    }

    // Rank progression methods
    public String getCurrentRank() { return currentRank; }
    public void setCurrentRank(String currentRank) { this.currentRank = currentRank; }

    public int getPrestige() { return prestige; }
    public void setPrestige(int prestige) { this.prestige = prestige; }

    public int getRebirth() { return rebirth; }
    public void setRebirth(int rebirth) { this.rebirth = rebirth; }

    public int getAscension() { return ascension; }
    public void setAscension(int ascension) { this.ascension = ascension; }

    // Pickaxe methods
    public long getPickaxeExp() { return pickaxeExp; }
    public void setPickaxeExp(long pickaxeExp) { this.pickaxeExp = pickaxeExp; }
    public void addPickaxeExp(long exp) { this.pickaxeExp += exp; }

    public int getPickaxeLevel() { return pickaxeLevel; }
    public void setPickaxeLevel(int pickaxeLevel) { this.pickaxeLevel = pickaxeLevel; }

    public Map<String, Integer> getPickaxeEnchants() { return new HashMap<>(pickaxeEnchants); }
    public void setPickaxeEnchants(Map<String, Integer> enchants) { this.pickaxeEnchants = new HashMap<>(enchants); }

    public int getPickaxeEnchantLevel(String enchant) {
        return pickaxeEnchants.getOrDefault(enchant, 0);
    }

    public void setPickaxeEnchantLevel(String enchant, int level) {
        if (level <= 0) {
            pickaxeEnchants.remove(enchant);
        } else {
            pickaxeEnchants.put(enchant, level);
        }
    }

    public void upgradePickaxeEnchant(String enchant) {
        int currentLevel = getPickaxeEnchantLevel(enchant);
        setPickaxeEnchantLevel(enchant, currentLevel + 1);
    }

    // Backpack methods
    public Map<String, Object> getBackpackData() { return new HashMap<>(backpackData); }
    public void setBackpackData(Map<String, Object> backpackData) { this.backpackData = new HashMap<>(backpackData); }

    @SuppressWarnings("unchecked")
    public <T> T getBackpackValue(String key, Class<T> type) {
        Object value = backpackData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public void setBackpackValue(String key, Object value) {
        backpackData.put(key, value);
    }

    public int getBackpackMaxVolume() {
        return getBackpackValue("maxVolume", Integer.class) != null ? 
           getBackpackValue("maxVolume", Integer.class) : 1000;
    }

    public void setBackpackMaxVolume(int maxVolume) {
        setBackpackValue("maxVolume", maxVolume);
    }

    public int getBackpackCurrentVolume() {
        return getBackpackValue("currentVolume", Integer.class) != null ? 
           getBackpackValue("currentVolume", Integer.class) : 0;
    }

    public void setBackpackCurrentVolume(int currentVolume) {
        setBackpackValue("currentVolume", currentVolume);
    }

    public double getBackpackSellMultiplier() {
        return getBackpackValue("sellMultiplier", Double.class) != null ? 
           getBackpackValue("sellMultiplier", Double.class) : 1.0;
    }

    public void setBackpackSellMultiplier(double multiplier) {
        setBackpackValue("sellMultiplier", multiplier);
    }

    public boolean isBackpackAutoSellEnabled() {
        return getBackpackValue("autoSellEnabled", Boolean.class) != null ? 
           getBackpackValue("autoSellEnabled", Boolean.class) : false;
    }

    public void setBackpackAutoSellEnabled(boolean enabled) {
        setBackpackValue("autoSellEnabled", enabled);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getBackpackBlocks() {
        Object blocks = backpackData.get("blocks");
        if (blocks instanceof Map) {
            return new HashMap<>((Map<String, Integer>) blocks);
        }
        return new HashMap<>();
    }

    public void setBackpackBlocks(Map<String, Integer> blocks) {
        setBackpackValue("blocks", new HashMap<>(blocks));
    }

    // Milestone methods
    public Set<String> getCompletedMilestones() { return new HashSet<>(completedMilestones); }
    public void setCompletedMilestones(Set<String> milestones) { this.completedMilestones = new HashSet<>(milestones); }

    public boolean hasMilestoneCompleted(String milestoneId) {
        return completedMilestones.contains(milestoneId);
    }

    public void completeMilestone(String milestoneId) {
        completedMilestones.add(milestoneId);
    }

    public Map<String, Long> getMilestoneProgress() { return new HashMap<>(milestoneProgress); }
    public void setMilestoneProgress(Map<String, Long> progress) { this.milestoneProgress = new HashMap<>(progress); }

    public long getMilestoneProgress(String milestoneId) {
        return milestoneProgress.getOrDefault(milestoneId, 0L);
    }

    public void setMilestoneProgress(String milestoneId, long progress) {
        milestoneProgress.put(milestoneId, progress);
    }

    public void addMilestoneProgress(String milestoneId, long progress) {
        long currentProgress = getMilestoneProgress(milestoneId);
        setMilestoneProgress(milestoneId, currentProgress + progress);
    }

    // Add multiplier methods
    public Map<String, Double> getMultipliers() { 
        return new HashMap<>(multipliers); 
    }

    public void setMultipliers(Map<String, Double> multipliers) { 
        this.multipliers = multipliers != null ? new HashMap<>(multipliers) : new HashMap<>(); 
    }

    public Map<String, Long> getMultiplierExpiry() { 
        return new HashMap<>(multiplierExpiry); 
    }

    public void setMultiplierExpiry(Map<String, Long> multiplierExpiry) { 
        this.multiplierExpiry = multiplierExpiry != null ? new HashMap<>(multiplierExpiry) : new HashMap<>(); 
    }

    public double getMultiplier(String type) {
        cleanExpiredMultipliers();
        return multipliers.getOrDefault(type, 1.0);
    }

    public void setMultiplier(String type, double multiplier, long durationMs) {
        multipliers.put(type, multiplier);
        if (durationMs > 0) {
            multiplierExpiry.put(type, System.currentTimeMillis() + durationMs);
        } else {
            multiplierExpiry.remove(type);
        }
    }

    public void removeMultiplier(String type) {
        multipliers.remove(type);
        multiplierExpiry.remove(type);
    }

    private void cleanExpiredMultipliers() {
        long currentTime = System.currentTimeMillis();
        multiplierExpiry.entrySet().removeIf(entry -> {
            if (currentTime > entry.getValue()) {
                multipliers.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public boolean hasActiveMultiplier(String type) {
        cleanExpiredMultipliers();
        return multipliers.containsKey(type) && multipliers.get(type) > 1.0;
    }

    public long getMultiplierTimeLeft(String type) {
        Long expiryTime = multiplierExpiry.get(type);
        if (expiryTime == null) return 0;
        
        long timeLeft = expiryTime - System.currentTimeMillis();
        return Math.max(0, timeLeft);
    }

    // Serialization
    public String toJson() {
        return GSON.toJson(this);
    }

    public static Profile fromJson(String json) {
        Type profileType = new TypeToken<Profile>(){}.getType();
        return GSON.fromJson(json, profileType);
    }

    // Database operations
    public CompletableFuture<Void> saveAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.lastSeen = System.currentTimeMillis();
                String json = this.toJson();
                MythicPrison.getInstance().getRedisManager().getSyncCommands().set("profile:" + uuid, json);
            } catch (Exception e) {
                System.err.println("Error saving profile for " + username + ": " + e.getMessage());
            }
        });
    }

    public static CompletableFuture<Profile> loadAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = MythicPrison.getInstance().getRedisManager().getSyncCommands().get("profile:" + uuid);
                if (json != null) {
                    return Profile.fromJson(json);
                }
                return null;
            } catch (Exception e) {
                System.err.println("Error loading profile for UUID " + uuid + ": " + e.getMessage());
                return null;
            }
        });
    }

    public static CompletableFuture<Profile> loadByUsernameAsync(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First try to get UUID from username mapping
                String uuid = MythicPrison.getInstance().getRedisManager().getSyncCommands().get("username:" + username.toLowerCase());
                if (uuid != null) {
                    return loadAsync(uuid).join();
                }
                return null;
            } catch (Exception e) {
                System.err.println("Error loading profile for username " + username + ": " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<Void> deleteAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                MythicPrison.getInstance().getRedisManager().getSyncCommands().del("profile:" + uuid);
                MythicPrison.getInstance().getRedisManager().getSyncCommands().del("username:" + username.toLowerCase());
            } catch (Exception e) {
                System.err.println("Error deleting profile for " + username + ": " + e.getMessage());
            }
        });
    }

    // Helper method to convert Profile to Backpack object
    public Backpack toBackpack() {
        Backpack backpack = new Backpack(this.uuid);
        backpack.setMaxVolume(getBackpackMaxVolume());
        backpack.setCurrentVolume(getBackpackCurrentVolume());
        backpack.setSellMultiplier(getBackpackSellMultiplier());
        backpack.setAutoSellEnabled(isBackpackAutoSellEnabled());
        
        // Set the blocks
        Map<String, Integer> blocks = getBackpackBlocks();
        for (Map.Entry<String, Integer> entry : blocks.entrySet()) {
            backpack.addBlock(entry.getKey(), entry.getValue());
        }
        
        return backpack;
    }

    // Helper method to update Profile from Backpack object
    public void updateFromBackpack(Backpack backpack) {
        setBackpackMaxVolume(backpack.getMaxVolume());
        setBackpackCurrentVolume(backpack.getCurrentVolume());
        setBackpackSellMultiplier(backpack.getSellMultiplier());
        setBackpackAutoSellEnabled(backpack.isAutoSellEnabled());
        setBackpackBlocks(backpack.getBlocks());
    }
}