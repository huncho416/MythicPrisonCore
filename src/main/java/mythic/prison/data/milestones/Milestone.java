package mythic.prison.data.milestones;

import java.util.HashMap;
import java.util.Map;

public class Milestone {
    private final String id;
    private final String name;
    private final String description;
    private final MilestoneType type;
    private final long targetValue;
    private final Map<String, Double> rewards; // Changed from single reward to multiple rewards
    private final int order;

    public Milestone(String id, String name, String description, MilestoneType type, long targetValue, Map<String, Double> rewards, int order) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetValue = targetValue;
        this.rewards = new HashMap<>(rewards);
        this.order = order;
    }

    // Constructor for backward compatibility with single reward
    public Milestone(String id, String name, String description, MilestoneType type, long targetValue, MilestoneReward reward, int order) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetValue = targetValue;
        this.rewards = new HashMap<>();
        this.order = order;
        
        // Convert single reward to rewards map
        if (reward != null) {
            String rewardKey = reward.getType().name().toLowerCase();
            this.rewards.put(rewardKey, reward.getAmount());
            
            // Handle item rewards
            if (reward.getType() == MilestoneReward.RewardType.ITEM && reward.getItem() != null) {
                this.rewards.put("item_" + reward.getItem(), 1.0);
            }
        }
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public MilestoneType getType() { return type; }
    public long getTargetValue() { return targetValue; }
    
    public Map<String, Double> getRewards() { return new HashMap<>(rewards); }
    
    // Backward compatibility - returns the first reward as MilestoneReward
    public MilestoneReward getReward() {
        if (rewards.isEmpty()) {
            return null;
        }
        
        Map.Entry<String, Double> firstEntry = rewards.entrySet().iterator().next();
        String rewardType = firstEntry.getKey();
        Double amount = firstEntry.getValue();
        
        // Handle item rewards
        if (rewardType.startsWith("item_")) {
            String itemName = rewardType.substring(5);
            return new MilestoneReward(MilestoneReward.RewardType.ITEM, amount, itemName);
        }
        
        // Handle other reward types
        try {
            MilestoneReward.RewardType type = MilestoneReward.RewardType.valueOf(rewardType.toUpperCase());
            return new MilestoneReward(type, amount);
        } catch (IllegalArgumentException e) {
            // Default to money if unknown type
            return new MilestoneReward(MilestoneReward.RewardType.MONEY, amount);
        }
    }
    
    public int getOrder() { return order; }

    // Helper methods for adding rewards
    public void addReward(String type, double amount) {
        rewards.put(type, amount);
    }
    
    public void addMoneyReward(double amount) {
        rewards.put("money", amount);
    }
    
    public void addTokenReward(double amount) {
        rewards.put("tokens", amount);
    }
    
    public void addSoulReward(double amount) {
        rewards.put("souls", amount);
    }
    
    public void addItemReward(String itemName, double amount) {
        rewards.put("item_" + itemName, amount);
    }

    public enum MilestoneType {
        BLOCKS_MINED, MONEY_EARNED, TOKENS_EARNED, RANK_REACHED, PRESTIGE_REACHED
    }
}