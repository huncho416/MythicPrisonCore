package mythic.prison.data.pickaxe;

public class PickaxeEnchant {
    private final String name;
    private final String description;
    private final int maxLevel;
    private final double baseCost;
    private final String currency;
    private final EnchantType type;

    public PickaxeEnchant(String name, String description, int maxLevel, double baseCost, String currency, EnchantType type) {
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.currency = currency;
        this.type = type;
    }

    public double getCostForLevel(int level) {
        return baseCost * Math.pow(1.5, level - 1);
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMaxLevel() { return maxLevel; }
    public double getBaseCost() { return baseCost; }
    public String getCurrency() { return currency; }
    public EnchantType getType() { return type; }

    public enum EnchantType {
        EFFICIENCY, FORTUNE, EXPLOSION, SPECIAL
    }
}