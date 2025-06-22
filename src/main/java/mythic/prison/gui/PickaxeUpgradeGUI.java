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
    
    public PickaxeUpgradeGUI(Player player, String enchantKey, PickaxeEnchant enchant, 
                           boolean isTokenEnchant, PickaxeEnchantGUI parentGUI) {
        super(player, "§e⬆ Upgrade " + enchant.getName(), InventoryType.CHEST_3_ROW);
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
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE);
        
        int currentLevel = isTokenEnchant ? 
            pickaxeManager.getTokenEnchantLevel(player, enchantKey) :
            pickaxeManager.getSoulEnchantLevel(player, enchantKey);
        
        // Check if already maxed
        if (currentLevel >= enchant.getMaxLevel()) {
            parentGUI.open();
            return;
        }
        
        // Enchant info (center)
        inventory.setItemStack(13, createEnchantInfoItem(currentLevel));
        
        // Upgrade button (left of center)
        inventory.setItemStack(11, createUpgradeButton(currentLevel));
        
        // Back button (right of center)
        inventory.setItemStack(15, createBackButton());
    }
    
    private ItemStack createEnchantInfoItem(int currentLevel) {
        Material material = isTokenEnchant ? Material.GOLD_BLOCK : Material.SOUL_SAND;
        String color = isTokenEnchant ? "§6" : "§5";
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + enchant.getDescription());
        lore.add("");
        lore.add("§7Current Level: §e" + currentLevel + "§7/§e" + enchant.getMaxLevel());
        lore.add("§7Next Level: §a" + (currentLevel + 1));
        
        return createItem(material, color + "§l" + enchant.getName(), lore);
    }
    
    private ItemStack createUpgradeButton(int currentLevel) {
        double cost = enchant.getCostForLevel(currentLevel + 1);
        String currency = isTokenEnchant ? "tokens" : "souls";
        double playerBalance = currencyManager.getBalance(player, currency);
        boolean canAfford = playerBalance >= cost;
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Upgrade Cost:");
        lore.add("  §e" + formatCurrency(cost) + " " + currency);
        lore.add("");
        lore.add("§7Your Balance:");
        lore.add("  §e" + formatCurrency(playerBalance) + " " + currency);
        lore.add("");
        
        if (canAfford) {
            lore.add("§a§l⇒ CLICK TO UPGRADE");
            return createItem(Material.EMERALD_BLOCK, "§a§lUpgrade Enchant", lore);
        } else {
            lore.add("§c§l✗ INSUFFICIENT FUNDS");
            lore.add("§7Need §c" + formatCurrency(cost - playerBalance) + " §7more");
            return createItem(Material.RED_CONCRETE, "§c§lCannot Upgrade", lore);
        }
    }
    
    private ItemStack createBackButton() {
        return createItem(Material.ARROW, "§c« Back", "§7Return to enchant menu");
    }
    
    @Override
    protected void handleClick(int slot, Click click) {
        if (slot == 11) {
            // Upgrade button
            handleUpgradeClick();
        } else if (slot == 15) {
            // Back button
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
                formatCurrency(cost) + " " + currency + ".");
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
            
            // Show success messages
            ChatUtil.sendMessage(player, "§a§l✦ ENCHANT UPGRADED!");
            ChatUtil.sendMessage(player, "§e" + enchant.getName() + " §7is now level §a" + newLevel + "§7!");
            
            // Check if maxed
            if (newLevel >= enchant.getMaxLevel()) {
                ChatUtil.sendMessage(player, "§6✦ " + enchant.getName() + " is now MAX LEVEL! ✦");
                parentGUI.open();
                return;
            }
            
            // Refresh GUI if not maxed
            populateItems();
        } else {
            // Refund on failure
            currencyManager.addBalance(player, currency, cost);
            ChatUtil.sendError(player, "Failed to upgrade enchant!");
        }
    }
    
    private String formatCurrency(double cost) {
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