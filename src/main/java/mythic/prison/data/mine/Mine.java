
package mythic.prison.data.mine;

import java.util.HashMap;
import java.util.Map;

public class Mine {

    private String id;
    private String name;
    private String displayName;
    private Object instance; // Generic object instead of Instance
    private Map<String, Double> blockComposition;
    private boolean isRegenerating;
    private long lastRegenTime;
    private int totalBlocks;
    private int blocksBroken;
    private double regenPercentage;

    public Mine(String id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.blockComposition = new HashMap<>();
        this.isRegenerating = false;
        this.lastRegenTime = System.currentTimeMillis();
        this.totalBlocks = 0;
        this.blocksBroken = 0;
        this.regenPercentage = 80.0; // Default 80% threshold for regen

        initializeDefaultComposition();
    }

    private void initializeDefaultComposition() {
        // Default block composition for mines
        switch (id.toLowerCase()) {
            case "a" -> {
                blockComposition.put("STONE", 70.0);
                blockComposition.put("COAL_ORE", 25.0);
                blockComposition.put("IRON_ORE", 5.0);
            }
            case "b" -> {
                blockComposition.put("STONE", 60.0);
                blockComposition.put("COAL_ORE", 20.0);
                blockComposition.put("IRON_ORE", 15.0);
                blockComposition.put("GOLD_ORE", 5.0);
            }
            case "c" -> {
                blockComposition.put("STONE", 50.0);
                blockComposition.put("IRON_ORE", 25.0);
                blockComposition.put("GOLD_ORE", 15.0);
                blockComposition.put("DIAMOND_ORE", 8.0);
                blockComposition.put("EMERALD_ORE", 2.0);
            }
            default -> {
                blockComposition.put("STONE", 80.0);
                blockComposition.put("COAL_ORE", 20.0);
            }
        }
    }

    public boolean needsRegeneration() {
        if (totalBlocks == 0) return false;
        double percentageBroken = (double) blocksBroken / totalBlocks * 100;
        return percentageBroken >= regenPercentage;
    }

    public void regenerate() {
        if (isRegenerating) return;

        isRegenerating = true;
        blocksBroken = 0;
        lastRegenTime = System.currentTimeMillis();

        // In a real implementation, this would regenerate the mine blocks
        // For now, we just reset the counters

        // Simulate regeneration time
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 5 second regen time
                isRegenerating = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void onBlockBreak(String blockType) {
        blocksBroken++;

        // Check if regeneration is needed
        if (needsRegeneration() && !isRegenerating) {
            regenerate();
        }
    }

    public String getRandomBlockType() {
        double random = Math.random() * 100;
        double cumulative = 0;

        for (Map.Entry<String, Double> entry : blockComposition.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }

        // Fallback to stone
        return "STONE";
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Map<String, Double> getBlockComposition() {
        return blockComposition;
    }

    public void setBlockComposition(Map<String, Double> blockComposition) {
        this.blockComposition = blockComposition;
    }

    public boolean isRegenerating() {
        return isRegenerating;
    }

    public void setRegenerating(boolean regenerating) {
        isRegenerating = regenerating;
    }

    public long getLastRegenTime() {
        return lastRegenTime;
    }

    public void setLastRegenTime(long lastRegenTime) {
        this.lastRegenTime = lastRegenTime;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public double getRegenPercentage() {
        return regenPercentage;
    }

    public void setRegenPercentage(double regenPercentage) {
        this.regenPercentage = regenPercentage;
    }

    public double getRegenerationProgress() {
        if (totalBlocks == 0) return 0;
        return Math.min(100.0, (double) blocksBroken / totalBlocks * 100);
    }

    public long getTimeSinceLastRegen() {
        return System.currentTimeMillis() - lastRegenTime;
    }
}