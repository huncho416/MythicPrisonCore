package mythic.prison.data.pets;

public enum PetRarity {
    COMMON("§7Common", 1.0, "§7", 25.0),
    UNCOMMON("§aUncommon", 1.2, "§a", 20.0),
    RARE("§9Rare", 1.5, "§9", 15.0),
    EPIC("§5Epic", 2.0, "§5", 10.0),
    LEGENDARY("§6Legendary", 3.0, "§6", 5.0),
    MYTHIC("§cMythic", 5.0, "§c", 1.0);

    private final String displayName;
    private final double multiplierBonus;
    private final String color;
    private final double baseChance;

    PetRarity(String displayName, double multiplierBonus, String color, double baseChance) {
        this.displayName = displayName;
        this.multiplierBonus = multiplierBonus;
        this.color = color;
        this.baseChance = baseChance;
    }

    public String getDisplayName() { return displayName; }
    public double getMultiplierBonus() { return multiplierBonus; }
    public String getColor() { return color; }
    public double getBaseChance() { return baseChance; }
}