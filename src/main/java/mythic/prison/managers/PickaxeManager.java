package mythic.prison.managers;

import mythic.prison.data.enchants.PickaxeEnchant;
import mythic.prison.data.enchants.TokenEnchant;
import mythic.prison.data.enchants.SoulEnchant;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.tag.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PickaxeManager {

    // Tags for pickaxe data
    private static final Tag<Integer> PICKAXE_LEVEL_TAG = Tag.Integer("pickaxe_level");
    private static final Tag<Long> PICKAXE_EXP_TAG = Tag.Long("pickaxe_exp");
    private static final Tag<Boolean> SOULBOUND_TAG = Tag.Boolean("soulbound");

    // Player data storage
    private final Map<String, Integer> playerPickaxeLevels = new ConcurrentHashMap<>();
    private final Map<String, Long> playerPickaxeExp = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> playerTokenEnchants = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> playerSoulEnchants = new ConcurrentHashMap<>();
    private final Map<String, Long> playerBlocksMined = new ConcurrentHashMap<>();

    // Enchants configuration
    private final Map<String, PickaxeEnchant> tokenEnchants = new HashMap<>();
    private final Map<String, PickaxeEnchant> soulEnchants = new HashMap<>();

    public PickaxeManager() {
        // Initialize with default values
        initializeTokenEnchants();
        initializeSoulEnchants();
        System.out.println("[PickaxeManager] Manager initialized");
    }

    // Update the initializeTokenEnchants method with descriptions
    private void initializeTokenEnchants() {
        // Initialize available token enchants
        tokenEnchants.put("efficiency", new TokenEnchant("Efficiency", "efficiency", 5, 50, 1.5));
        tokenEnchants.put("fortune", new TokenEnchant("Fortune", "fortune", 3, 100, 1.5));
        tokenEnchants.put("explosive", new TokenEnchant("Explosive", "explosive", 3, 200, 1.5));
        tokenEnchants.put("speed", new TokenEnchant("Speed", "speed", 3, 75, 1.5));
        tokenEnchants.put("haste", new TokenEnchant("Haste", "haste", 2, 150, 1.5));
        tokenEnchants.put("magnet", new TokenEnchant("Magnet", "magnet", 1, 125, 1.5));
        tokenEnchants.put("auto_sell", new TokenEnchant("Auto Sell", "auto_sell", 1, 300, 1.5));

        System.out.println("[PickaxeManager] Initialized " + tokenEnchants.size() + " token enchants");
    }

    // Update the initializeSoulEnchants method with descriptions
    private void initializeSoulEnchants() {
        // Initialize available soul enchants (more expensive, more powerful)
        soulEnchants.put("super_fortune", new SoulEnchant("Super Fortune", "super_fortune", 3, 1000, 2.0));
        soulEnchants.put("mega_explosive", new SoulEnchant("Mega Explosive", "mega_explosive", 2, 2000, 2.0));
        soulEnchants.put("auto_smelt", new SoulEnchant("Auto Smelt", "auto_smelt", 1, 1500, 2.0));
        soulEnchants.put("void_walker", new SoulEnchant("Void Walker", "void_walker", 1, 2500, 2.0));
        soulEnchants.put("time_warp", new SoulEnchant("Time Warp", "time_warp", 2, 3000, 2.0));

        System.out.println("[PickaxeManager] Initialized " + soulEnchants.size() + " soul enchants");
    }

    public void initializePlayer(Player player) {
        String playerUUID = player.getUuid().toString();

        // Initialize with default values if not present
        playerPickaxeLevels.putIfAbsent(playerUUID, 1);
        playerPickaxeExp.putIfAbsent(playerUUID, 0L);
        playerBlocksMined.putIfAbsent(playerUUID, 0L); // Add this line
        playerTokenEnchants.putIfAbsent(playerUUID, new HashMap<>());
        playerSoulEnchants.putIfAbsent(playerUUID, new HashMap<>());

        // Removed the console message: System.out.println("[PickaxeManager] Initialized player: " + player.getUsername());
    }

    public void initializePlayerExpBar(Player player) {
        try {
            // Initialize player data first
            initializePlayer(player);
            
            // Update the player's experience bar to show pickaxe progress
            updatePlayerExpBar(player);
            
            System.out.println("[PickaxeManager] Initialized exp bar for player: " + player.getUsername());
        } catch (Exception e) {
            System.err.println("[PickaxeManager] Error initializing exp bar for " + player.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePlayerExpBar(Player player) {
        try {
            int currentLevel = getPickaxeLevel(player);
            long currentExp = getPickaxeExp(player);
            long expRequired = getExpRequired(currentLevel);
            
            // Calculate progress (0.0 to 1.0)
            float progress = Math.min(1.0f, (float) currentExp / expRequired);
            
            // Set player's experience bar to show pickaxe progress
            player.setLevel(currentLevel);
            player.setExp(progress);
            
        } catch (Exception e) {
            System.err.println("[PickaxeManager] Error updating exp bar for " + player.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void giveSoulboundPickaxe(Player player) {
        try {
            // Ensure pickaxe data exists
            initializePlayer(player);

            // Create the soulbound pickaxe with player's name
            ItemStack pickaxe = ItemStack.builder(Material.DIAMOND_PICKAXE)
                    .customName(Component.text("§8[§d§l" + player.getUsername() + "'s §f§lPickaxe§8]"))
                    .lore(buildPickaxeLore(player))
                    .build();

        // Add tags to identify this as a soulbound pickaxe
        pickaxe = pickaxe.withTag(PICKAXE_LEVEL_TAG, getPickaxeLevel(player))
                .withTag(PICKAXE_EXP_TAG, getPickaxeExp(player))
                .withTag(SOULBOUND_TAG, true);

        // Always place in slot 0 (first slot)
        player.getInventory().setItemStack(0, pickaxe);

        // Removed the console message: System.out.println("[PickaxeManager] Gave soulbound pickaxe to: " + player.getUsername());

} catch (Exception e) {
    System.err.println("[PickaxeManager] Error giving soulbound pickaxe to " + player.getUsername() + ": " + e.getMessage());
    e.printStackTrace();
}
}

private java.util.List<Component> buildPickaxeLore(Player player) {
    java.util.List<Component> lore = new java.util.ArrayList<>();
    
    // Right under the name - Right click instruction
    lore.add(Component.text("§7Right click your pickaxe to access enchants"));
    lore.add(Component.text(""));
    
    // Statistics section
    lore.add(Component.text("§a§lStatistics"));
    
    // Level with XP bar (smaller)
    int level = getPickaxeLevel(player);
    String levelLine = "§fLevel: §d" + level + " " + createCompactExpProgressBar(player);
    lore.add(Component.text(levelLine));
    
    // Blocks mined (you'll need to add this tracking)
    long blocksMined = getPlayerBlocksMined(player); // You'll need to implement this
    lore.add(Component.text("§fBlocks: §d" + formatNumber(blocksMined)));
    
    lore.add(Component.text(""));
    
    // Enchants section
    lore.add(Component.text("§c§lEnchants"));
    
    // Collect all enchants with their levels
    java.util.List<EnchantDisplay> allEnchants = new java.util.ArrayList<>();
    
    // Add token enchants
    Map<String, Integer> tokenEnchantLevels = getPlayerTokenEnchants(player);
    for (Map.Entry<String, Integer> entry : tokenEnchantLevels.entrySet()) {
        PickaxeEnchant enchant = tokenEnchants.get(entry.getKey());
        if (enchant != null && entry.getValue() > 0) {
            String enchantColor = getEnchantColor(enchant.getName());
            allEnchants.add(new EnchantDisplay(enchant.getName(), entry.getValue(), 
                enchant.getMaxLevel(), getEnchantUnlockLevel(entry.getKey(), true), enchantColor));
        }
    }

    // Add soul enchants
    Map<String, Integer> soulEnchantLevels = getPlayerSoulEnchants(player);
    for (Map.Entry<String, Integer> entry : soulEnchantLevels.entrySet()) {
        PickaxeEnchant enchant = soulEnchants.get(entry.getKey());
        if (enchant != null && entry.getValue() > 0) {
            String enchantColor = getEnchantColor(enchant.getName());
            allEnchants.add(new EnchantDisplay(enchant.getName(), entry.getValue(), 
                enchant.getMaxLevel(), getEnchantUnlockLevel(entry.getKey(), false), enchantColor));
        }
    }

    // Sort by unlock level (lowest to highest)
    allEnchants.sort((a, b) -> Integer.compare(a.unlockLevel, b.unlockLevel));

    // Add enchants to lore
    if (!allEnchants.isEmpty()) {
        for (EnchantDisplay enchant : allEnchants) {
            String enchantLine = formatEnchantForLore(enchant);
            lore.add(Component.text(enchantLine));
        }
    } else {
        lore.add(Component.text("§7§oNo enchants applied"));
    }

    lore.add(Component.text(""));
    lore.add(Component.text("§c§lSOULBOUND"));

    return lore;
}

// Add this method to PickaxeManager to get enchant colors
private String getEnchantColor(String enchantName) {
    String name = enchantName.toLowerCase();
    
    switch (name) {
        // Token Enchants - Warm colors
        case "efficiency":
            return "§b"; // Aqua
        case "fortune":
            return "§a"; // Green
        case "explosive":
            return "§c"; // Red
        case "speed":
            return "§f"; // White
        case "haste":
            return "§9"; // Blue
        case "magnet":
            return "§e"; // Yellow
        case "auto sell":
        case "auto_sell":
            return "§e"; // Yellow
        
        // Soul Enchants - Dark/mystical colors
        case "super fortune":
        case "super_fortune":
            return "§a"; // Green
        case "mega explosive":
        case "mega_explosive":
            return "§4"; // Dark Red
        case "auto smelt":
        case "auto_smelt":
            return "§6"; // Gold
        case "void walker":
        case "void_walker":
            return "§8"; // Dark Gray
        case "time warp":
        case "time_warp":
            return "§5"; // Dark Purple
        
        default:
            // Fallback to original logic based on enchant type
            // Check if it's a token enchant by looking it up
            return tokenEnchants.containsKey(name) ? "§6" : "§5";
    }
}

// Add this new method for the compact XP bar
private String createCompactExpProgressBar(Player player) {
    long currentExp = getPickaxeExp(player);
    long expRequired = getExpRequired(getPickaxeLevel(player));
    
    // Calculate progress (0.0 to 1.0)
    double progress = Math.min(1.0, (double) currentExp / expRequired);
    
    int totalBars = 8; // Smaller bar for beside the level
    int filledBars = (int) (progress * totalBars);
    
    StringBuilder bar = new StringBuilder("§7[");
    for (int i = 0; i < totalBars; i++) {
        if (i < filledBars) {
            bar.append("§a█");
        } else {
            bar.append("§8█");
        }
    }
    bar.append("§7]");
    
    return bar.toString();
}

// Add this method to track blocks mined (you'll need to implement the tracking)
public long getPlayerBlocksMined(Player player) {
    // You'll need to add a blocks mined counter to your player data
    // For now, return 0 or a placeholder value
    String playerUUID = player.getUuid().toString();
    // Add this field to your class: private final Map<String, Long> playerBlocksMined = new ConcurrentHashMap<>();
    // Then uncomment the line below:
    return playerBlocksMined.getOrDefault(playerUUID, 0L);
    //return 0L; // Placeholder - replace with actual implementation
}

// Add this method to format large numbers
private String formatNumber(long number) {
    if (number >= 1_000_000_000) {
        return String.format("%.1fB", number / 1_000_000_000.0);
    } else if (number >= 1_000_000) {
        return String.format("%.1fM", number / 1_000_000.0);
    } else if (number >= 1_000) {
        return String.format("%.1fK", number / 1_000.0);
    } else {
        return String.valueOf(number);
    }
}

    private String createExpProgressBar(Player player) {
        long currentExp = getPickaxeExp(player);
        long expRequired = getExpRequired(getPickaxeLevel(player));
        
        // Calculate progress (0.0 to 1.0)
        double progress = Math.min(1.0, (double) currentExp / expRequired);
        
        int totalBars = 15; // Slightly shorter for pickaxe lore
        int filledBars = (int) (progress * totalBars);
        
        StringBuilder bar = new StringBuilder("§f§lEXP: §7[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append("§7] §b").append(String.format("%.1f", progress * 100)).append("%");
        
        return bar.toString();
    }

    private String formatEnchantForLore(EnchantDisplay enchant) {
        StringBuilder formatted = new StringBuilder();
        
        // Add enchant name with color
        formatted.append(enchant.color).append("§l").append(enchant.name);
        
        // Add level or MAX
        if (enchant.currentLevel >= enchant.maxLevel) {
            formatted.append(" §6§lMAX");
        } else {
            formatted.append(" §f").append(enchant.currentLevel);
        }
        
        return formatted.toString();
    }

    // Helper method to get the unlock level for each enchant
    private int getEnchantUnlockLevel(String enchantKey, boolean isTokenEnchant) {
        // Define unlock levels for each enchant
        // You can customize these values based on your game balance
        if (isTokenEnchant) {
            switch (enchantKey.toLowerCase()) {
                case "efficiency": return 1;
                case "fortune": return 5;
                case "speed": return 10;
                case "haste": return 15;
                case "explosive": return 20;
                case "magnet": return 25;
                case "auto_sell": return 30;
                default: return 1;
            }
        } else {
            switch (enchantKey.toLowerCase()) {
                case "super_fortune": return 35;
                case "auto_smelt": return 40;
                case "mega_explosive": return 45;
                case "void_walker": return 50;
                case "time_warp": return 55;
                default: return 35;
            }
        }
    }

    // Helper class to store enchant display information
    private static class EnchantDisplay {
        final String name;
        final int currentLevel;
        final int maxLevel;
        final int unlockLevel;
        final String color;
        
        EnchantDisplay(String name, int currentLevel, int maxLevel, int unlockLevel, String color) {
            this.name = name;
            this.currentLevel = currentLevel;
            this.maxLevel = maxLevel;
            this.unlockLevel = unlockLevel;
            this.color = color;
        }
    }

    private String formatEnchantForLore(String enchantName, int currentLevel, int maxLevel, String color) {
        StringBuilder formatted = new StringBuilder();

        // Add enchant name with color
        formatted.append("§f  ").append(color).append("§l").append(enchantName);

        // Add level display
        formatted.append(" §7[");

        // Create level progression display (e.g., ★★★☆☆ for level 3/5)
        for (int i = 1; i <= maxLevel; i++) {
            if (i <= currentLevel) {
                formatted.append("§a★"); // Filled star for achieved levels
            } else {
                formatted.append("§8☆"); // Empty star for remaining levels
            }
        }
        
        formatted.append("§7]");
        
        // Add max level indicator if maxed
        if (currentLevel >= maxLevel) {
            formatted.append(" §6§lMAX");
        }
        
        return formatted.toString();
    }

    public void givePickaxe(Player player) {
        // Legacy method - redirect to soulbound version
        giveSoulboundPickaxe(player);
    }

    public boolean isSoulboundPickaxe(ItemStack item) {
        if (item == null || item.isAir()) {
            return false;
        }

        // Check if item has soulbound tag and is a diamond pickaxe
        return item.material() == Material.DIAMOND_PICKAXE &&
                item.hasTag(SOULBOUND_TAG) &&
                Boolean.TRUE.equals(item.getTag(SOULBOUND_TAG));
    }

    public boolean preventPickaxeMovement(InventoryPreClickEvent event) {
    // This method is now simplified since we handle it directly in MythicPrison
    // Keep it for backwards compatibility but it won't be used
    return false;
}

    // Replace the existing updatePickaxe method with this simpler version
    public void updatePickaxe(Player player) {
        // Simply regenerate the pickaxe using existing method
        giveSoulboundPickaxe(player);
    }

    public void addPickaxeExp(Player player, long exp) {
        try {
            String playerUUID = player.getUuid().toString();
            long currentExp = playerPickaxeExp.getOrDefault(playerUUID, 0L);
            int currentLevel = playerPickaxeLevels.getOrDefault(playerUUID, 1);

            currentExp += exp;
            playerPickaxeExp.put(playerUUID, currentExp);

            // Check for level up
            long expRequired = getExpRequired(currentLevel);
            if (currentExp >= expRequired) {
                currentLevel++;
                playerPickaxeLevels.put(playerUUID, currentLevel);
                currentExp -= expRequired;
                playerPickaxeExp.put(playerUUID, currentExp);

                player.sendMessage("§6§l✦ PICKAXE LEVEL UP! §e§lLevel " + currentLevel);
                player.sendMessage("§eYour pickaxe has grown stronger!");
            }

            // Always update the pickaxe item after gaining experience (not just on level up)
            updatePickaxe(player);

            // Update the player's experience bar
            updatePlayerExpBar(player);

        } catch (Exception e) {
            System.err.println("[PickaxeManager] Error adding pickaxe exp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Legacy compatibility method for ItemInteractionListener
    // Replace the existing addExperience method with this simpler version
    public void addExperience(Player player, int exp) {
        // Use the existing addPickaxeExp method which already handles everything properly
        addPickaxeExp(player, (long) exp);
    }

    // Overloaded method for long values
    public void addExperience(Player player, long exp) {
        addPickaxeExp(player, exp);
    }

    // Enchant methods
    public Map<String, PickaxeEnchant> getTokenEnchants() {
        return new HashMap<>(tokenEnchants);
    }

    public Map<String, PickaxeEnchant> getSoulEnchants() {
        return new HashMap<>(soulEnchants);
    }

    public Map<String, Integer> getPlayerTokenEnchants(Player player) {
        String playerUUID = player.getUuid().toString();
        return playerTokenEnchants.getOrDefault(playerUUID, new HashMap<>());
    }

    public Map<String, Integer> getPlayerSoulEnchants(Player player) {
        String playerUUID = player.getUuid().toString();
        return playerSoulEnchants.getOrDefault(playerUUID, new HashMap<>());
    }

    // Generic method that works for both token and soul enchants
    public int getEnchantLevel(Player player, String enchantName) {
        String lowercaseName = enchantName.toLowerCase();
        
        // Check token enchants first
        int tokenLevel = getTokenEnchantLevel(player, lowercaseName);
        if (tokenLevel > 0) {
            return tokenLevel;
        }
        
        // Check soul enchants
        int soulLevel = getSoulEnchantLevel(player, lowercaseName);
        return soulLevel;
    }

    public boolean canPurchaseEnchant(Player player, String enchantName, boolean isTokenEnchant) {
        if (isTokenEnchant) {
            PickaxeEnchant enchant = tokenEnchants.get(enchantName.toLowerCase());
            if (enchant == null) return false;
            
            int currentLevel = getTokenEnchantLevel(player, enchantName);
            return currentLevel < enchant.getMaxLevel();
        } else {
            PickaxeEnchant enchant = soulEnchants.get(enchantName.toLowerCase());
            if (enchant == null) return false;
            
            int currentLevel = getSoulEnchantLevel(player, enchantName);
            return currentLevel < enchant.getMaxLevel();
        }
    }

    public boolean purchaseEnchant(Player player, String enchantName, boolean isTokenEnchant) {
        // Add currency checking logic here
        // For now, just delegate to add methods
        if (isTokenEnchant) {
            return addTokenEnchant(player, enchantName, 1);
        } else {
            return addSoulEnchant(player, enchantName, 1);
        }
    }

    public boolean addTokenEnchant(Player player, String enchantName, int level) {
        String playerUUID = player.getUuid().toString();
        PickaxeEnchant enchant = tokenEnchants.get(enchantName.toLowerCase());

        if (enchant == null) {
            return false;
        }

        Map<String, Integer> playerEnchants = playerTokenEnchants.computeIfAbsent(playerUUID, k -> new HashMap<>());
        int currentLevel = playerEnchants.getOrDefault(enchantName.toLowerCase(), 0);
        int newLevel = Math.min(currentLevel + level, enchant.getMaxLevel());

        if (newLevel > currentLevel) {
            playerEnchants.put(enchantName.toLowerCase(), newLevel);
            updatePickaxe(player);
            return true;
        }

        return false;
    }

    public boolean addSoulEnchant(Player player, String enchantName, int level) {
        String playerUUID = player.getUuid().toString();
        PickaxeEnchant enchant = soulEnchants.get(enchantName.toLowerCase());

        if (enchant == null) {
            return false;
        }

        Map<String, Integer> playerEnchants = playerSoulEnchants.computeIfAbsent(playerUUID, k -> new HashMap<>());
        int currentLevel = playerEnchants.getOrDefault(enchantName.toLowerCase(), 0);
        int newLevel = Math.min(currentLevel + level, enchant.getMaxLevel());

        if (newLevel > currentLevel) {
            playerEnchants.put(enchantName.toLowerCase(), newLevel);
            updatePickaxe(player);
            return true;
        }

        return false;
    }

    public boolean hasTokenEnchant(Player player, String enchantName) {
        Map<String, Integer> enchants = getPlayerTokenEnchants(player);
        return enchants.containsKey(enchantName.toLowerCase()) && enchants.get(enchantName.toLowerCase()) > 0;
    }

    public boolean hasSoulEnchant(Player player, String enchantName) {
        Map<String, Integer> enchants = getPlayerSoulEnchants(player);
        return enchants.containsKey(enchantName.toLowerCase()) && enchants.get(enchantName.toLowerCase()) > 0;
    }

    public int getTokenEnchantLevel(Player player, String enchantName) {
        Map<String, Integer> enchants = getPlayerTokenEnchants(player);
        return enchants.getOrDefault(enchantName.toLowerCase(), 0);
    }

    public int getSoulEnchantLevel(Player player, String enchantName) {
        Map<String, Integer> enchants = getPlayerSoulEnchants(player);
        return enchants.getOrDefault(enchantName.toLowerCase(), 0);
    }

    public int getPickaxeLevel(Player player) {
        return playerPickaxeLevels.getOrDefault(player.getUuid().toString(), 1);
    }

    public long getPickaxeExp(Player player) {
        return playerPickaxeExp.getOrDefault(player.getUuid().toString(), 0L);
    }

    public long getExpRequired(int level) {
        return (long) (100 * Math.pow(1.5, level - 1));
    }

    public double getPickaxeMultiplier(Player player) {
        int level = getPickaxeLevel(player);
        double multiplier = 1.0 + (level * 0.05); // 5% bonus per level

        // Add token enchant multipliers
        int fortuneLevel = getTokenEnchantLevel(player, "fortune");
        if (fortuneLevel > 0) {
            multiplier += (fortuneLevel * 0.25); // 25% per fortune level
        }

        // Add soul enchant multipliers
        int superFortuneLevel = getSoulEnchantLevel(player, "super_fortune");
        if (superFortuneLevel > 0) {
            multiplier += (superFortuneLevel * 0.5); // 50% per super fortune level
        }

        return multiplier;
    }

    public boolean hasPickaxe(Player player) {
        ItemStack item = player.getInventory().getItemStack(0);
        return isSoulboundPickaxe(item);
    }

    // Ensure player always has their pickaxe (call this periodically or on important events)
    public void ensurePlayerHasPickaxe(Player player) {
        if (!hasPickaxe(player)) {
            giveSoulboundPickaxe(player);
        }
    }

    // Update the handlePickaxeRightClick method
    public void handlePickaxeRightClick(Player player, ItemStack item) {
        if (isSoulboundPickaxe(item)) {
            // Open the pickaxe enchant GUI using the new system
            new mythic.prison.gui.PickaxeEnchantGUI(player).open();
        }
    }

    public void addBlocksMined(Player player, long blocks) {
    String playerUUID = player.getUuid().toString();
    long currentBlocks = playerBlocksMined.getOrDefault(playerUUID, 0L);
    playerBlocksMined.put(playerUUID, currentBlocks + blocks);
    
    // Update the pickaxe to reflect new stats
    updatePickaxe(player);
}

public boolean preventItemSwap(PlayerSwapItemEvent event) {
    Player player = event.getPlayer();
    ItemStack mainHandItem = event.getMainHandItem();
    ItemStack offHandItem = event.getOffHandItem();
    
    // Only prevent swapping TO the offhand if it's a soulbound pickaxe
    // This allows normal hotbar scrolling but prevents moving pickaxe to offhand
    if (isSoulboundPickaxe(mainHandItem)) {
        player.sendMessage("§cYou cannot move your soulbound pickaxe to your offhand!");
        return true; // Cancel the swap
    }
    
    return false;
}

// Add this method to check and restore pickaxe if missing
public void validatePlayerPickaxe(Player player) {
    try {
        ItemStack slot0Item = player.getInventory().getItemStack(0);
        
        // If slot 0 is empty or doesn't contain the soulbound pickaxe, restore it
        if (!isSoulboundPickaxe(slot0Item)) {
            System.out.println("[PickaxeManager] Restoring missing pickaxe for: " + player.getUsername());
            giveSoulboundPickaxe(player);
        }
    } catch (Exception e) {
        System.err.println("[PickaxeManager] Error validating pickaxe for " + player.getUsername() + ": " + e.getMessage());
        e.printStackTrace();
    }
}

// Force give pickaxe immediately on join
public void forceGivePickaxe(Player player) {
    try {
        giveSoulboundPickaxe(player);
        System.out.println("[PickaxeManager] Force gave pickaxe to: " + player.getUsername());
    } catch (Exception e) {
        System.err.println("[PickaxeManager] Error force giving pickaxe: " + e.getMessage());
        e.printStackTrace();
    }
}


}