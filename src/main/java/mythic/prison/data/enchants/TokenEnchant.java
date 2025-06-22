package mythic.prison.data.enchants;

public class TokenEnchant extends PickaxeEnchant {

    public TokenEnchant(String name, String id, int maxLevel, double baseCost, double costMultiplier) {
        super(name, id, maxLevel, baseCost, costMultiplier, generateDescription(name));
    }

    @Override
    public String getCurrencyType() {
        return "tokens";
    }

    private static String generateDescription(String name) {
        return switch (name.toLowerCase()) {
            case "efficiency" -> "Increases mining speed";
            case "fortune" -> "Increases block drops";
            case "explosive" -> "Breaks blocks in a radius";
            case "speed" -> "Gives speed effect while mining";
            case "haste" -> "Gives haste effect while mining";
            case "magnet" -> "Automatically picks up items";
            case "auto sell" -> "Automatically sells mined blocks";
            default -> "A powerful token enchant";
        };
    }
}