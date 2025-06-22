package mythic.prison.data.enchants;

public abstract class PickaxeEnchant {
    protected final String name;
    protected final String id;
    protected final int maxLevel;
    protected final double baseCost;
    protected final double costMultiplier;
    protected final String description;
    
    public PickaxeEnchant(String name, String id, int maxLevel, double baseCost, double costMultiplier, String description) {
        this.name = name;
        this.id = id;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.description = description;
    }
    
    public String getName() { return name; }
    public String getId() { return id; }
    public int getMaxLevel() { return maxLevel; }
    public double getBaseCost() { return baseCost; }
    public double getCostMultiplier() { return costMultiplier; }
    public String getDescription() { return description; }
    
    // Add the missing getCostForLevel method
    public double getCostForLevel(int level) {
        return baseCost * Math.pow(costMultiplier, level - 1);
    }
    
    public abstract String getCurrencyType();
}