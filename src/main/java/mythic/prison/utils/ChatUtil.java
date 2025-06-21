package mythic.prison.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.text.DecimalFormat;

public class ChatUtil {
    
    private static final String PREFIX = ""; // Removed the MythicPrison prefix
    private static final String ERROR_PREFIX = "§c§l[ERROR] §r";
    private static final String SUCCESS_PREFIX = "§a§l[SUCCESS] §r";
    
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat SHORT_FORMAT = new DecimalFormat("#,##0.#");

    public static void sendMessage(Object playerObj, String message) {
        if (playerObj instanceof Player player) {
            player.sendMessage(Component.text(PREFIX + message));
        } else {
            System.out.println("[ChatUtil] " + message);
        }
    }

    public static void sendError(Object playerObj, String message) {
        if (playerObj instanceof Player player) {
            player.sendMessage(Component.text(ERROR_PREFIX + "§c" + message));
        } else {
            System.err.println("[ChatUtil ERROR] " + message);
        }
    }

    public static void sendSuccess(Object playerObj, String message) {
        if (playerObj instanceof Player player) {
            player.sendMessage(Component.text(SUCCESS_PREFIX + "§a" + message));
        } else {
            System.out.println("[ChatUtil SUCCESS] " + message);
        }
    }

    public static String formatMoney(double amount) {
        // Updated to match CurrencyManager formatting - supports extended suffixes
        if (amount >= 1000000000000L) { // 1 trillion or more
            if (amount >= 1e57) {
                return String.format("%.1fOC", amount / 1e57); // Octodecillion
            } else if (amount >= 1e54) {
                return String.format("%.1fSP", amount / 1e54); // Septendecillion
            } else if (amount >= 1e51) {
                return String.format("%.1fSD", amount / 1e51); // Sexdecillion
            } else if (amount >= 1e48) {
                return String.format("%.1fQN", amount / 1e48); // Quindecillion
            } else if (amount >= 1e45) {
                return String.format("%.1fQT", amount / 1e45); // Quattuordecillion
            } else if (amount >= 1e42) {
                return String.format("%.1fTR", amount / 1e42); // Tredecillion
            } else if (amount >= 1e39) {
                return String.format("%.1fDD", amount / 1e39); // Duodecillion
            } else if (amount >= 1e36) {
                return String.format("%.1fUN", amount / 1e36); // Undecillion
            } else if (amount >= 1e33) {
                return String.format("%.1fD", amount / 1e33); // Decillion
            } else if (amount >= 1e30) {
                return String.format("%.1fN", amount / 1e30); // Nonillion
            } else if (amount >= 1e27) {
                return String.format("%.1fO", amount / 1e27); // Octillion
            } else if (amount >= 1e24) {
                return String.format("%.1fSS", amount / 1e24); // Septillion
            } else if (amount >= 1e21) {
                return String.format("%.1fS", amount / 1e21); // Sextillion
            } else if (amount >= 1e18) {
                return String.format("%.1fQQ", amount / 1e18); // Quintillion
            } else if (amount >= 1e15) {
                return String.format("%.1fQ", amount / 1e15); // Quadrillion
            } else {
                return String.format("%.1fT", amount / 1e12); // Trillion
            }
        } else if (amount >= 1_000_000_000) {
            return SHORT_FORMAT.format(amount / 1_000_000_000) + "B";
        } else if (amount >= 1_000_000) {
            return SHORT_FORMAT.format(amount / 1_000_000) + "M";
        } else if (amount >= 1_000) {
            return SHORT_FORMAT.format(amount / 1_000) + "K";
        } else {
            return MONEY_FORMAT.format(amount);
        }
    }

    public static void broadcast(String message) {
        System.out.println("[BROADCAST] " + message);
        // In a real implementation, you'd send to all online players
    }

                // Send welcome message
}