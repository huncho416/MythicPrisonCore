package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class EconomyCommand extends Command {

    public EconomyCommand() {
        super("economy", "eco");

        setDefaultExecutor((sender, context) -> {
            // Check if player has permission (simplified check)
            if (sender instanceof Player player && !isAdmin(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }
            showEconomyHelp(sender);
        });

        // /economy give <player> <currency> <amount>
        ArgumentWord giveArg = ArgumentType.Word("give").from("give");
        ArgumentString playerArg = ArgumentType.String("player");
        ArgumentWord currencyArg = ArgumentType.Word("currency").from("money", "rubies", "essence", "coins", "tokens", "shards", "credits", "souls", "beacons", "trophies");
        ArgumentString amountArg = ArgumentType.String("amount"); // Use String and parse manually

        addSyntax((sender, context) -> {
            // Check permissions
            if (sender instanceof Player player && !isAdmin(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            String currency = context.get(currencyArg);
            String amountStr = context.get(amountArg);

            try {
                double amount = Double.parseDouble(amountStr);
                executeGive(sender, targetName, currency, amount);
            } catch (NumberFormatException e) {
                if (sender instanceof Player player) {
                    ChatUtil.sendError(player, "Invalid amount: " + amountStr);
                } else {
                    sender.sendMessage("Invalid amount: " + amountStr);
                }
            }
        }, giveArg, playerArg, currencyArg, amountArg);

        // /economy take <player> <currency> <amount>
        ArgumentWord takeArg = ArgumentType.Word("take").from("take");
        addSyntax((sender, context) -> {
            // Check permissions
            if (sender instanceof Player player && !isAdmin(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            String currency = context.get(currencyArg);
            String amountStr = context.get(amountArg);

            try {
                double amount = Double.parseDouble(amountStr);
                executeTake(sender, targetName, currency, amount);
            } catch (NumberFormatException e) {
                if (sender instanceof Player player) {
                    ChatUtil.sendError(player, "Invalid amount: " + amountStr);
                } else {
                    sender.sendMessage("Invalid amount: " + amountStr);
                }
            }
        }, takeArg, playerArg, currencyArg, amountArg);

        // /economy set <player> <currency> <amount>
        ArgumentWord setArg = ArgumentType.Word("set").from("set");
        addSyntax((sender, context) -> {
            // Check permissions
            if (sender instanceof Player player && !isAdmin(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            String currency = context.get(currencyArg);
            String amountStr = context.get(amountArg);

            try {
                double amount = Double.parseDouble(amountStr);
                executeSet(sender, targetName, currency, amount);
            } catch (NumberFormatException e) {
                if (sender instanceof Player player) {
                    ChatUtil.sendError(player, "Invalid amount: " + amountStr);
                } else {
                    sender.sendMessage("Invalid amount: " + amountStr);
                }
            }
        }, setArg, playerArg, currencyArg, amountArg);

        // /economy reset <player>
        ArgumentWord resetArg = ArgumentType.Word("reset").from("reset");
        addSyntax((sender, context) -> {
            // Check permissions
            if (sender instanceof Player player && !isAdmin(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            String targetName = context.get(playerArg);
            executeReset(sender, targetName);
        }, resetArg, playerArg);
    }

    private static boolean isAdmin(Player player) {
        // Simple admin check - allow everyone for now
        return true; // Set to true to allow everyone to use economy commands for testing
    }

    private static void showEconomyHelp(Object sender) {
        if (sender instanceof Player player) {
            ChatUtil.sendMessage(player, "§d§l§m            §r §d§lECONOMY COMMANDS §r§d§l§m            ");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§d/economy give <player> <currency> <amount> §7- Give currency");
            ChatUtil.sendMessage(player, "§d/economy take <player> <currency> <amount> §7- Take currency");
            ChatUtil.sendMessage(player, "§d/economy set <player> <currency> <amount> §7- Set currency");
            ChatUtil.sendMessage(player, "§d/economy reset <player> §7- Reset all currencies");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§fCurrencies: §emoney, rubies, essence, coins, tokens, shards, credits, souls, beacons, trophies");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§d§l§m                                                    ");
        } else {
            // For console/non-player senders, use System.out.println
            System.out.println("Economy Commands:");
            System.out.println("/economy give <player> <currency> <amount> - Give currency");
            System.out.println("/economy take <player> <currency> <amount> - Take currency");
            System.out.println("/economy set <player> <currency> <amount> - Set currency");
            System.out.println("/economy reset <player> - Reset all currencies");
            System.out.println("Currencies: money, rubies, essence, coins, tokens, shards, credits, souls, beacons, trophies");
        }
    }

    private static void executeGive(Object sender, String targetName, String currency, double amount) {
        Player target = findPlayerByName(targetName);

        if (target == null) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Player '" + targetName + "' not found!");
            } else {
                System.out.println("Player '" + targetName + "' not found!");
            }
            return;
        }

        if (amount <= 0) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Amount must be positive!");
            } else {
                System.out.println("Amount must be positive!");
            }
            return;
        }

        MythicPrison.getInstance().getCurrencyManager().addBalance(target, currency, amount);

        String formattedAmount = ChatUtil.formatMoney(amount);

        if (sender instanceof Player player) {
            ChatUtil.sendSuccess(player, "Gave " + formattedAmount + " " + currency + " to " + target.getUsername());
        } else {
            System.out.println("Gave " + formattedAmount + " " + currency + " to " + target.getUsername());
        }

        ChatUtil.sendMessage(target, "§aYou received " + formattedAmount + " " + currency + "!");
    }

    private static void executeTake(Object sender, String targetName, String currency, double amount) {
        Player target = findPlayerByName(targetName);

        if (target == null) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Player '" + targetName + "' not found!");
            } else {
                System.out.println("Player '" + targetName + "' not found!");
            }
            return;
        }

        if (amount <= 0) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Amount must be positive!");
            } else {
                System.out.println("Amount must be positive!");
            }
            return;
        }

        boolean success = MythicPrison.getInstance().getCurrencyManager().removeBalance(target, currency, amount);

        if (success) {
            String formattedAmount = ChatUtil.formatMoney(amount);

            if (sender instanceof Player player) {
                ChatUtil.sendSuccess(player, "Took " + formattedAmount + " " + currency + " from " + target.getUsername());
            } else {
                System.out.println("Took " + formattedAmount + " " + currency + " from " + target.getUsername());
            }

            ChatUtil.sendMessage(target, "§c" + formattedAmount + " " + currency + " was removed from your account.");
        } else {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, target.getUsername() + " doesn't have enough " + currency + "!");
            } else {
                System.out.println(target.getUsername() + " doesn't have enough " + currency + "!");
            }
        }
    }

    private static void executeSet(Object sender, String targetName, String currency, double amount) {
        Player target = findPlayerByName(targetName);

        if (target == null) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Player '" + targetName + "' not found!");
            } else {
                System.out.println("Player '" + targetName + "' not found!");
            }
            return;
        }

        if (amount < 0) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Amount cannot be negative!");
            } else {
                System.out.println("Amount cannot be negative!");
            }
            return;
        }

        MythicPrison.getInstance().getCurrencyManager().setBalance(target, currency, amount);

        String formattedAmount = ChatUtil.formatMoney(amount);

        if (sender instanceof Player player) {
            ChatUtil.sendSuccess(player, "Set " + target.getUsername() + "'s " + currency + " to " + formattedAmount);
        } else {
            System.out.println("Set " + target.getUsername() + "'s " + currency + " to " + formattedAmount);
        }

        ChatUtil.sendMessage(target, "§eYour " + currency + " balance was set to " + formattedAmount + "!");
    }

    private static void executeReset(Object sender, String targetName) {
        Player target = findPlayerByName(targetName);

        if (target == null) {
            if (sender instanceof Player player) {
                ChatUtil.sendError(player, "Player '" + targetName + "' not found!");
            } else {
                System.out.println("Player '" + targetName + "' not found!");
            }
            return;
        }

        // Reset all currencies
        String[] currencies = {"money", "rubies", "essence", "coins", "tokens", "shards", "credits", "souls", "beacons", "trophies"};
        for (String currency : currencies) {
            MythicPrison.getInstance().getCurrencyManager().setBalance(target, currency, 0);
        }

        if (sender instanceof Player player) {
            ChatUtil.sendSuccess(player, "Reset all currencies for " + target.getUsername());
        } else {
            System.out.println("Reset all currencies for " + target.getUsername());
        }

        ChatUtil.sendMessage(target, "§cAll your currencies have been reset!");
    }

    private static Player findPlayerByName(String playerName) {
        try {
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            return connectionManager.getOnlinePlayers().stream()
                    .filter(p -> p.getUsername().equalsIgnoreCase(playerName))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding player: " + e.getMessage());
            return null;
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new EconomyCommand());
    }
}