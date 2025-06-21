package mythic.prison.data.perks;

public enum PerkRarity {
    NORMAL("§f", 60.0),
    RARE("§9", 25.0),
    ELITE("§5", 10.0),
    LEGENDARY("§6", 3.5),
    MYTHICAL("§d", 1.0),
    GODLY("§c", 0.4),
    ULTIMATE("§4", 0.1);

    private final String color;
    private final double chance;

    PerkRarity(String color, double chance) {
        this.color = color;
        this.chance = chance;
    }

    public String getColor() { return color; }
    public double getChance() { return chance; }
}