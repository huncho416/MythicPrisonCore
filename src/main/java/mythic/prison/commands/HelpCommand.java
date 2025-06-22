package mythic.prison.commands;

import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help");

        // Default executor - shows main help
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            showMainHelp((Player) sender);
        });

        // Add category argument for specific help topics
        ArgumentWord categoryArg = ArgumentType.Word("category")
                .from("mining", "ranking", "currency", "backpack", "pickaxe", "pets", "gangs", "multipliers", "auto", "automation", "commands");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String category = context.get(categoryArg);
            showCategoryHelp(player, category);
        }, categoryArg);
    }

    private void showMainHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lMYTHIC PRISON HELP §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lHelp Categories:");
        ChatUtil.sendMessage(player, "§d/help mining §7- Mining, blocks, and mines");
        ChatUtil.sendMessage(player, "§d/help ranking §7- Ranks, prestige, rebirth, ascension");
        ChatUtil.sendMessage(player, "§d/help currency §7- Money, tokens, souls, and other currencies");
        ChatUtil.sendMessage(player, "§d/help backpack §7- Inventory management and auto-sell");
        ChatUtil.sendMessage(player, "§d/help pickaxe §7- Pickaxe levels and enchants");
        ChatUtil.sendMessage(player, "§d/help pets §7- Pet system and companions");
        ChatUtil.sendMessage(player, "§d/help gangs §7- Gang creation and management");
        ChatUtil.sendMessage(player, "§d/help multipliers §7- Boosts and multipliers");
        ChatUtil.sendMessage(player, "§d/help auto §7- Automation features");
        ChatUtil.sendMessage(player, "§d/help commands §7- All available commands");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lQuick Commands:");
        ChatUtil.sendMessage(player, "§d/rank §7- View your rank information");
        ChatUtil.sendMessage(player, "§d/bal §7- Check your balance");
        ChatUtil.sendMessage(player, "§d/mine §7- Go to mines");
        ChatUtil.sendMessage(player, "§d/backpack §7- Open your backpack");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                    ");
    }

    private void showCategoryHelp(Player player, String category) {
        switch (category.toLowerCase()) {
            case "mining" -> showMiningHelp(player);
            case "ranking" -> showRankingHelp(player);
            case "currency" -> showCurrencyHelp(player);
            case "backpack" -> showBackpackHelp(player);
            case "pickaxe" -> showPickaxeHelp(player);
            case "pets" -> showPetsHelp(player);
            case "gangs" -> showGangsHelp(player);
            case "multipliers" -> showMultipliersHelp(player);
            case "auto", "automation" -> showAutoHelp(player);
            case "commands" -> showCommandsHelp(player);
            default -> {
                ChatUtil.sendError(player, "Unknown help category: " + category);
                showMainHelp(player);
            }
        }
    }

    private void showMiningHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§l§m            §r §6§lMINING SYSTEM §r§6§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lMining Basics:");
        ChatUtil.sendMessage(player, "§7• Break blocks in mines to earn money and items");
        ChatUtil.sendMessage(player, "§7• Different mines have different block values");
        ChatUtil.sendMessage(player, "§7• Higher rank mines give more money per block");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lMine Commands:");
        ChatUtil.sendMessage(player, "§d/mine §7- Teleport to your current mine");
        ChatUtil.sendMessage(player, "§d/mine list §7- See all available mines");
        ChatUtil.sendMessage(player, "§d/mine <name> §7- Go to a specific mine");
        ChatUtil.sendMessage(player, "§d/visit mine §7- Alternative mine teleport");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lTips:");
        ChatUtil.sendMessage(player, "§7• Use fortune enchants to get more drops");
        ChatUtil.sendMessage(player, "§7• Enable auto-sell to maximize efficiency");
        ChatUtil.sendMessage(player, "§7• Check §d/multipliers §7for active boosts");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§l§m                                                ");
    }

    private void showRankingHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§l§m            §r §e§lRANKING SYSTEM §r§e§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lRank System:");
        ChatUtil.sendMessage(player, "§7• Progress from rank A to Z using money");
        ChatUtil.sendMessage(player, "§7• Each rank costs more than the previous");
        ChatUtil.sendMessage(player, "§7• Higher ranks unlock better mines and features");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lPrestige System:");
        ChatUtil.sendMessage(player, "§7• Available when you reach rank Z");
        ChatUtil.sendMessage(player, "§7• §c§lCosts money §7- increases with each prestige");
        ChatUtil.sendMessage(player, "§7• Resets rank to A but gives permanent multipliers");
        ChatUtil.sendMessage(player, "§7• Rewards: Souls + 10% money multiplier per prestige");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lRebirth System:");
        ChatUtil.sendMessage(player, "§7• Requires 10+ prestiges to unlock");
        ChatUtil.sendMessage(player, "§7• §c§lCosts significantly more money §7than prestige");
        ChatUtil.sendMessage(player, "§7• Resets rank and prestige but keeps currencies");
        ChatUtil.sendMessage(player, "§7• Rewards: Beacons + 50% money + 25% soul multipliers");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAscension System:");
        ChatUtil.sendMessage(player, "§7• Requires 5+ rebirths to unlock");
        ChatUtil.sendMessage(player, "§7• §c§lCosts extreme amounts of money");
        ChatUtil.sendMessage(player, "§7• Resets everything except currencies");
        ChatUtil.sendMessage(player, "§7• Rewards: 100% money + 50% soul + 25% beacon multipliers");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCommands:");
        ChatUtil.sendMessage(player, "§d/rank §7- View your rank information");
        ChatUtil.sendMessage(player, "§d/rankup §7- Rank up (costs money)");
        ChatUtil.sendMessage(player, "§d/rankupmax §7- Rank up as much as possible");
        ChatUtil.sendMessage(player, "§d/prestige §7- View prestige information");
        ChatUtil.sendMessage(player, "§d/rebirth §7- View rebirth information");
        ChatUtil.sendMessage(player, "§d/ascension §7- View ascension information");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l⚠ IMPORTANT: All progression now costs money! ⚠");
        ChatUtil.sendMessage(player, "§7Save up before attempting prestige/rebirth/ascension!");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§l§m                                                    ");
    }

    private void showCurrencyHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§a§l§m            §r §a§lCURRENCY SYSTEM §r§a§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCurrency Types:");
        ChatUtil.sendMessage(player, "§a§lMoney §7- Primary currency, earned from mining");
        ChatUtil.sendMessage(player, "§6§lTokens §7- Earned from mining, used for enchants");
        ChatUtil.sendMessage(player, "§5§lSouls §7- Earned from prestiging, used for soul enchants");
        ChatUtil.sendMessage(player, "§b§lEssence §7- Rare currency from special events");
        ChatUtil.sendMessage(player, "§c§lCredits §7- Premium currency for special purchases");
        ChatUtil.sendMessage(player, "§e§lBeacons §7- Earned from rebirthing, very valuable");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lCurrency Commands:");
        ChatUtil.sendMessage(player, "§d/bal §7- Check your balance");
        ChatUtil.sendMessage(player, "§d/bal <currency> §7- Check specific currency");
        ChatUtil.sendMessage(player, "§d/pay <player> <amount> [currency] §7- Send money");
        ChatUtil.sendMessage(player, "§d/leaderboard §7- View top players by currency");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lTips:");
        ChatUtil.sendMessage(player, "§7• Save money for ranking up and progression");
        ChatUtil.sendMessage(player, "§7• Use tokens to buy pickaxe enchants");
        ChatUtil.sendMessage(player, "§7• Souls from prestiging unlock powerful enchants");
        ChatUtil.sendMessage(player, "§7• Multipliers increase currency gains");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§a§l§m                                                ");
    }

    private void showBackpackHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§9§l§m            §r §9§lBACKPACK SYSTEM §r§9§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lBackpack Features:");
        ChatUtil.sendMessage(player, "§7• Extra storage space for mining items");
        ChatUtil.sendMessage(player, "§7• Auto-pickup items when mining");
        ChatUtil.sendMessage(player, "§7• Auto-sell when backpack is full");
        ChatUtil.sendMessage(player, "§7• Upgradeable size and features");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lBackpack Commands:");
        ChatUtil.sendMessage(player, "§d/backpack §7- Open your backpack");
        ChatUtil.sendMessage(player, "§d/backpack sell §7- Sell all items in backpack");
        ChatUtil.sendMessage(player, "§d/backpack autosell §7- Toggle auto-sell feature");
        ChatUtil.sendMessage(player, "§d/backpack upgrade §7- Upgrade backpack size");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAuto-Sell:");
        ChatUtil.sendMessage(player, "§7• Automatically sells items when backpack fills up");
        ChatUtil.sendMessage(player, "§7• Saves time and maximizes mining efficiency");
        ChatUtil.sendMessage(player, "§7• Can be enabled/disabled per player preference");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§9§l§m                                                ");
    }

    private void showPickaxeHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§l§m            §r §6§lPICKAXE SYSTEM §r§6§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lPickaxe Progression:");
        ChatUtil.sendMessage(player, "§7• Gain EXP by mining blocks");
        ChatUtil.sendMessage(player, "§7• Level up to unlock better enchants");
        ChatUtil.sendMessage(player, "§7• Higher levels = access to more powerful enchants");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lEnchant Types:");
        ChatUtil.sendMessage(player, "§6§lToken Enchants §7- Purchased with tokens");
        ChatUtil.sendMessage(player, "§7  • Fortune, Efficiency, Auto-Sell, etc.");
        ChatUtil.sendMessage(player, "§5§lSoul Enchants §7- Purchased with souls");
        ChatUtil.sendMessage(player, "§7  • Rare and powerful effects");
        ChatUtil.sendMessage(player, "§7  • Unlocked through prestiging");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lPickaxe Commands:");
        ChatUtil.sendMessage(player, "§d/pickaxe §7- View your pickaxe info");
        ChatUtil.sendMessage(player, "§d/pickaxe enchants §7- View all available enchants");
        ChatUtil.sendMessage(player, "§d/pickaxe token §7- View token enchants");
        ChatUtil.sendMessage(player, "§d/pickaxe soul §7- View soul enchants");
        ChatUtil.sendMessage(player, "§d/pickaxe buy <enchant> §7- Purchase an enchant");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§6§l§m                                                ");
    }

    private void showPetsHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§2§l§m            §r §2§lPET SYSTEM §r§2§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lPet Features:");
        ChatUtil.sendMessage(player, "§7• Loyal companions that follow you around");
        ChatUtil.sendMessage(player, "§7• Provide passive bonuses and effects");
        ChatUtil.sendMessage(player, "§7• Level up through use and feeding");
        ChatUtil.sendMessage(player, "§7• Different pets have different abilities");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lPet Commands:");
        ChatUtil.sendMessage(player, "§d/pet §7- View your current pet");
        ChatUtil.sendMessage(player, "§d/pet list §7- See all available pets");
        ChatUtil.sendMessage(player, "§d/pet summon <pet> §7- Summon a specific pet");
        ChatUtil.sendMessage(player, "§d/pet dismiss §7- Dismiss your current pet");
        ChatUtil.sendMessage(player, "§d/pet feed §7- Feed your pet to level it up");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lTips:");
        ChatUtil.sendMessage(player, "§7• Different pets excel in different areas");
        ChatUtil.sendMessage(player, "§7• Higher level pets provide better bonuses");
        ChatUtil.sendMessage(player, "§7• Some pets are unlocked through progression");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§2§l§m                                                ");
    }

    private void showGangsHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l§m            §r §c§lGANG SYSTEM §r§c§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lGang Features:");
        ChatUtil.sendMessage(player, "§7• Create or join gangs with other players");
        ChatUtil.sendMessage(player, "§7• Shared gang bank and resources");
        ChatUtil.sendMessage(player, "§7• Gang challenges and competitions");
        ChatUtil.sendMessage(player, "§7• Gang-specific bonuses and multipliers");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lGang Commands:");
        ChatUtil.sendMessage(player, "§d/gang §7- View your gang information");
        ChatUtil.sendMessage(player, "§d/gang create <name> §7- Create a new gang");
        ChatUtil.sendMessage(player, "§d/gang join <gang> §7- Join an existing gang");
        ChatUtil.sendMessage(player, "§d/gang leave §7- Leave your current gang");
        ChatUtil.sendMessage(player, "§d/gang invite <player> §7- Invite a player to your gang");
        ChatUtil.sendMessage(player, "§d/gang bank §7- Access gang bank");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lGang Ranks:");
        ChatUtil.sendMessage(player, "§7• Leader - Full gang control");
        ChatUtil.sendMessage(player, "§7• Officer - Can invite and manage members");
        ChatUtil.sendMessage(player, "§7• Member - Basic gang participation");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l§m                                                ");
    }

    private void showMultipliersHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lMULTIPLIER SYSTEM §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lMultiplier Types:");
        ChatUtil.sendMessage(player, "§a§lMoney Multipliers §7- Increase money gains from mining");
        ChatUtil.sendMessage(player, "§6§lToken Multipliers §7- Increase token drops");
        ChatUtil.sendMessage(player, "§5§lSoul Multipliers §7- Increase soul earnings");
        ChatUtil.sendMessage(player, "§b§lGlobal Multipliers §7- Affect all currencies");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lSources of Multipliers:");
        ChatUtil.sendMessage(player, "§7• Prestigious - +10% money per prestige");
        ChatUtil.sendMessage(player, "§7• Rebirth - +50% money, +25% souls per rebirth");
        ChatUtil.sendMessage(player, "§7• Ascension - +100% money, +50% souls, +25% beacons");
        ChatUtil.sendMessage(player, "§7• Pickaxe enchants and perks");
        ChatUtil.sendMessage(player, "§7• Gang bonuses and events");
        ChatUtil.sendMessage(player, "§7• Temporary boost items");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lMultiplier Commands:");
        ChatUtil.sendMessage(player, "§d/multipliers §7- View your active multipliers");
        ChatUtil.sendMessage(player, "§d/boosts §7- See available boost items");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lTips:");
        ChatUtil.sendMessage(player, "§7• Multipliers stack additively");
        ChatUtil.sendMessage(player, "§7• Higher progression = better permanent multipliers");
        ChatUtil.sendMessage(player, "§7• Use boost items during intensive mining sessions");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                    ");
    }

    private void showAutoHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§3§l§m            §r §3§lAUTOMATION FEATURES §r§3§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAuto-Rankup:");
        ChatUtil.sendMessage(player, "§7• Automatically rank up when you have enough money");
        ChatUtil.sendMessage(player, "§7• Continues until you can't afford next rank");
        ChatUtil.sendMessage(player, "§7Commands: §d/autorankup §7or §d/rankupmax");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAuto-Prestige:");
        ChatUtil.sendMessage(player, "§7• Automatically prestige when at rank Z");
        ChatUtil.sendMessage(player, "§7• §c§lRequires money for each prestige");
        ChatUtil.sendMessage(player, "§7• Can be toggled on/off per player");
        ChatUtil.sendMessage(player, "§7Commands: §d/autoprestige §7or §d/prestigemax");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAuto-Rebirth:");
        ChatUtil.sendMessage(player, "§7• Automatically rebirth when requirements are met");
        ChatUtil.sendMessage(player, "§7• §c§lRequires significant money investment");
        ChatUtil.sendMessage(player, "§7• Requires 10+ prestiges to activate");
        ChatUtil.sendMessage(player, "§7Commands: §d/autorebirth §7or §d/rebirthmax");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAuto-Ascension:");
        ChatUtil.sendMessage(player, "§7• Automatically ascend when requirements are met");
        ChatUtil.sendMessage(player, "§7• §c§lRequires extreme money costs");
        ChatUtil.sendMessage(player, "§7• Requires 5+ rebirths to activate");
        ChatUtil.sendMessage(player, "§7Commands: §d/autoascension");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§lAuto-Sell:");
        ChatUtil.sendMessage(player, "§7• Automatically sell backpack when full");
        ChatUtil.sendMessage(player, "§7• Toggle through backpack settings");
        ChatUtil.sendMessage(player, "§7Commands: §d/backpack autosell");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§c§l⚠ WARNING: Auto-features will spend your money! ⚠");
        ChatUtil.sendMessage(player, "§7Make sure you have enough saved for desired progression!");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§3§l§m                                                ");
    }

    private void showCommandsHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§l§m            §r §f§lALL COMMANDS §r§f§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lCore Commands:");
        ChatUtil.sendMessage(player, "§d/help [category] §7- Show help information");
        ChatUtil.sendMessage(player, "§d/rank [player] §7- View rank information");
        ChatUtil.sendMessage(player, "§d/bal [currency] §7- Check balance");
        ChatUtil.sendMessage(player, "§d/pay <player> <amount> [currency] §7- Send money");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lProgression Commands:");
        ChatUtil.sendMessage(player, "§d/rankup §7- Rank up (costs money)");
        ChatUtil.sendMessage(player, "§d/rankupmax §7- Rank up as much as possible");
        ChatUtil.sendMessage(player, "§d/prestige §7- View prestige info (costs money)");
        ChatUtil.sendMessage(player, "§d/rebirth §7- View rebirth info (costs money)");
        ChatUtil.sendMessage(player, "§d/ascension §7- View ascension info (costs money)");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lMining Commands:");
        ChatUtil.sendMessage(player, "§d/mine §7- Show mine help menu");
        ChatUtil.sendMessage(player, "§d/mine go §7- Teleport to your mine");
        ChatUtil.sendMessage(player, "§d/visit <location> §7- Teleport to locations");
        ChatUtil.sendMessage(player, "§d/backpack §7- Open backpack");
        ChatUtil.sendMessage(player, "§d/pickaxe §7- View pickaxe information");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lSocial Commands:");
        ChatUtil.sendMessage(player, "§d/gang §7- Gang management");
        ChatUtil.sendMessage(player, "§d/pet §7- Pet management");
        ChatUtil.sendMessage(player, "§d/leaderboard §7- View leaderboards");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lUtility Commands:");
        ChatUtil.sendMessage(player, "§d/multipliers §7- View active multipliers");
        ChatUtil.sendMessage(player, "§d/stats §7- View your statistics");
        ChatUtil.sendMessage(player, "§d/milestones §7- Check progress milestones");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§l§m                                                    ");
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new HelpCommand());
    }
}