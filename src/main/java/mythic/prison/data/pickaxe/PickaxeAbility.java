package mythic.prison.data.pickaxe;

public class PickaxeAbility {
    private String id;
    private String name;
    private String description;
    private int cooldownSeconds;
    private int durationSeconds;
    private double multiplier;
    private AbilityType type;
    private int maxLevel;
    
    public enum AbilityType {
        UNIVERSAL_BOOST("Universal Booster", "Boosts all multipliers"),
        TOKEN_RAIN("Token Rain", "Spawns tokens around you"),
        SOUL_HARVEST("Soul Harvest", "Rapidly generates souls"),
        MINE_STORM("Mine Storm", "Breaks multiple blocks at once"),
        FORTUNE_FINDER("Fortune Finder", "Increases rare item chances");
        
        private final String displayName;
        private final String description;
        
        AbilityType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public PickaxeAbility(String id, String name, String description, AbilityType type,
                          int cooldownSeconds, int durationSeconds, double multiplier, int maxLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.cooldownSeconds = cooldownSeconds;
        this.durationSeconds = durationSeconds;
        this.multiplier = multiplier;
        this.maxLevel = maxLevel;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AbilityType getType() { return type; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public int getDurationSeconds() { return durationSeconds; }
    public double getMultiplier() { return multiplier; }
    public int getMaxLevel() { return maxLevel; }
}