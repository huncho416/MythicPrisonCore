package mythic.prison.gui;

import mythic.prison.MythicPrison;
import mythic.prison.data.enchants.PickaxeEnchant;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.managers.PickaxeManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class PickaxeUpgradeGUI extends BaseGUI {
    
    private final PickaxeManager pickaxeManager;
    private final CurrencyManager currencyManager;
    private final String enchantKey;
    private final PickaxeEnchant enchant;
    private final boolean isTokenEnchant;
    private final PickaxeEnchantGUI parentGUI;
    
private boolean showingTokenEnchants = true; // Add this to your PickaxeUpgradeGUI class
    
    public PickaxeUpgradeGUI(Player player, String enchantKey, PickaxeEnchant enchant, 
                           boolean isTokenEnchant, PickaxeEnchantGUI parentGUI) {
        super(player, "§d§lUpgrade " + enchant.getName(), InventoryType.CHEST_3_ROW);
        this.pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
        this.currencyManager = MythicPrison.getInstance().getCurrencyManager();
        this.enchantKey = enchantKey;
        this.enchant = enchant;
        this.isTokenEnchant = isTokenEnchant;
        this.parentGUI = parentGUI;
    }
    
    @Override
    protected void populateItems() {
        // Clear inventory
        inventory.clear();
        
        int currentLevel = isTokenEnchant ? 
            pickaxeManager.getTokenEnchantLevel(player, enchantKey) :
            pickaxeManager.getSoulEnchantLevel(player, enchantKey);
        
        // Check if enchant is already maxed
        if (currentLevel >= enchant.getMaxLevel()) {
            // Enchant is maxed, return to main menu
            parentGUI.open();
            return;
        }
        
        // Enchant info item
        inventory.setItemStack(13, createEnchantInfoItem(currentLevel));
        
        // Upgrade button
        inventory.setItemStack(11, createUpgradeButton(currentLevel));
        
        // Back button
        inventory.setItemStack(15, createBackButton());
        
        // Fill empty slots
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE);
    }
    
    private ItemStack createEnchantInfoItem(int currentLevel) {
        Material material = isTokenEnchant ? Material.GOLD_BLOCK : Material.SOUL_SAND;
        String color = isTokenEnchant ? "§6" : "§5";
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + enchant.getDescription());
        lore.add("");
        lore.add("§f§lCurrent Level: §a" + currentLevel + "§7/§a" + enchant.getMaxLevel());
        
        return createItem(material, color + "§l" + enchant.getName(), lore);
    }
    
    private ItemStack createUpgradeButton(int currentLevel) {
        if (currentLevel >= enchant.getMaxLevel()) {
            return createItem(Material.RED_CONCRETE, "§c§lMAX LEVEL", 
                "§7This enchant is already maxed!");
        }
        
        double cost = enchant.getCostForLevel(currentLevel + 1);
        String currency = isTokenEnchant ? "tokens" : "souls";
        String currencyColor = currencyManager.getCurrencyColor(currency);
        
        double playerBalance = currencyManager.getBalance(player, currency);
        
        boolean canAfford = playerBalance >= cost;
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Cost: " + currencyColor + formatCost(cost) + " " + currency);
        lore.add("");
        lore.add("§7Your Balance: " + currencyColor + formatCost(playerBalance) + " " + currency);
        lore.add("");
        
        if (canAfford) {
            lore.add("§a§lClick to upgrade!");
            return createItem(Material.EMERALD_BLOCK, "§a§lUpgrade", lore);
        } else {
            lore.add("§c§lInsufficient " + currency + "!");
            return createItem(Material.RED_CONCRETE, "§c§lCannot Upgrade", lore);
        }
    }
    
    private ItemStack createBackButton() {
        return createItem(Material.ARROW, "§e§lBack", "§7Return to enchant menu");
    }
    
    @Override
    protected void handleClick(int slot, Click click) {
        if (slot == 11) {
            handleUpgradeClick();
        } else if (slot == 15) {
            parentGUI.open();
        }
    }
    
    private void handleUpgradeClick() {
        int currentLevel = isTokenEnchant ? 
            pickaxeManager.getTokenEnchantLevel(player, enchantKey) :
            pickaxeManager.getSoulEnchantLevel(player, enchantKey);
        
        if (currentLevel >= enchant.getMaxLevel()) {
            ChatUtil.sendError(player, "This enchant is already at maximum level!");
            return;
        }
        
        double cost = enchant.getCostForLevel(currentLevel + 1);
        String currency = isTokenEnchant ? "tokens" : "souls";
        
        double playerBalance = currencyManager.getBalance(player, currency);
        
        if (playerBalance < cost) {
            ChatUtil.sendError(player, "Insufficient " + currency + "! You need " + 
                formatCost(cost) + " " + currency + ".");
            return;
        }
        
        // Deduct currency
        currencyManager.removeBalance(player, currency, cost);
        
        // Add enchant level
        boolean success = isTokenEnchant ? 
            pickaxeManager.addTokenEnchant(player, enchantKey, 1) :
            pickaxeManager.addSoulEnchant(player, enchantKey, 1);
        
        if (success) {
            int newLevel = currentLevel + 1;
            ChatUtil.sendMessage(player, "§a§l✦ ENCHANT UPGRADED!");
            ChatUtil.sendMessage(player, "§e" + enchant.getName() + " §7is now level §a" + newLevel + "§7!");
        
            // Check if enchant is now maxed
            if (newLevel >= enchant.getMaxLevel()) {
                player.sendMessage("§6✦ " + enchant.getName() + " is now MAX LEVEL! ✦");
                
                // Immediate return to parent GUI without any delay
                parentGUI.open();
                return;
            }
        
            // Refresh this GUI if not maxed
            populateItems();
        } else {
            ChatUtil.sendError(player, "Failed to upgrade enchant!");
            // Refund the currency if upgrade failed
            currencyManager.addBalance(player, currency, cost);
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

private int calculateMaxAffordable(int currentLevel, double balance) {
    int maxPossible = enchant.getMaxLevel() - currentLevel;
    double totalCost = 0;

    for (int i = 1; i <= maxPossible; i++) {
        totalCost += enchant.getCostForLevel(currentLevel + i);
        if (totalCost > balance) {
            return i - 1;
        }
    }

    return maxPossible;
}

private String getEnchantColor(PickaxeEnchant enchant) {
    String enchantName = enchant.getName().toLowerCase();
    
    switch (enchantName) {
        // Token Enchants - Warm colors
        case "efficiency":
            return "§b"; // Aqua
        case "fortune":
            return "§a"; // Green
        case "explosion":
            return "§c"; // Red
        case "special":
            return "§d"; // Light Purple
        case "auto sell":
        case "auto_sell":
            return "§e"; // Yellow
        case "token finder":
        case "token_finder":
            return "§6"; // Gold
        case "experience":
            return "§2"; // Dark Green
        case "speed":
            return "§f"; // White
        case "haste":
            return "§9"; // Blue
        
        // Soul Enchants - Dark/mystical colors
        case "soul collector":
        case "soul_collector":
            return "§8"; // Dark Gray
        case "super fortune":
        case "super_fortune":
            return "§a"; // Green
        case "mega explosion":
        case "mega_explosion":
            return "§4"; // Dark Red
        case "divine touch":
        case "divine_touch":
            return "§b"; // Aqua
        case "ethereal efficiency":
        case "ethereal_efficiency":
            return "§3"; // Dark Aqua
        case "cosmic fortune":
        case "cosmic_fortune":
            return "§5"; // Dark Purple
        case "soul stealer":
        case "soul_stealer":
            return "§0"; // Black
        case "void walker":
        case "void_walker":
            return "§8"; // Dark Gray
        case "phantom strike":
        case "phantom_strike":
            return "§7"; // Gray
        case "shadow realm":
        case "shadow_realm":
            return "§0"; // Black
        case "soul burn":
        case "soul_burn":
            return "§c"; // Red
        case "spirit link":
        case "spirit_link":
            return "§b"; // Aqua
        case "dark magic":
        case "dark_magic":
            return "§5"; // Dark Purple
        case "necromancy":
            return "§8"; // Dark Gray
        case "soul harvest":
        case "soul_harvest":
            return "§2"; // Dark Green
        case "void efficiency":
        case "void_efficiency":
            return "§8"; // Dark Gray
        case "cursed fortune":
        case "cursed_fortune":
            return "§4"; // Dark Red
        case "infernal blast":
        case "infernal_blast":
            return "§c"; // Red
        
        default:
            // Fallback to original logic
            return showingTokenEnchants ? "§6" : "§5";
    }
}

private Material getEnchantMaterial(PickaxeEnchant enchant) {
    // Map each enchant to a unique material based on the enchant's name
    String enchantName = enchant.getName().toLowerCase();
    
    switch (enchantName) {
        // Token Enchants
        case "efficiency":
            return Material.DIAMOND_PICKAXE;
        case "fortune":
            return Material.EMERALD;
        case "explosion":
            return Material.TNT;
        case "special":
            return Material.NETHER_STAR;
        case "auto sell":
        case "auto_sell":
            return Material.CHEST;
        case "token finder":
        case "token_finder":
            return Material.GOLD_INGOT;
        case "experience":
            return Material.EXPERIENCE_BOTTLE;
        case "speed":
            return Material.SUGAR;
        case "haste":
            return Material.CLOCK;
        
        // Soul Enchants
        case "soul collector":
        case "soul_collector":
            return Material.SOUL_SAND;
        case "super fortune":
        case "super_fortune":
            return Material.EMERALD_BLOCK;
        case "mega explosion":
        case "mega_explosion":
            return Material.TNT_MINECART;
        case "divine touch":
        case "divine_touch":
            return Material.ENCHANTED_BOOK;
        case "ethereal efficiency":
        case "ethereal_efficiency":
            return Material.DIAMOND;
        case "cosmic fortune":
        case "cosmic_fortune":
            return Material.BEACON;
        case "soul stealer":
        case "soul_stealer":
            return Material.WITHER_SKELETON_SKULL;
        case "void walker":
        case "void_walker":
            return Material.END_PORTAL_FRAME;
        case "phantom strike":
        case "phantom_strike":
            return Material.PHANTOM_MEMBRANE;
        case "shadow realm":
        case "shadow_realm":
            return Material.OBSIDIAN;
        case "soul burn":
        case "soul_burn":
            return Material.REDSTONE;
        case "spirit link":
        case "spirit_link":
            return Material.SOUL_LANTERN;
        case "dark magic":
        case "dark_magic":
            return Material.WITHER_ROSE;
        case "necromancy":
            return Material.ZOMBIE_HEAD;
        case "soul harvest":
        case "soul_harvest":
            return Material.SOUL_SOIL;
        case "void efficiency":
        case "void_efficiency":
            return Material.NETHERITE_PICKAXE;
        case "cursed fortune":
        case "cursed_fortune":
            return Material.CRYING_OBSIDIAN;
        case "infernal blast":
        case "infernal_blast":
            return Material.RESPAWN_ANCHOR;
        
        default:
            // Fallback - use a default material
            return Material.GOLD_BLOCK;
    }
}

private ItemStack createEnchantItem(PickaxeEnchant enchant, int currentLevel) {
    Material material = getEnchantMaterial(enchant);
    String enchantColor = getEnchantColor(enchant);
    
    List<String> lore = new ArrayList<>();
    lore.add("§7" + enchant.getDescription());
    lore.add("");
    lore.add("§f§lCurrent Level: §a" + currentLevel + "§7/§a" + enchant.getMaxLevel());
    
    if (currentLevel < enchant.getMaxLevel()) {
        double cost = enchant.getCostForLevel(currentLevel + 1);
        String currency = showingTokenEnchants ? "tokens" : "souls";
        
        lore.add("§f§lNext Level Cost: §e" + formatCost(cost) + " " + currency);
        lore.add("");
        lore.add("§a§lClick to upgrade!");
    } else {
        lore.add("");
        lore.add("§c§lMAX LEVEL");
    }
    
    return createItem(material, enchantColor + "§l" + enchant.getName(), lore);
}
}