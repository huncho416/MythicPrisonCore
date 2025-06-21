package mythic.prison.data.pets;

import java.util.UUID;

public class Pet {
    private final String id;
    private final PetType petType;
    private int level;
    private long experience;
    private boolean isActive;
    private final String ownerUUID;

    public Pet(PetType petType, String ownerUUID) {
        this.id = UUID.randomUUID().toString();
        this.petType = petType;
        this.level = 1;
        this.experience = 0;
        this.isActive = false;
        this.ownerUUID = ownerUUID;
    }

    public Pet(String id, PetType petType, int level, long experience, String ownerUUID) {
        this.id = id;
        this.petType = petType;
        this.level = level;
        this.experience = experience;
        this.isActive = false;
        this.ownerUUID = ownerUUID;
    }

    public double getCurrentMultiplier() {
        double baseMultiplier = petType.getBaseMultiplier();
        double maxMultiplier = petType.getMaxMultiplier();
        double progression = (double) (level - 1) / (petType.getMaxLevel() - 1);
        return baseMultiplier + (maxMultiplier - baseMultiplier) * progression;
    }

    public String getDisplayName() {
        return petType.getRarity().getColor() + petType.getName() + " " + petType.getEmoji() + " §7(Lv." + level + ")";
    }

    public long getExpRequiredForNextLevel() {
        return (long) (100 * Math.pow(1.2, level - 1));
    }

    public boolean canLevelUp() {
        return level < petType.getMaxLevel() && experience >= getExpRequiredForNextLevel();
    }

    public void levelUp() {
        if (canLevelUp()) {
            experience -= getExpRequiredForNextLevel();
            level++;
        }
    }

    public void addExperience(long exp) {
        this.experience += exp;
        while (canLevelUp()) {
            levelUp();
        }
    }

    public String getBoostType() {
        return switch (petType.getBoostType()) {
            case MONEY -> "Money";
            case TOKENS -> "Tokens";
            case EXP -> "Experience";
            case LUCK -> "Luck";
            default -> "Unknown";
        };
    }

    // Getters and setters
    public String getId() { return id; }
    public PetType getPetType() { return petType; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getExperience() { return experience; }
    public void setExperience(long experience) { this.experience = experience; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getOwnerUUID() { return ownerUUID; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pet pet = (Pet) obj;
        return id.equals(pet.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}