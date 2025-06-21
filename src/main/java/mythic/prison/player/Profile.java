// src/main/java/mythic/prison/player/Profile.java
package mythic.prison.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mythic.prison.MythicPrison;

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

    public Profile(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.permissions = new ArrayList<>();
        this.ranks = new ArrayList<>();
        this.currencies = new HashMap<>();
        this.stats = new HashMap<>();
        this.lastSeen = System.currentTimeMillis();

        // Initialize default currencies
        initializeDefaultCurrencies();
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
}