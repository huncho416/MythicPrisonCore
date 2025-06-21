package mythic.prison.data.backpack;

public class AutoSellSettings {
    private boolean enabled;
    private int intervalSeconds;
    private double multiplier;
    private int level;

    public AutoSellSettings() {
        this.enabled = false;
        this.intervalSeconds = 60; // Default 60 seconds
        this.multiplier = 1.0;
        this.level = 1;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }

    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public double getUpgradeCost() {
        return 1000000 * Math.pow(2, level - 1); // Exponential cost
    }

    public double getNextMultiplier() {
        return 1.0 + (level * 0.1); // 10% increase per level
    }
}