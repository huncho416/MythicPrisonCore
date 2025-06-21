package mythic.prison.data.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerProfile {
    private final String uuid;
    private String username;
    private long lastSeen;

    // Currencies
    private final Map<String, Double> currencies;

    // Progression
    private String currentRank;
    private int prestige;
    private int rebirth;
    private int ascension = 0;
    private long totalPlaytime;

    // Stats
    private long blocksMined;
    private long monstersKilled;
    private double totalMoneyEarned;
    private long commandsUsed;
    private long deathCount;

    // Multipliers
    private final Map<String, Double> multipliers;
    private final Map<String, Long> multiplierExpiry;

    // Settings
    private boolean autoSellEnabled;
    private boolean pvpEnabled;
    private boolean chatEnabled;
    private String language;

    // Social
    private String gangId;
    private String gangRank;

    // Special data
    private final Map<String, Object> customData;

    // Add these fields
    private boolean autoPrestigeEnabled = false;
    private boolean autoRebirthEnabled = false;

    public PlayerProfile(String uuid) {
        this.uuid = uuid;
        this.currencies = new HashMap<>();
        this.multipliers = new HashMap<>();
        this.multiplierExpiry = new HashMap<>();
        this.customData = new HashMap<>();
        this.lastSeen = System.currentTimeMillis();

        // Initialize default values
        this.currentRank = "A";
        this.prestige = 0;
        this.rebirth = 0;
        this.totalPlaytime = 0;
        this.blocksMined = 0;
        this.monstersKilled = 0;
        this.totalMoneyEarned = 0;
        this.commandsUsed = 0;
        this.deathCount = 0;
        this.autoSellEnabled = false;
        this.pvpEnabled = true;
        this.chatEnabled = true;
        this.language = "en";

        // Initialize default currencies
        this.currencies.put("money", 0.0);
        this.currencies.put("tokens", 0.0);
        this.currencies.put("souls", 0.0);
        this.currencies.put("beacons", 0.0);
        this.currencies.put("gems", 0.0);

        // Initialize default multipliers (1.0x for all currency types)
        initializeDefaultMultipliers();
    }

    private void initializeDefaultMultipliers() {
        String[] defaultTypes = {"money", "tokens", "souls", "beacons", "experience", "gems"};
        for (String type : defaultTypes) {
            multipliers.put(type, 1.0);
        }
    }

    // Currency methods
    public double getBalance(String currency) {
        return currencies.getOrDefault(currency, 0.0);
    }

    public void setBalance(String currency, double amount) {
        currencies.put(currency, Math.max(0, amount));
    }

    public boolean addBalance(String currency, double amount) {
        if (amount < 0) return false;
        double current = getBalance(currency);
        setBalance(currency, current + amount);
        return true;
    }

    public boolean removeBalance(String currency, double amount) {
        if (amount < 0) return false;
        double current = getBalance(currency);
        if (current < amount) return false;
        setBalance(currency, current - amount);
        return true;
    }

    // Multiplier methods (consolidated and improved)
    public double getMultiplier(String type) {
        // Clean up expired multipliers first
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

    // Custom data methods
    public void setCustomData(String key, Object value) {
        customData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, Class<T> type) {
        Object value = customData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public Object getCustomData(String key) {
        return customData.get(key);
    }

    public void removeCustomData(String key) {
        customData.remove(key);
    }

    // Getters and Setters
    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public Map<String, Double> getCurrencies() { return new HashMap<>(currencies); }

    public String getCurrentRank() { return currentRank; }
    public void setCurrentRank(String currentRank) { this.currentRank = currentRank; }
    public int getPrestige() { return prestige; }
    public void setPrestige(int prestige) { this.prestige = prestige; }
    public int getRebirth() { return rebirth; }
    public void setRebirth(int rebirth) { this.rebirth = rebirth; }
    public int getAscension() {
        return ascension;
    }

    public void setAscension(int ascension) {
        this.ascension = Math.max(0, ascension);
    }
    public long getTotalPlaytime() { return totalPlaytime; }
    public void setTotalPlaytime(long totalPlaytime) { this.totalPlaytime = totalPlaytime; }
    public void addPlaytime(long playtime) { this.totalPlaytime += playtime; }

    public long getBlocksMined() { return blocksMined; }
    public void setBlocksMined(long blocksMined) { this.blocksMined = blocksMined; }
    public void addBlocksMined(long blocks) { this.blocksMined += blocks; }
    public long getMonstersKilled() { return monstersKilled; }
    public void setMonstersKilled(long monstersKilled) { this.monstersKilled = monstersKilled; }
    public void addMonstersKilled(long monsters) { this.monstersKilled += monsters; }
    public double getTotalMoneyEarned() { return totalMoneyEarned; }
    public void setTotalMoneyEarned(double totalMoneyEarned) { this.totalMoneyEarned = totalMoneyEarned; }
    public void addMoneyEarned(double money) { this.totalMoneyEarned += money; }
    public long getCommandsUsed() { return commandsUsed; }
    public void setCommandsUsed(long commandsUsed) { this.commandsUsed = commandsUsed; }
    public void addCommandUsed() { this.commandsUsed++; }
    public long getDeathCount() { return deathCount; }
    public void setDeathCount(long deathCount) { this.deathCount = deathCount; }
    public void addDeath() { this.deathCount++; }

    public Map<String, Double> getMultipliers() { return new HashMap<>(multipliers); }
    public Map<String, Long> getMultiplierExpiry() { return new HashMap<>(multiplierExpiry); }

    public boolean isAutoSellEnabled() { return autoSellEnabled; }
    public void setAutoSellEnabled(boolean autoSellEnabled) { this.autoSellEnabled = autoSellEnabled; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }
    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getGangId() { return gangId; }
    public void setGangId(String gangId) { this.gangId = gangId; }
    public String getGangRank() { return gangRank; }
    public void setGangRank(String gangRank) { this.gangRank = gangRank; }

    public Map<String, Object> getCustomData() { return new HashMap<>(customData); }

    // Add these getter/setter methods
    public boolean isAutoPrestigeEnabled() { return autoPrestigeEnabled; }
    public void setAutoPrestigeEnabled(boolean enabled) { this.autoPrestigeEnabled = enabled; }

    public boolean isAutoRebirthEnabled() { return autoRebirthEnabled; }
    public void setAutoRebirthEnabled(boolean enabled) { this.autoRebirthEnabled = enabled; }
}