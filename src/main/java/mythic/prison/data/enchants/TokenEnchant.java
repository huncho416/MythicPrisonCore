package mythic.prison.data.enchants;

import mythic.prison.data.enchants.PickaxeEnchant.EnchantType;

public class TokenEnchant extends PickaxeEnchant {

    private final String id;

    public TokenEnchant(String displayName, String id, int maxLevel, double baseCost, double costMultiplier) {
        super(displayName, getDescription(id, 1), maxLevel, baseCost, "tokens", getEnchantType(id));
        this.id = id;
    }

    public String getDescription(int level) {
        return getDescription(this.id, level);
    }

    private static String getDescription(String id, int level) {
        return switch (id.toLowerCase()) {
            case "efficiency" -> "§7Increases mining speed by §a" + (level * 20) + "%";
            case "fortune" -> "§7Increases money drops by §a" + (level * 10) + "%";
            case "haste" -> "§7Increases mining haste by §a" + level + " §7levels";
            case "speed" -> "§7Increases movement speed by §a" + (level * 10) + "%";
            default -> "§7Unknown enchant effect";
        };
    }

    private static EnchantType getEnchantType(String id) {
        return switch (id.toLowerCase()) {
            case "efficiency" -> EnchantType.EFFICIENCY;
            case "fortune" -> EnchantType.FORTUNE;
            case "haste", "speed" -> EnchantType.SPECIAL;
            default -> EnchantType.SPECIAL;
        };
    }
}