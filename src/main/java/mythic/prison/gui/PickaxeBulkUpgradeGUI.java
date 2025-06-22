package mythic.prison.gui;

import mythic.prison.MythicPrison;
import mythic.prison.data.enchants.PickaxeEnchant;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.managers.PickaxeManager;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class PickaxeBulkUpgradeGUI extends BaseGUI {

    private final PickaxeManager pickaxeManager;
    private final CurrencyManager currencyManager;
    private final String enchantKey;
    private final PickaxeEnchant enchant;
    private final boolean isTokenEnchant;
    private final PickaxeEnchantGUI parentGUI;

    private final int[] quantities = {1, 5, 10, 25, 50, 100, 250, 500, 1000};

    public PickaxeBulkUpgradeGUI(Player player, String enchantKey, PickaxeEnchant enchant,
                                 boolean isTokenEnchant, PickaxeEnchantGUI parentGUI) {
        super(player, "§d§lUpgrade " + enchant.getName(), InventoryType.CHEST_6_ROW);
        this.pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
        this.currencyManager = MythicPrison.getInstance().getCurrencyManager();
        this.enchantKey = enchantKey;
        this.enchant = enchant;
        this.isTokenEnchant = isTokenEnchant;
        this.parentGUI = parentGUI;
    }

    @Override
    protected void populateItems() {
        inventory.clear();

        int currentLevel = isTokenEnchant ?
                pickaxeManager.getTokenEnchantLevel(player, enchantKey) :
                pickaxeManager.getSoulEnchantLevel(player, enchantKey);

        // Enchant info item
        inventory.setItemStack(4, createEnchantInfoItem(currentLevel));

        // Quantity buttons
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29}; // Bottom two rows

        for (int i = 0; i < quantities.length && i < slots.length; i++) {
            inventory.setItemStack(slots[i], createQuantityButton(quantities[i], currentLevel));
        }

        // Max button
        inventory.setItemStack(31, createMaxButton(currentLevel));

        // Back button
        inventory.setItemStack(49, createBackButton());

        // Fill empty slots
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE);
    }

    private ItemStack createEnchantInfoItem(int currentLevel) {
        Material material = getEnchantMaterial(enchant);
        String color = isTokenEnchant ? "§6" : "§5";
        String currency = isTokenEnchant ? "tokens" : "souls";

        List<String> lore = new ArrayList<>();
        lore.add("§7" + enchant.getDescription());
        lore.add("");
        lore.add("§f§lCurrent Level: §a" + currentLevel + "§7/§a" + enchant.getMaxLevel());

        if (currentLevel < enchant.getMaxLevel()) {
            double nextCost = enchant.getCostForLevel(currentLevel + 1);
            lore.add("§f§lNext Level Cost: §e" + formatCost(nextCost) + " " + currency);
        } else {
            lore.add("§c§lMAX LEVEL REACHED!");
        }

        double balance = currencyManager.getBalance(player, currency);
        String currencyColor = currencyManager.getCurrencyColor(currency);
        lore.add("");
        lore.add("§f§lYour Balance: " + currencyColor + formatCost(balance) + " " + currency);

        return createItem(material, color + "§l" + enchant.getName(), lore);
    }

    private ItemStack createQuantityButton(int quantity, int currentLevel) {
        if (currentLevel >= enchant.getMaxLevel()) {
            return createItem(Material.RED_DYE, "§c§l+" + quantity, "§7Enchant is already maxed!");
        }

        int maxPossible = Math.min(quantity, enchant.getMaxLevel() - currentLevel);
        double totalCost = calculateTotalCost(currentLevel, maxPossible);
        String currency = isTokenEnchant ? "tokens" : "souls";
        String currencyColor = currencyManager.getCurrencyColor(currency);

        double balance = currencyManager.getBalance(player, currency);
        boolean canAfford = balance >= totalCost;

        List<String> lore = new ArrayList<>();
        lore.add("§7Upgrade by §a" + maxPossible + " §7levels");
        lore.add("");
        lore.add("§7Total Cost: " + currencyColor + formatCost(totalCost) + " " + currency);
        lore.add("§7Your Balance: " + currencyColor + formatCost(balance) + " " + currency);
        lore.add("");

        if (maxPossible < quantity) {
            lore.add("§e§l⚠ Limited to " + maxPossible + " levels");
            lore.add("");
        }

        Material material;
        String title;
        if (canAfford) {
            material = Material.GREEN_DYE;
            title = "§a§l+" + quantity;
            lore.add("§a§lClick to upgrade!");
        } else {
            material = Material.RED_DYE;
            title = "§c§l+" + quantity;
            lore.add("§c§lInsufficient " + currency + "!");
        }

        return createItem(material, title, lore);
    }

    private ItemStack createMaxButton(int currentLevel) {
        if (currentLevel >= enchant.getMaxLevel()) {
            return createItem(Material.HOPPER, "§c§lMAX", "§7Enchant is already maxed!");
        }

        String currency = isTokenEnchant ? "tokens" : "souls";
        String currencyColor = currencyManager.getCurrencyColor(currency);
        double balance = currencyManager.getBalance(player, currency);

        int maxAffordable = calculateMaxAffordable(currentLevel, balance);
        double totalCost = calculateTotalCost(currentLevel, maxAffordable);

        List<String> lore = new ArrayList<>();
        lore.add("§7Upgrade to maximum possible level");
        lore.add("");
        lore.add("§7Levels to gain: §a" + maxAffordable);
        lore.add("§7Total Cost: " + currencyColor + formatCost(totalCost) + " " + currency);
        lore.add("§7Your Balance: " + currencyColor + formatCost(balance) + " " + currency);
        lore.add("");

        if (maxAffordable > 0) {
            return createItem(Material.NETHER_STAR, "§d§lMAX UPGRADE", lore.toArray(new String[0]));
        } else {
            lore.add("§c§lCannot afford any upgrades!");
            return createItem(Material.BARRIER, "§c§lMAX UPGRADE", lore);
        }
    }

    private ItemStack createBackButton() {
        return createItem(Material.ARROW, "§c« Back", "§7Return to enchant menu");
    }

    @Override
    protected void handleClick(int slot, Click click) {
        if (slot == 49) {
            // Back button
            parentGUI.open();
            return;
        }

        int currentLevel = isTokenEnchant ?
                pickaxeManager.getTokenEnchantLevel(player, enchantKey) :
                pickaxeManager.getSoulEnchantLevel(player, enchantKey);

        if (currentLevel >= enchant.getMaxLevel()) {
            player.sendMessage("§c§lThis enchant is already at maximum level!");
            return;
        }

        // Handle quantity buttons
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29};
        for (int i = 0; i < slots.length && i < quantities.length; i++) {
            if (slot == slots[i]) {
                handleUpgrade(quantities[i], currentLevel);
                return;
            }
        }

        // Handle max button
        if (slot == 31) {
            handleMaxUpgrade(currentLevel);
        }
    }

    private void handleUpgrade(int quantity, int currentLevel) {
        int maxPossible = Math.min(quantity, enchant.getMaxLevel() - currentLevel);
        if (maxPossible <= 0) {
            player.sendMessage("§c§lCannot upgrade this enchant further!");
            return;
        }

        double totalCost = calculateTotalCost(currentLevel, maxPossible);
        String currency = isTokenEnchant ? "tokens" : "souls";
        double balance = currencyManager.getBalance(player, currency);

        if (balance < totalCost) {
            player.sendMessage("§c§lInsufficient " + currency + "! You need " + formatCost(totalCost) + " " + currency + ".");
            return;
        }

        // Deduct currency
        currencyManager.removeBalance(player, currency, totalCost);

        // Add enchant levels
        boolean success = isTokenEnchant ?
                pickaxeManager.addTokenEnchant(player, enchantKey, maxPossible) :
                pickaxeManager.addSoulEnchant(player, enchantKey, maxPossible);

        if (success) {
            player.sendMessage("§a§l✦ ENCHANT UPGRADED!");
            player.sendMessage("§e" + enchant.getName() + " §7upgraded by §a" + maxPossible + " §7levels!");

            // Check if maxed
            int newLevel = currentLevel + maxPossible;
            if (newLevel >= enchant.getMaxLevel()) {
                player.sendMessage("§6✦ " + enchant.getName() + " is now MAX LEVEL! ✦");
            }

            // Refresh GUI
            populateItems();
        } else {
            // Refund on failure
            currencyManager.addBalance(player, currency, totalCost);
            player.sendMessage("§c§lFailed to upgrade enchant!");
        }
    }

    private void handleMaxUpgrade(int currentLevel) {
        String currency = isTokenEnchant ? "tokens" : "souls";
        double balance = currencyManager.getBalance(player, currency);

        int maxAffordable = calculateMaxAffordable(currentLevel, balance);
        if (maxAffordable <= 0) {
            player.sendMessage("§c§lYou cannot afford any upgrades!");
            return;
        }

        handleUpgrade(maxAffordable, currentLevel);
    }

    private double calculateTotalCost(int fromLevel, int levels) {
        double total = 0;
        for (int i = 0; i < levels; i++) {
            total += enchant.getCostForLevel(fromLevel + i + 1);
        }
        return total;
    }

    private int calculateMaxAffordable(int currentLevel, double balance) {
        int affordable = 0;
        double runningCost = 0;

        for (int level = currentLevel + 1; level <= enchant.getMaxLevel(); level++) {
            double levelCost = enchant.getCostForLevel(level);
            if (runningCost + levelCost <= balance) {
                runningCost += levelCost;
                affordable++;
            } else {
                break;
            }
        }

        return affordable;
    }

    // Add this method to get the enchant material
    private Material getEnchantMaterial(PickaxeEnchant enchant) {
        String enchantName = enchant.getName().toLowerCase();
        
        switch (enchantName) {
            // Token Enchants
            case "efficiency":
                return Material.DIAMOND_PICKAXE;
            case "fortune":
                return Material.EMERALD;
            case "explosive":
                return Material.TNT;
            case "speed":
                return Material.SUGAR;
            case "haste":
                return Material.CLOCK;
            case "magnet":
                return Material.IRON_INGOT; // Changed from Material.MAGNET to Material.IRON_INGOT
            case "auto sell":
            case "auto_sell":
                return Material.CHEST;
            
            // Soul Enchants
            case "super fortune":
            case "super_fortune":
                return Material.EMERALD_BLOCK;
            case "mega explosive":
            case "mega_explosive":
                return Material.TNT_MINECART;
            case "auto smelt":
            case "auto_smelt":
                return Material.FURNACE;
            case "void walker":
            case "void_walker":
                return Material.END_PORTAL_FRAME;
            case "time warp":
            case "time_warp":
                return Material.CLOCK;
            
            default:
                // Fallback based on enchant type
                return isTokenEnchant ? Material.GOLD_INGOT : Material.SOUL_SAND;
        }
    }

    private String formatCost(double cost) {
        if (cost >= 1_000_000_000) {
            return String.format("%.1fB", cost / 1_000_000_000);
        } else if (cost >= 1_000_000) {
            return String.format("%.1fM", cost / 1_000_000);
        } else if (cost >= 1_000) {
            return String.format("%.1fK", cost / 1_000);
        } else {
            return String.format("%.0f", cost);
        }
    }
}