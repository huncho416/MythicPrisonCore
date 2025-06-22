package mythic.prison.gui;

import mythic.prison.MythicPrison;
import mythic.prison.data.enchants.PickaxeEnchant;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.managers.PickaxeManager;
import mythic.prison.managers.PickaxeEffectsManager;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PickaxeEnchantGUI extends BaseGUI {
    
    private final PickaxeManager pickaxeManager;
    private final CurrencyManager currencyManager;
    private final PickaxeEffectsManager effectsManager;
    private boolean showingTokenEnchants = true;
    
    public PickaxeEnchantGUI(Player player) {
        super(player, "§d§lPickaxe Enchants", InventoryType.CHEST_6_ROW);
        this.pickaxeManager = MythicPrison.getInstance().getPickaxeManager();
        this.currencyManager = MythicPrison.getInstance().getCurrencyManager();
        this.effectsManager = MythicPrison.getInstance().getPickaxeEffectsManager();
    }
    
    @Override
    protected void populateItems() {
        // Clear inventory
        inventory.clear();
        
        // Toggle buttons for Token/Soul enchants
        inventory.setItemStack(3, createToggleItem(Material.GOLD_BLOCK, "§6§lToken Enchants", showingTokenEnchants));
        inventory.setItemStack(5, createToggleItem(Material.SOUL_SAND, "§5§lSoul Enchants", !showingTokenEnchants));
        
        // DO NOT add a back button here - this is the main menu
        // Remove any existing back button code from slot 49 or other slots
        
        // Populate enchants
        populateEnchants();
        
        // Fill empty slots
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE);
    }

    private ItemStack createBackButton() {
        return createItem(Material.ARROW, "§e§lBack", "§7Close enchant menu");
    }
    
    private ItemStack createToggleItem(Material material, String name, boolean isActive) {
        List<String> lore = new ArrayList<>();
        if (isActive) {
            lore.add("§a§lACTIVE");
            lore.add("§7Currently viewing this category");
        } else {
            lore.add("§c§lINACTIVE");
            lore.add("§7Click to switch to this category");
        }
        return createItem(material, name, lore);
    }
    
    private void populateEnchants() {
        Map<String, PickaxeEnchant> enchants = showingTokenEnchants ? 
            pickaxeManager.getTokenEnchants() : pickaxeManager.getSoulEnchants();
    
        int row = 2; // Start from third row (index 2)
        int col = 1; // Start from second column (index 1) to avoid first column
        
        for (Map.Entry<String, PickaxeEnchant> entry : enchants.entrySet()) {
            // Calculate slot position: row * 9 + col
            int slot = row * 9 + col;
            
            // Make sure we don't exceed inventory bounds
            if (slot >= inventory.getSize()) break;
            
            PickaxeEnchant enchant = entry.getValue();
            int currentLevel = showingTokenEnchants ? 
                pickaxeManager.getTokenEnchantLevel(player, entry.getKey()) :
                pickaxeManager.getSoulEnchantLevel(player, entry.getKey());
            
            ItemStack enchantItem = createEnchantItem(enchant, currentLevel);
            inventory.setItemStack(slot, enchantItem);
            
            // Move to next position
            col++;
            if (col >= 8) { // Stop at column 7 (index 7) to avoid last column (index 8)
                col = 1; // Reset to second column
                row++; // Move to next row
            }
        }
    }
    
    private ItemStack createEnchantItem(PickaxeEnchant enchant, int currentLevel) {
        Material material = getEnchantMaterial(enchant);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + enchant.getDescription());
        lore.add("");
        
        // Add percentage chance if it's a chance-based enchant
        if (isChanceBasedEnchant(enchant.getId())) {
            double chance = effectsManager.getEnchantChance(enchant.getId(), Math.max(currentLevel, 1));
            lore.add("§e⚡ Activation Chance: §a" + String.format("%.1f", chance) + "%");
            lore.add("");
        } else if (isPassiveEnchant(enchant.getId())) {
            lore.add("§a✓ Passive Effect (Always Active)");
            lore.add("");
        }
        
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

        String enchantColor = getEnchantColor(enchant);
        return createItem(material, enchantColor + "§l" + enchant.getName(), lore);
    }

    private boolean isChanceBasedEnchant(String enchantId) {
        return switch (enchantId.toLowerCase()) {
            case "fortune", "explosive", "explosion", "mega_explosive", "telepathy", "magnet",
                 "auto_smelt", "smelting", "experience", "tokenator", "auto_sell",
                 "soul_extraction", "soulextraction", "super_fortune", "void_walker", "time_warp" -> true;
            default -> false;
        };
    }

    private boolean isPassiveEnchant(String enchantId) {
        return switch (enchantId.toLowerCase()) {
            case "efficiency", "speed", "haste" -> true;
            default -> false;
        };
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
                // Fallback to original logic for unknown enchants
                return showingTokenEnchants ? Material.GOLD_BLOCK : Material.SOUL_SAND;
        }
    }
    
    @Override
    protected void handleClick(int slot, Click click) {
        // DO NOT handle back button clicks here - this is the main menu
        
        // Handle toggle buttons
        if (slot == 3) {
            showingTokenEnchants = true;
            populateItems();
            return;
        } else if (slot == 5) {
            showingTokenEnchants = false;
            populateItems();
            return;
        }
        
        // Handle enchant clicks
        handleEnchantClick(slot);
    }
    
    private void handleEnchantClick(int slot) {
        Map<String, PickaxeEnchant> enchants = showingTokenEnchants ? 
            pickaxeManager.getTokenEnchants() : pickaxeManager.getSoulEnchants();
        
        // Calculate which enchant was clicked based on the new grid system
        int row = slot / 9;
        int col = slot % 9;
        
        // Only handle clicks in valid enchant area (rows 2+, columns 1-7)
        if (row < 2 || col < 1 || col > 7) return;
        
        // Calculate enchant index based on grid position
        int enchantIndex = (row - 2) * 7 + (col - 1);
        String[] enchantKeys = enchants.keySet().toArray(new String[0]);
        
        if (enchantIndex >= 0 && enchantIndex < enchantKeys.length) {
            String enchantKey = enchantKeys[enchantIndex];
            PickaxeEnchant enchant = enchants.get(enchantKey);
            
            int currentLevel = showingTokenEnchants ? 
                pickaxeManager.getTokenEnchantLevel(player, enchantKey) :
                pickaxeManager.getSoulEnchantLevel(player, enchantKey);
            
            if (currentLevel < enchant.getMaxLevel()) {
                new PickaxeBulkUpgradeGUI(player, enchantKey, enchant, showingTokenEnchants, this).open();
            }
        }
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