package mythic.prison.data.backpack;

import java.util.HashMap;
import java.util.Map;

public class Backpack {
    private final String playerUUID;
    private int maxVolume;
    private int currentVolume;
    private double sellMultiplier;
    private boolean autoSellEnabled;
    private int autoSellInterval;
    private final Map<String, Integer> blocks;

    public Backpack(String playerUUID) {
        this.playerUUID = playerUUID;
        this.maxVolume = 1000; // Default capacity
        this.currentVolume = 0;
        this.sellMultiplier = 1.0;
        this.autoSellEnabled = false;
        this.autoSellInterval = 60; // 60 seconds
        this.blocks = new HashMap<>();
    }

    public void addBlock(String blockType, int amount) {
        if (currentVolume + amount <= maxVolume) {
            blocks.put(blockType, blocks.getOrDefault(blockType, 0) + amount);
            currentVolume += amount;
        }
    }

    public boolean removeBlock(String blockType, int amount) {
        int current = blocks.getOrDefault(blockType, 0);
        if (current >= amount) {
            if (current == amount) {
                blocks.remove(blockType);
            } else {
                blocks.put(blockType, current - amount);
            }
            currentVolume -= amount;
            return true;
        }
        return false;
    }

    public void clear() {
        blocks.clear();
        currentVolume = 0;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public boolean isFull() {
        return currentVolume >= maxVolume;
    }

    public int getAvailableSpace() {
        return maxVolume - currentVolume;
    }

    // Getters and setters
    public String getPlayerUUID() { return playerUUID; }
    public int getMaxVolume() { return maxVolume; }
    public void setMaxVolume(int maxVolume) { this.maxVolume = maxVolume; }
    public int getCurrentVolume() { return currentVolume; }
    public void setCurrentVolume(int currentVolume) { this.currentVolume = currentVolume; }
    public double getSellMultiplier() { return sellMultiplier; }
    public void setSellMultiplier(double sellMultiplier) { this.sellMultiplier = sellMultiplier; }
    public boolean isAutoSellEnabled() { return autoSellEnabled; }
    public void setAutoSellEnabled(boolean autoSellEnabled) { this.autoSellEnabled = autoSellEnabled; }
    public int getAutoSellInterval() { return autoSellInterval; }
    public void setAutoSellInterval(int autoSellInterval) { this.autoSellInterval = autoSellInterval; }
    public Map<String, Integer> getBlocks() { return new HashMap<>(blocks); }
}