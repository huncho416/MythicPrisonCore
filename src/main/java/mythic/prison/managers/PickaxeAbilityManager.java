package mythic.prison.managers;

import net.minestom.server.entity.Player;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PickaxeAbilityManager {

    private final Map<String, Map<String, Integer>> playerAbilities = new ConcurrentHashMap<>();

    public void initializePlayer(Player player) {
        String uuid = player.getUuid().toString();
        playerAbilities.putIfAbsent(uuid, new ConcurrentHashMap<>());
    }

    public void initializePlayer(Object player) {
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public int getAbilityLevel(Object player, String abilityId) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return 0;
        
        return playerAbilities.getOrDefault(uuid, new ConcurrentHashMap<>()).getOrDefault(abilityId, 0);
    }

    public void setAbilityLevel(Object player, String abilityId, int level) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return;
        
        if (player instanceof Player p) {
            initializePlayer(p);
        } else {
            playerAbilities.putIfAbsent(uuid, new ConcurrentHashMap<>());
        }
        
        playerAbilities.get(uuid).put(abilityId, Math.max(0, level));
    }

    public void upgradeAbility(Object player, String abilityId) {
        int currentLevel = getAbilityLevel(player, abilityId);
        setAbilityLevel(player, abilityId, currentLevel + 1);
    }

    public boolean hasAbility(Object player, String abilityId) {
        return getAbilityLevel(player, abilityId) > 0;
    }

    public Map<String, Integer> getPlayerAbilities(Object player) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return new ConcurrentHashMap<>();
        
        return new ConcurrentHashMap<>(playerAbilities.getOrDefault(uuid, new ConcurrentHashMap<>()));
    }

    private String getPlayerUUID(Object player) {
        if (player instanceof Player p) {
            return p.getUuid().toString();
        }
        return player != null ? player.toString() : null;
    }
}