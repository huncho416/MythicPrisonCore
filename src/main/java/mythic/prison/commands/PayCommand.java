package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class PayCommand extends Command {

    // Define the currencies based on what's available in CurrencyManager
    private static final String[] CURRENCIES = {"money", "tokens", "souls", "essence", "credits", "beacons"};

    public PayCommand() {
        super("pay", "send");

        // /pay <player> <amount> [currency]
        ArgumentString playerArg = ArgumentType.String("player");
        ArgumentString amountArg = ArgumentType.String("amount");
        ArgumentString currencyArg = ArgumentType.String("currency");

        // /pay <player> <amount> (default to money)
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String targetName = context.get(playerArg);
            String amountStr = context.get(amountArg);

            try {
                double amount = Double.parseDouble(amountStr);
                executePay(player, targetName, amount, "money");
            } catch (NumberFormatException e) {
                ChatUtil.sendError(player, "Invalid amount: " + amountStr);
                ChatUtil.sendMessage(player, "§7Please enter a valid number.");
            }
        }, playerArg, amountArg);

        // /pay <player> <amount> <currency>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String targetName = context.get(playerArg);
            String amountStr = context.get(amountArg);
            String currency = context.get(currencyArg);

            try {
                double amount = Double.parseDouble(amountStr);
                executePay(player, targetName, amount, currency);
            } catch (NumberFormatException e) {
                ChatUtil.sendError(player, "Invalid amount: " + amountStr);
                ChatUtil.sendMessage(player, "§7Please enter a valid number.");
            }
        }, playerArg, amountArg, currencyArg);

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            ChatUtil.sendError(player, "Usage: /pay <player> <amount> [currency]");
            ChatUtil.sendMessage(player, "§7Available currencies: " + String.join("§7, §f", CURRENCIES));
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§7Examples:");
            ChatUtil.sendMessage(player, "§f/pay PlayerName 1000 §7- Send $1000 money");
            ChatUtil.sendMessage(player, "§f/pay PlayerName 50 tokens §7- Send 50 tokens");
            ChatUtil.sendMessage(player, "§f/pay PlayerName 25 souls §7- Send 25 souls");
        });
    }

    private static void executePay(Player player, String targetName, double amount, String currency) {
        // Validate amount
        if (amount <= 0) {
            ChatUtil.sendError(player, "Amount must be positive!");
            return;
        }

        // Check for reasonable limits
        if (amount > 1_000_000_000) {
            ChatUtil.sendError(player, "Amount is too large!");
            ChatUtil.sendMessage(player, "§7Maximum transfer: 1,000,000,000");
            return;
        }

        // Find target player
        Player target = findPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(player, "Player '" + targetName + "' not found!");
            ChatUtil.sendMessage(player, "§7Make sure the player is online and the name is correct.");
            return;
        }

        // Check if trying to send to self
        if (player.getUsername().equals(target.getUsername())) {
            ChatUtil.sendError(player, "You cannot send money to yourself!");
            return;
        }

        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

        // Validate currency
        boolean validCurrency = false;
        for (String validCur : CURRENCIES) {
            if (validCur.equalsIgnoreCase(currency)) {
                currency = validCur;
                validCurrency = true;
                break;
            }
        }

        if (!validCurrency) {
            ChatUtil.sendError(player, "Invalid currency: " + currency);
            ChatUtil.sendMessage(player, "§7Valid currencies: §f" + String.join("§7, §f", CURRENCIES));
            return;
        }

        // Check if player has enough balance
        double playerBalance = currencyManager.getBalance(player, currency);
        if (playerBalance < amount) {
            String color = currencyManager.getCurrencyColor(currency);
            ChatUtil.sendError(player, "Insufficient " + currency + "!");
            ChatUtil.sendMessage(player, "§7You have: " + color + ChatUtil.formatMoney(playerBalance) + " " + currency);
            ChatUtil.sendMessage(player, "§7You need: " + color + ChatUtil.formatMoney(amount) + " " + currency);
            return;
        }

        // Perform the transfer
        if (transferMoney(player, target, currency, amount, currencyManager)) {
            String color = currencyManager.getCurrencyColor(currency);
            String symbol = currencyManager.getCurrencySymbol(currency);
            String formattedAmount = ChatUtil.formatMoney(amount);
            String currencyDisplay = currency.equals("money") ? "" : " " + currency;

            // Notify sender
            ChatUtil.sendSuccess(player, "You sent " + color + symbol + formattedAmount + currencyDisplay + " §ato " + target.getUsername() + "!");

            // Show new balance
            double newBalance = currencyManager.getBalance(player, currency);
            ChatUtil.sendMessage(player, "§7New balance: " + color + symbol + ChatUtil.formatMoney(newBalance) + currencyDisplay);

            // Notify receiver
            ChatUtil.sendSuccess(target, "You received " + color + symbol + formattedAmount + currencyDisplay + " §afrom " + player.getUsername() + "!");

            // Show receiver's new balance
            double targetBalance = currencyManager.getBalance(target, currency);
            ChatUtil.sendMessage(target, "§7New balance: " + color + symbol + ChatUtil.formatMoney(targetBalance) + currencyDisplay);

            // Log the transaction
            System.out.println("[PayCommand] " + player.getUsername() + " sent " + formattedAmount + " " + currency + " to " + target.getUsername());

            // Send confirmation with transaction details
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§7§l§m    §r §7Transaction Complete §r§7§l§m    ");
            ChatUtil.sendMessage(player, "§7From: §f" + player.getUsername());
            ChatUtil.sendMessage(player, "§7To: §f" + target.getUsername());
            ChatUtil.sendMessage(player, "§7Amount: " + color + symbol + formattedAmount + currencyDisplay);
            ChatUtil.sendMessage(player, "§7§l§m                              ");
        } else {
            ChatUtil.sendError(player, "Transfer failed! Please try again.");
            ChatUtil.sendMessage(player, "§7If this continues, contact an administrator.");
        }
    }

    private static boolean transferMoney(Player from, Player to, String currency, double amount, CurrencyManager currencyManager) {
        // Check if sender has enough balance
        if (!currencyManager.hasBalance(from, currency, amount)) {
            return false;
        }

        // Remove from sender
        if (currencyManager.removeBalance(from, currency, amount)) {
            // Add to receiver
            currencyManager.addBalance(to, currency, amount);
            return true;
        }

        return false;
    }

    private static Player findPlayerByName(String playerName) {
        try {
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            var players = connectionManager.getOnlinePlayers();

            for (Player player : players) {
                if (player.getUsername().equalsIgnoreCase(playerName)) {
                    return player;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding player: " + e.getMessage());
        }
        return null;
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new PayCommand());
    }
}