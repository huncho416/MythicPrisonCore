package mythic.prison.data.milestones;

import java.util.HashSet;
import java.util.Set;

public class PlayerMilestones {
    private final String playerUUID;
    private final Set<String> completedMilestones;

    public PlayerMilestones(String playerUUID) {
        this.playerUUID = playerUUID;
        this.completedMilestones = new HashSet<>();
    }

    public void completeMilestone(String milestoneId) {
        completedMilestones.add(milestoneId);
    }

    public boolean isCompleted(String milestoneId) {
        return completedMilestones.contains(milestoneId);
    }

    public Set<String> getCompletedMilestones() {
        return new HashSet<>(completedMilestones);
    }

    public String getPlayerUUID() {
        return playerUUID;
    }
}