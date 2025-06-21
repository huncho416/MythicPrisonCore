
package mythic.prison.data.perks;

public class PickaxePerk {

    private String name;
    private PerkRarity rarity;
    private double multiplier;
    private String boostType;
    private int tier;

    public PickaxePerk(String name, PerkRarity rarity, double multiplier, String boostType, int tier) {
        this.name = name;
        this.rarity = rarity;
        this.multiplier = multiplier;
        this.boostType = boostType;
        this.tier = tier;
    }

    public String getName() { return name; }
    public PerkRarity getRarity() { return rarity; }
    public double getMultiplier() { return multiplier; }
    public String getBoostType() { return boostType; }
    public int getTier() { return tier; }
}