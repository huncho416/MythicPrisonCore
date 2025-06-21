package mythic.prison.data.pets;

public class PetType {
    private final String name;
    private final String emoji;
    private final PetRarity rarity;
    private final double baseMultiplier;
    private final double maxMultiplier;
    private final int maxLevel;
    private final BoostType boostType;

    public PetType(String name, String emoji, PetRarity rarity, double baseMultiplier, double maxMultiplier, int maxLevel, BoostType boostType) {
        this.name = name;
        this.emoji = emoji;
        this.rarity = rarity;
        this.baseMultiplier = baseMultiplier;
        this.maxMultiplier = maxMultiplier;
        this.maxLevel = maxLevel;
        this.boostType = boostType;
    }

    // Getters
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public PetRarity getRarity() { return rarity; }
    public double getBaseMultiplier() { return baseMultiplier; }
    public double getMaxMultiplier() { return maxMultiplier; }
    public int getMaxLevel() { return maxLevel; }
    public BoostType getBoostType() { return boostType; }

    public enum PetRarity {
        COMMON("§f", "Common", 25.0),
        UNCOMMON("§a", "Uncommon", 20.0),
        RARE("§9", "Rare", 15.0),
        EPIC("§5", "Epic", 8.0),
        LEGENDARY("§6", "Legendary", 3.0),
        MYTHIC("§c", "Mythic", 1.0);

        private final String color;
        private final String displayName;
        private final double baseChance;

        PetRarity(String color, String displayName, double baseChance) {
            this.color = color;
            this.displayName = displayName;
            this.baseChance = baseChance;
        }

        public String getColor() { return color; }
        public String getDisplayName() { return displayName; }
        public double getBaseChance() { return baseChance; }
    }

    public enum BoostType {
        MONEY, TOKENS, EXP, LUCK
    }
}