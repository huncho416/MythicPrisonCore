package mythic.prison.data.milestones;

public class MilestoneReward {
    private final RewardType type;
    private final double amount;
    private final String item;

    public MilestoneReward(RewardType type, double amount, String item) {
        this.type = type;
        this.amount = amount;
        this.item = item;
    }

    public MilestoneReward(RewardType type, double amount) {
        this(type, amount, null);
    }

    // Getters
    public RewardType getType() { return type; }
    public double getAmount() { return amount; }
    public String getItem() { return item; }

    public enum RewardType {
        MONEY, TOKENS, SOULS, ITEM, MULTIPLIER
    }
}