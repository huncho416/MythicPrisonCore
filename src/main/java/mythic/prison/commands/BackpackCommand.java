package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.backpack.Backpack;
import mythic.prison.managers.BackpackManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class BackpackCommand extends Command {

    public BackpackCommand() {
        super("backpack", "bp");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeBackpackInfo(player);
        });

        // Add "sell" subcommand
        ArgumentWord sellArg = ArgumentType.Word("sell").from("sell");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeSell(player);
        }, sellArg);

        // Add "autosell" subcommand
        ArgumentWord autoSellArg = ArgumentType.Word("autosell").from("autosell");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            executeToggleAutoSell(player);
        }, autoSellArg);

        // Add "upgrade" subcommand with upgrade type
        ArgumentWord upgradeArg = ArgumentType.Word("upgrade").from("upgrade");
        ArgumentWord upgradeTypeArg = ArgumentType.Word("upgradeType").from("capacity", "multiplier", "autosell", "cap", "mult", "auto");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String upgradeType = context.get(upgradeTypeArg);
            executeUpgrade(player, upgradeType);
        }, upgradeArg, upgradeTypeArg);
    }

    private static void executeBackpackInfo(Player player) {
        BackpackManager backpackManager = MythicPrison.getInstance().getBackpackManager();
        Backpack backpack = backpackManager.getBackpack(player);

        if (backpack == null) {
            ChatUtil.sendError(player, "Backpack not initialized!");
            return;
        }

        ChatUtil.sendMessage(player, "§d§l§m                §r §d§lYOUR BACKPACK §r§d§l§m                ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCapacity: §a" + backpack.getCurrentVolume() + "§7/§a" + backpack.getMaxVolume() + " §7blocks");
        ChatUtil.sendMessage(player, "§f§lSell Multiplier: §a" + String.format("%.1fx", backpack.getSellMultiplier()));
        ChatUtil.sendMessage(player, "§f§lAuto-sell: " + (backpack.isAutoSellEnabled() ? "§aEnabled" : "§cDisabled"));

        if (backpack.isAutoSellEnabled()) {
            ChatUtil.sendMessage(player, "§f§lAuto-sell Interval: §a" + backpack.getAutoSellInterval() + "s");
        }

        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§fCommands:");
        ChatUtil.sendMessage(player, "§d/backpack sell §7- Sell all items");
        ChatUtil.sendMessage(player, "§d/backpack upgrade capacity §7- Upgrade capacity");
        ChatUtil.sendMessage(player, "§d/backpack upgrade multiplier §7- Upgrade sell multiplier");
        ChatUtil.sendMessage(player, "§d/backpack upgrade autosell §7- Upgrade auto-sell");
        ChatUtil.sendMessage(player, "§d/backpack autosell §7- Toggle auto-sell");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                ");
    }

    private static void executeSell(Player player) {
        BackpackManager backpackManager = MythicPrison.getInstance().getBackpackManager();
        backpackManager.sellBackpack(player, true);
    }

    private static void executeUpgrade(Player player, String upgradeType) {
        BackpackManager backpackManager = MythicPrison.getInstance().getBackpackManager();

        switch (upgradeType.toLowerCase()) {
            case "capacity", "cap" -> backpackManager.upgradeCapacity(player);
            case "multiplier", "mult" -> backpackManager.upgradeSellMultiplier(player);
            case "autosell", "auto" -> backpackManager.upgradeAutoSell(player);
            default -> ChatUtil.sendError(player, "Invalid upgrade type! Use: capacity, multiplier, or autosell");
        }
    }

    private static void executeToggleAutoSell(Player player) {
        BackpackManager backpackManager = MythicPrison.getInstance().getBackpackManager();
        backpackManager.toggleAutoSell(player);
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new BackpackCommand());
    }
}