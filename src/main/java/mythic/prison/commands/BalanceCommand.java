package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class BalanceCommand extends Command {

    public BalanceCommand() {
        super("balance", "bal", "money");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeBalance(player);
        });

        // Add player name argument for checking other balances
        ArgumentWord playerArg = ArgumentType.Word("player");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String targetName = context.get(playerArg);
            executeBalanceOther(player, targetName);
        }, playerArg);
    }

    private static void executeBalance(Player player) {
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

        ChatUtil.sendMessage(player, "§d§l§m                    §r §d§lYOUR BALANCE §r§d§l§m                    ");
        ChatUtil.sendMessage(player, "");

        // Updated currencies array to match MultiplierManager
        String[] currencies = {"money", "tokens", "souls", "essence", "credits", "beacons"};

        for (String currency : currencies) {
            String color = currencyManager.getCurrencyColor(currency);
            String name = currency.substring(0, 1).toUpperCase() + currency.substring(1);
            double balance = currencyManager.getBalance(player, currency);
            // Use CurrencyManager formatting instead of ChatUtil
            String formattedBalance = currencyManager.formatCurrency(currency, balance);

            ChatUtil.sendMessage(player, color + "§l" + name + ": §f" + formattedBalance);
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                        ");
    }

    private static void executeBalanceOther(Player player, String targetName) {
        // Try to find the target player by name
        Player target = findPlayerByName(targetName);

        if (target == null) {
            ChatUtil.sendError(player, "Player not found!");
            return;
        }

        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();

        ChatUtil.sendMessage(player, "§d§l§m                §r §d§l" + targetName.toUpperCase() + "'S BALANCE §r§d§l§m                ");
        ChatUtil.sendMessage(player, "");

        String[] currencies = {"money", "tokens", "souls", "essence", "credits", "beacons"};

        for (String currency : currencies) {
            String color = currencyManager.getCurrencyColor(currency);
            String name = currency.substring(0, 1).toUpperCase() + currency.substring(1);
            double balance = currencyManager.getBalance(target, currency);
            // Use CurrencyManager formatting instead of ChatUtil
            String formattedBalance = currencyManager.formatCurrency(currency, balance);

            ChatUtil.sendMessage(player, color + "§l" + name + ": §f" + formattedBalance);
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                            ");
    }

    private static Player findPlayerByName(String playerName) {
        try {
            // Try to get the player from the server
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
        net.minestom.server.MinecraftServer.getCommandManager().register(new BalanceCommand());
    }
}