package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.enchants.PickaxeEnchant;
import mythic.prison.data.enchants.TokenEnchant;
import mythic.prison.data.enchants.SoulEnchant;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

import java.util.Arrays;
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

    // Enchants configuration
    private final Map<String, PickaxeEnchant> tokenEnchants = new HashMap<>();
    private final Map<String, PickaxeEnchant> soulEnchants = new HashMap<>();

    public PickaxeManager() {
        // Initialize with default values
        initializeTokenEnchants();
        initializeSoulEnchants();
        System.out.println("[PickaxeManager] Manager initialized");
    }

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
        playerTokenEnchants.putIfAbsent(playerUUID, new HashMap<>());
        playerSoulEnchants.putIfAbsent(playerUUID, new HashMap<>());

        System.out.println("[PickaxeManager] Initialized player: " + player.getUsername());
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

            // Create the soulbound pickaxe
            ItemStack pickaxe = ItemStack.builder(Material.DIAMOND_PICKAXE)
                    .customName(Component.text("§d§lMYTHIC §fPickaxe"))
                    .lore(buildPickaxeLore(player))
                    .build();

            // Add tags to identify this as a soulbound pickaxe
            pickaxe = pickaxe.withTag(PICKAXE_LEVEL_TAG, getPickaxeLevel(player))
                    .withTag(PICKAXE_EXP_TAG, getPickaxeExp(player))
                    .withTag(SOULBOUND_TAG, true);

            // Always place in slot 0 (first slot)
            player.getInventory().setItemStack(0, pickaxe);

            System.out.println("[PickaxeManager] Gave soulbound pickaxe to: " + player.getUsername());

        } catch (Exception e) {
            System.err.println("[PickaxeManager] Error giving soulbound pickaxe to " + player.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private java.util.List<Component> buildPickaxeLore(Player player) {
        java.util.List<Component> lore = new java.util.ArrayList<>();

        lore.add(Component.text("§7A legendary pickaxe forged"));
        lore.add(Component.text("§7in the depths of MythicPvP!"));
        lore.add(Component.text(""));
        lore.add(Component.text("§f§lLevel: §a" + getPickaxeLevel(player)));
        lore.add(Component.text("§f§lEXP: §b" + getPickaxeExp(player) + "§7/§b" + getExpRequired(getPickaxeLevel(player))));

        // Add token enchants to lore
        Map<String, Integer> tokenEnchantLevels = getPlayerTokenEnchants(player);
        if (!tokenEnchantLevels.isEmpty()) {
            lore.add(Component.text(""));
            lore.add(Component.text("§6§lToken Enchants:"));
            for (Map.Entry<String, Integer> entry : tokenEnchantLevels.entrySet()) {
                PickaxeEnchant enchant = tokenEnchants.get(entry.getKey());
                if (enchant != null) {
                    lore.add(Component.text("§e" + enchant.getName() + " §7Level " + entry.getValue()));
                }
            }
        }

        // Add soul enchants to lore
        Map<String, Integer> soulEnchantLevels = getPlayerSoulEnchants(player);
        if (!soulEnchantLevels.isEmpty()) {
            lore.add(Component.text(""));
            lore.add(Component.text("§5§lSoul Enchants:"));
            for (Map.Entry<String, Integer> entry : soulEnchantLevels.entrySet()) {
                PickaxeEnchant enchant = soulEnchants.get(entry.getKey());
                if (enchant != null) {
                    lore.add(Component.text("§d" + enchant.getName() + " §7Level " + entry.getValue()));
                }
            }
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§c§lSOULBOUND"));
        lore.add(Component.text("§d§lMYTHIC§fPVP §8| §fPrison Server"));

        return lore;
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
        // Check if player is trying to move the soulbound pickaxe
        ItemStack clickedItem = event.getClickedItem();
        
        // Prevent moving soulbound pickaxe from slot 0
        if (event.getSlot() == 0) {
            ItemStack slotItem = event.getInventory().getItemStack(0);
            if (isSoulboundPickaxe(slotItem)) {
                return true; // Prevent any interaction with slot 0 if it contains soulbound pickaxe
            }
        }

        // Prevent moving soulbound pickaxe FROM any slot
        if (isSoulboundPickaxe(clickedItem)) {
            return true; // Prevent moving soulbound pickaxe
        }

        return false;
    }

    public void updatePickaxe(Player player) {
        try {
            // Get current pickaxe from slot 0
            ItemStack currentPickaxe = player.getInventory().getItemStack(0);

            // Only update if it's a soulbound pickaxe
            if (isSoulboundPickaxe(currentPickaxe)) {
                // Create updated pickaxe
                ItemStack updatedPickaxe = ItemStack.builder(Material.DIAMOND_PICKAXE)
                        .customName(Component.text("§d§lMYTHIC §fPickaxe"))
                        .lore(buildPickaxeLore(player))
                        .build();

                // Add tags
                updatedPickaxe = updatedPickaxe.withTag(PICKAXE_LEVEL_TAG, getPickaxeLevel(player))
                        .withTag(PICKAXE_EXP_TAG, getPickaxeExp(player))
                        .withTag(SOULBOUND_TAG, true);

                // Update the pickaxe in slot 0
                player.getInventory().setItemStack(0, updatedPickaxe);
            } else {
                // Player doesn't have their soulbound pickaxe, give them one
                giveSoulboundPickaxe(player);
            }

        } catch (Exception e) {
            System.err.println("[PickaxeManager] Error updating pickaxe for " + player.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
        }
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

                // Update the pickaxe item
                updatePickaxe(player);
            }

            // Update the player's experience bar
            updatePlayerExpBar(player);

        } catch (Exception e) {
            System.err.println("[PickaxeManager] Error adding pickaxe exp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Legacy compatibility method for ItemInteractionListener
    public void addExperience(Player player, int exp) {
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
}