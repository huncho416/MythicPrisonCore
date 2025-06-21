package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.HashMap;

public class CurrencyManager {

    private static final Map<String, CurrencyInfo> CURRENCY_INFO = new HashMap<>();
    
    static {
        // Initialize currency information
        CURRENCY_INFO.put("money", new CurrencyInfo("Money", "§a", "$", "💰"));
        CURRENCY_INFO.put("tokens", new CurrencyInfo("Tokens", "§b", "", "⚡"));
        CURRENCY_INFO.put("souls", new CurrencyInfo("Souls", "§d", "", "👻"));
        CURRENCY_INFO.put("beacons", new CurrencyInfo("Beacons", "§e", "", "🔆"));
        CURRENCY_INFO.put("gems", new CurrencyInfo("Gems", "§c", "", "💎"));
        CURRENCY_INFO.put("ascension_points", new CurrencyInfo("Ascension Points", "§5", "", "⭐"));
    }

    public double getBalance(Player player, String currency) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return 0.0;
        return profile.getBalance(currency);
    }

    public double getBalance(String uuid, String currency) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(uuid);
        if (profile == null) return 0.0;
        return profile.getBalance(currency);
    }

    public void setBalance(Player player, String currency, double amount) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return;
        
        profile.setBalance(currency, amount);
        
        // Track money earned for stats
        if ("money".equals(currency) && amount > profile.getBalance(currency)) {
            profile.addMoneyEarned(amount - profile.getBalance(currency));
        }
    }

    public boolean addBalance(Player player, String currency, double amount) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;
        
        boolean success = profile.addBalance(currency, amount);
        
        // Track money earned for stats
        if (success && "money".equals(currency)) {
            profile.addMoneyEarned(amount);
        }
        
        return success;
    }

    public boolean removeBalance(Player player, String currency, double amount) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return false;
        
        return profile.removeBalance(currency, amount);
    }

    public boolean hasBalance(Player player, String currency, double amount) {
        return getBalance(player, currency) >= amount;
    }

    public boolean transferMoney(Player from, Player to, String currency, double amount) {
        if (amount <= 0) return false;
        
        PlayerProfile fromProfile = MythicPrison.getInstance().getProfileManager().getProfile(from);
        PlayerProfile toProfile = MythicPrison.getInstance().getProfileManager().getProfile(to);
        
        if (fromProfile == null || toProfile == null) return false;
        
        if (fromProfile.getBalance(currency) < amount) return false;
        
        fromProfile.removeBalance(currency, amount);
        toProfile.addBalance(currency, amount);
        
        return true;
    }

    // Currency display methods
    public String getCurrencyColor(String currency) {
        CurrencyInfo info = CURRENCY_INFO.get(currency.toLowerCase());
        return info != null ? info.getColor() : "§7";
    }

    public String getCurrencyName(String currency) {
        CurrencyInfo info = CURRENCY_INFO.get(currency.toLowerCase());
        return info != null ? info.getName() : currency.substring(0, 1).toUpperCase() + currency.substring(1);
    }

    public String getCurrencySymbol(String currency) {
        CurrencyInfo info = CURRENCY_INFO.get(currency.toLowerCase());
        return info != null ? info.getSymbol() : "";
    }

    public String getCurrencyIcon(String currency) {
        CurrencyInfo info = CURRENCY_INFO.get(currency.toLowerCase());
        return info != null ? info.getIcon() : "📊";
    }

    public String formatCurrency(String currency, double amount) {
        CurrencyInfo info = CURRENCY_INFO.get(currency.toLowerCase());
        if (info != null) {
            String color = info.getColor();
            String symbol = info.getSymbol();
            String icon = info.getIcon();
            
            if ("money".equals(currency.toLowerCase())) {
                return color + symbol + formatMoney(amount);
            } else {
                return color + icon + " " + formatNumber(amount);
            }
        }
        return "§7" + formatNumber(amount);
    }

    public String formatMoney(double amount) {
        return formatLargeNumber(amount, true);
    }

    private String formatNumber(double number) {
        // Handle whole numbers without decimal places
        if (number == Math.floor(number) && !Double.isInfinite(number)) {
            // It's a whole number, format without decimals
            if (number >= 1e30) { // Nonillions
                double nonillions = number / 1e30;
                if (nonillions == Math.floor(nonillions)) {
                    return String.format("%.0fn", nonillions);
                } else {
                    return String.format("%.1fn", nonillions);
                }
            } else if (number >= 1e27) { // Octillions
                double octillions = number / 1e27;
                if (octillions == Math.floor(octillions)) {
                    return String.format("%.0fo", octillions);
                } else {
                    return String.format("%.1fo", octillions);
                }
            } else if (number >= 1e24) { // Septillions
                double septillions = number / 1e24;
                if (septillions == Math.floor(septillions)) {
                    return String.format("%.0fss", septillions);
                } else {
                    return String.format("%.1fss", septillions);
                }
            } else if (number >= 1e21) { // Sextillions
                double sextillions = number / 1e21;
                if (sextillions == Math.floor(sextillions)) {
                    return String.format("%.0fs", sextillions);
                } else {
                    return String.format("%.1fs", sextillions);
                }
            } else if (number >= 1e18) { // Quintillions
                double quintillions = number / 1e18;
                if (quintillions == Math.floor(quintillions)) {
                    return String.format("%.0fqq", quintillions);
                } else {
                    return String.format("%.1fqq", quintillions);
                }
            } else if (number >= 1_000_000_000_000_000L) { // Quadrillions
                double quadrillions = number / 1_000_000_000_000_000L;
                if (quadrillions == Math.floor(quadrillions)) {
                    return String.format("%.0fqd", quadrillions);
                } else {
                    return String.format("%.1fqd", quadrillions);
                }
            } else if (number >= 1_000_000_000_000L) { // Trillions
                double trillions = number / 1_000_000_000_000L;
                if (trillions == Math.floor(trillions)) {
                    return String.format("%.0ft", trillions);
                } else {
                    return String.format("%.1ft", trillions);
                }
            } else if (number >= 1_000_000_000) { // Billions
                double billions = number / 1_000_000_000;
                if (billions == Math.floor(billions)) {
                    return String.format("%.0fb", billions);
                } else {
                    return String.format("%.1fb", billions);
                }
            } else if (number >= 1_000_000) { // Millions
                double millions = number / 1_000_000;
                if (millions == Math.floor(millions)) {
                    return String.format("%.0fm", millions);
                } else {
                    return String.format("%.1fm", millions);
                }
            } else if (number >= 1_000) { // Thousands
                double thousands = number / 1_000;
                if (thousands == Math.floor(thousands)) {
                    return String.format("%.0fk", thousands);
                } else {
                    return String.format("%.1fk", thousands);
                }
            } else {
                return String.format("%.0f", number);
            }
        } else {
            // It has decimal places, format normally
            if (number >= 1e30) {
                return String.format("%.1fn", number / 1e30);
            } else if (number >= 1e27) {
                return String.format("%.1fo", number / 1e27);
            } else if (number >= 1e24) {
                return String.format("%.1fss", number / 1e24);
            } else if (number >= 1e21) {
                return String.format("%.1fs", number / 1e21);
            } else if (number >= 1e18) {
                return String.format("%.1fqq", number / 1e18);
            } else if (number >= 1_000_000_000_000_000L) {
                return String.format("%.1fqd", number / 1_000_000_000_000_000L);
            } else if (number >= 1_000_000_000_000L) {
                return String.format("%.1ft", number / 1_000_000_000_000L);
            } else if (number >= 1_000_000_000) {
                return String.format("%.1fb", number / 1_000_000_000);
            } else if (number >= 1_000_000) {
                return String.format("%.1fm", number / 1_000_000);
            } else if (number >= 1_000) {
                return String.format("%.1fk", number / 1_000);
            } else {
                return String.format("%.2f", number);
            }
        }
    }

    /**
     * Formats large numbers with appropriate suffixes
     * Supports up to octodecillion (1e57)
     */
    private String formatLargeNumber(double amount, boolean isMoney) {
        if (amount < 0) {
            return "-" + formatLargeNumber(-amount, isMoney);
        }

        // Debug: Let's test the logic with specific values
        // System.out.println("Formatting amount: " + amount);

        // Use long values for exact comparisons to avoid floating point issues
        if (amount >= 1000000000000L) { // 1 trillion (1e12)
            if (amount >= 1e57) {
                return formatWithSuffix(amount, 1e57, "OC", isMoney); // Octodecillion
            } else if (amount >= 1e54) {
                return formatWithSuffix(amount, 1e54, "SP", isMoney); // Septendecillion
            } else if (amount >= 1e51) {
                return formatWithSuffix(amount, 1e51, "SD", isMoney); // Sexdecillion
            } else if (amount >= 1e48) {
                return formatWithSuffix(amount, 1e48, "QN", isMoney); // Quindecillion
            } else if (amount >= 1e45) {
                return formatWithSuffix(amount, 1e45, "QT", isMoney); // Quattuordecillion
            } else if (amount >= 1e42) {
                return formatWithSuffix(amount, 1e42, "TR", isMoney); // Tredecillion
            } else if (amount >= 1e39) {
                return formatWithSuffix(amount, 1e39, "DD", isMoney); // Duodecillion
            } else if (amount >= 1e36) {
                return formatWithSuffix(amount, 1e36, "UN", isMoney); // Undecillion
            } else if (amount >= 1e33) {
                return formatWithSuffix(amount, 1e33, "D", isMoney); // Decillion
            } else if (amount >= 1e30) {
                return formatWithSuffix(amount, 1e30, "N", isMoney); // Nonillion
            } else if (amount >= 1e27) {
                return formatWithSuffix(amount, 1e27, "O", isMoney); // Octillion
            } else if (amount >= 1e24) {
                return formatWithSuffix(amount, 1e24, "SS", isMoney); // Septillion
            } else if (amount >= 1e21) {
                return formatWithSuffix(amount, 1e21, "S", isMoney); // Sextillion
            } else if (amount >= 1e18) {
                return formatWithSuffix(amount, 1e18, "QQ", isMoney); // Quintillion
            } else if (amount >= 1e15) {
                return formatWithSuffix(amount, 1e15, "Q", isMoney); // Quadrillion
            } else {
                return formatWithSuffix(amount, 1e12, "T", isMoney); // Trillion
            }
        } else if (amount >= 1000000000L) { // 1 billion (1e9)
            return formatWithSuffix(amount, 1e9, "B", isMoney); // Billion
        } else if (amount >= 1000000L) { // 1 million (1e6)
            return formatWithSuffix(amount, 1e6, "M", isMoney); // Million
        } else if (amount >= 1000L) { // 1 thousand (1e3)
            return formatWithSuffix(amount, 1e3, "K", isMoney); // Thousand
        } else {
            // Format small numbers
            if (isMoney) {
                return String.format("%.2f", amount);
            } else {
                if (amount == (long) amount) {
                    return String.format("%.0f", amount);
                } else {
                    return String.format("%.1f", amount);
                }
            }
        }
    }

    /**
     * Helper method to format a number with a specific suffix and divisor
     */
    private String formatWithSuffix(double amount, double divisor, String suffix, boolean isMoney) {
        double scaledAmount = amount / divisor;
        
        if (isMoney) {
            if (scaledAmount >= 100) {
                return String.format("%.0f%s", scaledAmount, suffix);
            } else if (scaledAmount >= 10) {
                return String.format("%.1f%s", scaledAmount, suffix);
            } else {
                return String.format("%.2f%s", scaledAmount, suffix);
            }
        } else {
            if (scaledAmount >= 100) {
                return String.format("%.0f%s", scaledAmount, suffix);
            } else {
                return String.format("%.1f%s", scaledAmount, suffix);
            }
        }
    }

    // Get all currencies with positive balance
    public Map<String, Double> getAllBalances(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) return new HashMap<>();
        
        Map<String, Double> balances = new HashMap<>();
        for (String currency : CURRENCY_INFO.keySet()) {
            double balance = profile.getBalance(currency);
            if (balance > 0) {
                balances.put(currency, balance);
            }
        }
        return balances;
    }

    // Check if currency exists
    public boolean isValidCurrency(String currency) {
        return CURRENCY_INFO.containsKey(currency.toLowerCase());
    }

    // Get all available currencies
    public String[] getAvailableCurrencies() {
        return CURRENCY_INFO.keySet().toArray(new String[0]);
    }

    // Inner class for currency information
    private static class CurrencyInfo {
        private final String name;
        private final String color;
        private final String symbol;
        private final String icon;

        public CurrencyInfo(String name, String color, String symbol, String icon) {
            this.name = name;
            this.color = color;
            this.symbol = symbol;
            this.icon = icon;
        }

        public String getName() { return name; }
        public String getColor() { return color; }
        public String getSymbol() { return symbol; }
        public String getIcon() { return icon; }
    }
}