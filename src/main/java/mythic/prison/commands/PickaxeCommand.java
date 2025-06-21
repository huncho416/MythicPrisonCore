package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.enchants.PickaxeEnchant;
import mythic.prison.managers.PickaxeManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

public class PickaxeCommand extends Command {

    public PickaxeCommand() {
        super("pickaxe", "pick");

        // Default executor - show pickaxe info
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executePickaxeInfo((Player) sender);
        });

        // /pickaxe enchants
        ArgumentWord enchantsArg = ArgumentType.Word("enchants").from("enchants", "enchant");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeEnchants((Player) sender);
        }, enchantsArg);

        // /pickaxe token
        ArgumentWord tokenArg = ArgumentType.Word("token").from("token", "tokens");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeTokenEnchants((Player) sender);
        }, tokenArg);

        // /pickaxe soul
        ArgumentWord soulArg = ArgumentType.Word("soul").from("soul", "souls");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeSoulEnchants((Player) sender);
        }, soulArg);

        // /pickaxe buy <enchant>
        ArgumentWord buyArg = ArgumentType.Word("buy").from("buy", "purchase");
        ArgumentString enchantNameArg = ArgumentType.String("enchantName");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String enchantName = context.get(enchantNameArg);
            executeBuyEnchant(player, enchantName);
        }, buyArg, enchantNameArg);
    }

    private static void executePickaxeInfo(Player player) {
        PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();

        ChatUtil.sendMessage(player, "§d§l§m                §r §d§lYOUR PICKAXE §r§d§l§m                ");
        ChatUtil.sendMessage(player, "");

        int level = pickaxeManager.getPickaxeLevel(player);
        long exp = pickaxeManager.getPickaxeExp(player);
        int expRequired = getExpRequired(level);

        ChatUtil.sendMessage(player, "§f§lLevel: §a" + level);
        ChatUtil.sendMessage(player, "§f§lEXP: §b" + exp + "§7/§b" + expRequired);

        // Show progress bar
        float progress = Math.min(1.0f, (float) exp / expRequired);
        int barLength = 20;
        int filledBars = (int) (progress * barLength);

        StringBuilder progressBar = new StringBuilder("§7[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                progressBar.append("§a■");
            } else {
                progressBar.append("§7■");
            }
        }
        progressBar.append("§7] §f").append(String.format("%.1f", progress * 100)).append("%");

        ChatUtil.sendMessage(player, progressBar.toString());
        ChatUtil.sendMessage(player, "");

        // Show active enchants
        showActiveEnchants(player);

        ChatUtil.sendMessage(player, "§fCommands:");
        ChatUtil.sendMessage(player, "§d/pickaxe enchants §7- View all enchants");
        ChatUtil.sendMessage(player, "§d/pickaxe token §7- View token enchants");
        ChatUtil.sendMessage(player, "§d/pickaxe soul §7- View soul enchants");
        ChatUtil.sendMessage(player, "§d/pickaxe buy <enchant> §7- Buy an enchant");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                ");
    }

    private static void showActiveEnchants(Player player) {
        PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
        boolean hasEnchants = false;

        // Check token enchants
        for (String enchantName : pickaxeManager.getTokenEnchants().keySet()) {
            int level = pickaxeManager.getEnchantLevel(player, enchantName);
            if (level > 0) {
                if (!hasEnchants) {
                    ChatUtil.sendMessage(player, "§f§lActive Enchants:");
                    hasEnchants = true;
                }
                PickaxeEnchant enchant = pickaxeManager.getTokenEnchants().get(enchantName);
                ChatUtil.sendMessage(player, "  §6" + enchant.getName() + " §f" + level);
            }
        }

        // Check soul enchants
        for (String enchantName : pickaxeManager.getSoulEnchants().keySet()) {
            int level = pickaxeManager.getEnchantLevel(player, enchantName);
            if (level > 0) {
                if (!hasEnchants) {
                    ChatUtil.sendMessage(player, "§f§lActive Enchants:");
                    hasEnchants = true;
                }
                PickaxeEnchant enchant = pickaxeManager.getSoulEnchants().get(enchantName);
                ChatUtil.sendMessage(player, "  §5" + enchant.getName() + " §f" + level);
            }
        }

        if (!hasEnchants) {
            ChatUtil.sendMessage(player, "§7No enchants active. Use §f/pickaxe buy <enchant> §7to purchase enchants!");
        }
        ChatUtil.sendMessage(player, "");
    }

    private static void executeEnchants(Player player) {
        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lPICKAXE ENCHANTS §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§lToken Enchants:");
        displayEnchants(player, MythicPrison.getInstance().getPickaxeManager().getTokenEnchants(), true);
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§5§lSoul Enchants:");
        displayEnchants(player, MythicPrison.getInstance().getPickaxeManager().getSoulEnchants(), false);
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                        ");
    }

    private static void executeTokenEnchants(Player player) {
        ChatUtil.sendMessage(player, "§6§l§m            §r §6§lTOKEN ENCHANTS §r§6§l§m            ");
        ChatUtil.sendMessage(player, "");
        displayEnchants(player, MythicPrison.getInstance().getPickaxeManager().getTokenEnchants(), true);
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§l§m                                                    ");
    }

    private static void executeSoulEnchants(Player player) {
        ChatUtil.sendMessage(player, "§5§l§m            §r §5§lSOUL ENCHANTS §r§5§l§m            ");
        ChatUtil.sendMessage(player, "");
        displayEnchants(player, MythicPrison.getInstance().getPickaxeManager().getSoulEnchants(), false);
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§5§l§m                                                    ");
    }

    private static void executeBuyEnchant(Player player, String enchantName) {
        PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();

        // Check if it's a token enchant
        if (pickaxeManager.getTokenEnchants().containsKey(enchantName.toLowerCase())) {
            PickaxeEnchant enchant = pickaxeManager.getTokenEnchants().get(enchantName.toLowerCase());
            int currentLevel = pickaxeManager.getEnchantLevel(player, enchantName);

            if (currentLevel >= enchant.getMaxLevel()) {
                ChatUtil.sendError(player, enchant.getName() + " is already at maximum level!");
                return;
            }

            if (pickaxeManager.purchaseEnchant(player, enchantName, true)) {
                ChatUtil.sendSuccess(player, "Successfully purchased " + enchant.getName() + " " + (currentLevel + 1) + "!");
                return;
            }
        }

        // Check if it's a soul enchant
        if (pickaxeManager.getSoulEnchants().containsKey(enchantName.toLowerCase())) {
            PickaxeEnchant enchant = pickaxeManager.getSoulEnchants().get(enchantName.toLowerCase());
            int currentLevel = pickaxeManager.getEnchantLevel(player, enchantName);

            if (currentLevel >= enchant.getMaxLevel()) {
                ChatUtil.sendError(player, enchant.getName() + " is already at maximum level!");
                return;
            }

            if (pickaxeManager.purchaseEnchant(player, enchantName, false)) {
                ChatUtil.sendSuccess(player, "Successfully purchased " + enchant.getName() + " " + (currentLevel + 1) + "!");
                return;
            }
        }

        // Enchant not found
        ChatUtil.sendError(player, "Enchant not found: " + enchantName);
        ChatUtil.sendMessage(player, "§7Use §f/pickaxe enchants §7to see available enchants.");
    }

    private static void displayEnchants(Player player, java.util.Map<String, PickaxeEnchant> enchants, boolean isTokenEnchant) {
        PickaxeManager pickaxeManager = MythicPrison.getInstance().getPickaxeManager();

        if (enchants.isEmpty()) {
            ChatUtil.sendMessage(player, "§7No enchants available in this category.");
            return;
        }

        for (PickaxeEnchant enchant : enchants.values()) {
            int currentLevel = pickaxeManager.getEnchantLevel(player, enchant.getName());
            int maxLevel = enchant.getMaxLevel();
            boolean canBuy = pickaxeManager.canPurchaseEnchant(player, enchant.getName(), isTokenEnchant);

            String color = isTokenEnchant ? "§6" : "§5";
            String status = currentLevel >= maxLevel ? "§a§lMAXED" :
                    canBuy ? "§a§lAVAILABLE" : "§c§lLOCKED";

            double cost = enchant.getCostForLevel(currentLevel + 1);
            String formattedCost = ChatUtil.formatMoney(cost);
            String currency = isTokenEnchant ? "tokens" : "souls";

            ChatUtil.sendMessage(player, color + "§l" + enchant.getName() + " §7[§f" + currentLevel + "§7/§f" + maxLevel + "§7] " + status);
            ChatUtil.sendMessage(player, "  §7" + enchant.getDescription());

            if (currentLevel < maxLevel) {
                ChatUtil.sendMessage(player, "  §7Cost: " +
                        MythicPrison.getInstance().getCurrencyManager().getCurrencyColor(currency) +
                        formattedCost + " " + currency);
                ChatUtil.sendMessage(player, "  §7Command: §f/pickaxe buy " + enchant.getName());
            }
            ChatUtil.sendMessage(player, "");
        }
    }

    private static int getExpRequired(int level) {
        return 100 * level; // Same formula as in PickaxeManager
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new PickaxeCommand());
    }
}