package mythic.prison.data.enchants;

import mythic.prison.data.enchants.PickaxeEnchant.EnchantType;

public class SoulEnchant extends PickaxeEnchant {

    private final String id;

    public SoulEnchant(String displayName, String id, int maxLevel, double baseCost, double costMultiplier) {
        super(displayName, getDescription(id, 1), maxLevel, baseCost, "souls", EnchantType.SPECIAL);
        this.id = id;
    }

    public String getDescription(int level) {
        return getDescription(this.id, level);
    }

    private static String getDescription(String id, int level) {
        return switch (id.toLowerCase()) {
            case "soul_reaper" -> "§7Gain §5" + (level * 0.5) + " souls §7per block mined";
            case "soul_multiplier" -> "§7Multiply soul gains by §5" + (level * 1) + "%";
            case "soul_boost" -> "§7" + (level * 5) + "% §7chance for §5soul boost";
            default -> "§7Unknown soul enchant effect";
        };
    }
}