package mythic.prison.data.enchants;

public class SoulEnchant extends PickaxeEnchant {

    public SoulEnchant(String name, String id, int maxLevel, double baseCost, double costMultiplier) {
        super(name, id, maxLevel, baseCost, costMultiplier, generateDescription(name));
    }

    @Override
    public String getCurrencyType() {
        return "souls";
    }

    private static String generateDescription(String name) {
        return switch (name.toLowerCase()) {
            case "super fortune" -> "Massively increases block drops";
            case "mega explosive" -> "Breaks huge areas of blocks";
            case "auto smelt" -> "Automatically smelts ores";
            case "void walker" -> "Allows mining through bedrock";
            case "time warp" -> "Slows down time while mining";
            default -> "A legendary soul enchant";
        };
    }
}