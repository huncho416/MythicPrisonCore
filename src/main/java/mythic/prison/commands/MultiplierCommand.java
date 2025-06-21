package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.MultiplierManager;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.MinecraftServer;

public class MultiplierCommand extends Command {

    public MultiplierCommand() {
        super("multiplier", "multi");

        // /multiplier (show own multipliers)
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return;
            }
            showPlayerMultipliers(player, player);
        });

        // /multiplier set <player> <type> <amount> [duration]
        var playerArg = ArgumentType.Word("player");
        var typeArg = ArgumentType.Word("type").from("money", "tokens", "souls", "beacons", "experience", "gems", "universal", "enchant");
        var amountArg = ArgumentType.Double("amount");
        var durationArg = ArgumentType.Long("duration");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return;
            }

            if (!isOperator(player)) {
                sendMessage(player, "§cYou don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            String type = context.get(typeArg);
            double amount = context.get(amountArg);
            Long duration = context.get(durationArg);

            Player target = getPlayerByName(targetName);
            if (target == null) {
                sendMessage(player, "§cPlayer '" + targetName + "' not found!");
                return;
            }

            MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
            long durationMs = duration != null ? duration * 1000 : 0;

            multiplierManager.setMultiplier(target, type, amount, durationMs);

            if (duration != null && duration > 0) {
                sendMessage(player, "§aSet " + type + " multiplier to " + String.format("%.2fx", amount) + 
                    " for " + target.getUsername() + " for " + duration + " seconds!");
                sendMessage(target, "§aYou received a " + String.format("%.2fx", amount) + 
                    " " + type + " multiplier for " + duration + " seconds!");
            } else {
                sendMessage(player, "§aSet permanent " + type + " multiplier to " + String.format("%.2fx", amount) + 
                    " for " + target.getUsername() + "!");
                sendMessage(target, "§aYou received a permanent " + String.format("%.2fx", amount) + 
                    " " + type + " multiplier!");
            }

        }, ArgumentType.Literal("set"), playerArg, typeArg, amountArg, durationArg);

        // /multiplier set <player> <type> <amount>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return;
            }

            if (!isOperator(player)) {
                sendMessage(player, "§cYou don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            String type = context.get(typeArg);
            double amount = context.get(amountArg);

            Player target = getPlayerByName(targetName);
            if (target == null) {
                sendMessage(player, "§cPlayer '" + targetName + "' not found!");
                return;
            }

            MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
            multiplierManager.setMultiplier(target, type, amount, 0);

            sendMessage(player, "§aSet permanent " + type + " multiplier to " + String.format("%.2fx", amount) + 
                " for " + target.getUsername() + "!");
            sendMessage(target, "§aYou received a permanent " + String.format("%.2fx", amount) + 
                " " + type + " multiplier!");

        }, ArgumentType.Literal("set"), playerArg, typeArg, amountArg);

        // /multiplier remove <player> <type>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return;
            }

            if (!isOperator(player)) {
                sendMessage(player, "§cYou don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            String type = context.get(typeArg);

            Player target = getPlayerByName(targetName);
            if (target == null) {
                sendMessage(player, "§cPlayer '" + targetName + "' not found!");
                return;
            }

            MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
            multiplierManager.removeMultiplier(target, type);

            sendMessage(player, "§aRemoved " + type + " multiplier from " + target.getUsername() + "!");
            sendMessage(target, "§cYour " + type + " multiplier has been removed!");

        }, ArgumentType.Literal("remove"), playerArg, typeArg);

        // /multiplier check <player>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            Player target = getPlayerByName(targetName);
            if (target == null) {
                sendMessage(player, "§cPlayer '" + targetName + "' not found!");
                return;
            }

            showPlayerMultipliers(player, target);

        }, ArgumentType.Literal("check"), playerArg);

        // /multiplier help
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return;
            }

            sendMessage(player, "§6=== Multiplier Commands ===");
            sendMessage(player, "§e/multiplier §7- Show your multipliers");
            sendMessage(player, "§e/multiplier check <player> §7- Check player's multipliers");
            if (isOperator(player)) {
                sendMessage(player, "§c=== Admin Commands ===");
                sendMessage(player, "§e/multiplier set <player> <type> <amount> [duration] §7- Set a multiplier");
                sendMessage(player, "§e/multiplier remove <player> <type> §7- Remove a multiplier");
            }
            sendMessage(player, "§7Currency Types: money, tokens, souls, beacons, experience, gems");
            sendMessage(player, "§7Special Types: universal, enchant");

        }, ArgumentType.Literal("help"));
    }

    private void showPlayerMultipliers(Player viewer, Player target) {
        MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        
        String title = target.equals(viewer) ? "Your Multipliers" : target.getUsername() + "'s Multipliers";
        
        sendMessage(viewer, "§6§l§m                                                ");
        sendMessage(viewer, "§6§l           " + title.toUpperCase());
        sendMessage(viewer, "§6§l§m                                                ");
        sendMessage(viewer, "");
        
        // Currency multipliers (using CurrencyManager colors)
        String[] currencyTypes = {"money", "tokens", "souls", "beacons", "experience", "gems"};
        
        sendMessage(viewer, "§6§lCurrency Multipliers:");
        for (String type : currencyTypes) {
            double baseMultiplier = multiplierManager.getMultiplier(target, type);
            double totalMultiplier = multiplierManager.getTotalMultiplier(target, type);
            
            // Get the actual color from CurrencyManager
            String color = currencyManager.getCurrencyColor(type);
            String icon = getMultiplierIcon(type);
            String displayName = formatMultiplierType(type);
            
            if (baseMultiplier > 1.0 || totalMultiplier > 1.0) {
                sendMessage(viewer, color + "§l" + icon + " " + displayName + ": §f" + 
                    String.format("%.2fx", totalMultiplier) + 
                    (baseMultiplier != totalMultiplier ? " §7(Base: " + String.format("%.2fx", baseMultiplier) + ")" : ""));
            } else {
                sendMessage(viewer, "§7" + icon + " " + displayName + ": §f1.00x");
            }
        }
        
        sendMessage(viewer, "");
        
        // Special multipliers
        sendMessage(viewer, "§d§lSpecial Multipliers:");
        
        // Universal multiplier
        double universalMultiplier = multiplierManager.getMultiplier(target, "universal");
        if (universalMultiplier > 1.0) {
            sendMessage(viewer, "§f§l🌟 Universal: §f" + String.format("%.2fx", universalMultiplier) + " §7(Multiplies all currencies)");
        } else {
            sendMessage(viewer, "§7🌟 Universal: §f1.00x §7(Multiplies all currencies)");
        }
        
        // Enchant multiplier  
        double enchantMultiplier = multiplierManager.getMultiplier(target, "enchant");
        if (enchantMultiplier > 1.0) {
            sendMessage(viewer, "§5§l✨ Enchant: §f" + String.format("%.2fx", enchantMultiplier) + " §7(Multiplies enchant proc chance)");
        } else {
            sendMessage(viewer, "§7✨ Enchant: §f1.00x §7(Multiplies enchant proc chance)");
        }
        
        sendMessage(viewer, "");
        sendMessage(viewer, "§7Use §e/multiplier help §7for more commands");
        sendMessage(viewer, "§6§l§m                                                ");
    }

    private String getMultiplierIcon(String type) {
        return switch (type.toLowerCase()) {
            case "money" -> "💰";      // Same as scoreboard
            case "tokens" -> "⚡";     // Same as scoreboard
            case "souls" -> "👻";      // Same as scoreboard
            case "beacons" -> "🔶";    // Same as scoreboard
            case "experience" -> "⭐";
            case "gems" -> "💎";
            case "universal" -> "🌟";
            case "enchant" -> "✨";
            default -> "📦";
        };
    }

    private String formatMultiplierType(String type) {
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        String color = currencyManager.getCurrencyColor(type);

        return switch (type.toLowerCase()) {
            case "money" -> color + "Money";
            case "tokens" -> color + "Tokens";
            case "souls" -> color + "Souls";
            case "gems" -> color + "Gems";
            case "crystals" -> color + "Crystals";
            default -> "§7" + type; // Default grey for unknown types
        };
    }

    private Player getPlayerByName(String name) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equalsIgnoreCase(name)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    private boolean isOperator(Player player) {
        return player.getUsername().equalsIgnoreCase("admin") || 
               player.getUsername().equalsIgnoreCase("owner") ||
               player.getUsername().equalsIgnoreCase(System.getProperty("server.owner", ""));
    }

    private void sendMessage(Player player, String message) {
        try {
            ChatUtil.sendMessage(player, message);
        } catch (Exception e) {
            player.sendMessage(message);
        }
    }

    public static void register() {
        MinecraftServer.getCommandManager().register(new MultiplierCommand());
    }
}