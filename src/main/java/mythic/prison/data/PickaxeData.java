package mythic.prison.data;

import java.util.HashMap;
import java.util.Map;

public class PickaxeData {
    private int level;
    private long experience;  // ✅ Changed from int to long
    private Map<String, Integer> enchants;

    public PickaxeData() {
        this.level = 1;
        this.experience = 0L;  // ✅ Explicitly set as long
        this.enchants = new HashMap<>();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(100, level));
    }

    public long getExperience() {  // ✅ Changed from int to long
        return experience;
    }

    public void setExperience(long experience) {  // ✅ Changed from int to long
        this.experience = Math.max(0L, experience);
    }

    public Map<String, Integer> getEnchants() {
        return enchants;
    }

    public int getEnchantLevel(String enchantName) {
        return enchants.getOrDefault(enchantName.toLowerCase(), 0);
    }

    public void setEnchantLevel(String enchantName, int level) {
        if (level <= 0) {
            enchants.remove(enchantName.toLowerCase());
        } else {
            enchants.put(enchantName.toLowerCase(), level);
        }
    }

    public void addEnchantLevel(String enchantName, int levels) {
        int currentLevel = getEnchantLevel(enchantName);
        setEnchantLevel(enchantName, currentLevel + levels);
    }

    public boolean hasEnchant(String enchantName) {
        return getEnchantLevel(enchantName) > 0;
    }
}