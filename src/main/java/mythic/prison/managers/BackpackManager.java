package mythic.prison.managers;

import mythic.prison.data.backpack.Backpack;
import mythic.prison.MythicPrison;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.entity.Player;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import mythic.prison.player.Profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class BackpackManager {

    private final Map<String, Backpack> playerBackpacks = new ConcurrentHashMap<>();
    private MongoCollection<Document> backpackCollection;

    public BackpackManager() {
        // Initialize MongoDB collection for backpacks
        try {
            MongoDatabase database = MythicPrison.getInstance().getMongoManager().getDatabase();
            this.backpackCollection = database.getCollection("backpacks");
        } catch (Exception e) {
            System.err.println("[BackpackManager] Failed to initialize database connection: " + e.getMessage());
        }
    }

    public void initializePlayer(Player player) {
        String uuid = player.getUuid().toString();
        if (!playerBackpacks.containsKey(uuid)) {
            // Try to load from database first
            loadBackpackFromDatabase(uuid).thenAccept(backpack -> {
                if (backpack != null) {
                    playerBackpacks.put(uuid, backpack);
                } else {
                    // Create new backpack if none exists
                    Backpack newBackpack = new Backpack(uuid);
                    playerBackpacks.put(uuid, newBackpack);
                    saveBackpackToDatabase(newBackpack);
                }
            });
        }
    }

    public void initializePlayer(Object player) {
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public Backpack getBackpack(Player player) {
        String uuid = player.getUuid().toString();
        return playerBackpacks.get(uuid);
    }

    public Backpack getPlayerBackpack(Player player) {
        return getBackpack(player);
    }

    public void addBlock(Player player, String blockType, int amount) {
        Backpack backpack = getBackpack(player);
        if (backpack != null) {
            backpack.addBlock(blockType, amount);
            // Save to database async
            saveBackpackToDatabase(backpack);
        }
    }

    // Add this method to properly save backpack data after adding blocks
    public void addBlocks(Player player, String blockType, int amount) {
        Backpack backpack = getBackpack(player);
        if (backpack != null) {
            backpack.addBlock(blockType, amount);
            // Save to database immediately for real-time updates
            saveBackpackToDatabase(backpack);
            
            // Update scoreboard
            ScoreboardManager scoreboardManager = MythicPrison.getInstance().getScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.updatePlayerScoreboard(player);
            }
        }
    }

    public void sellBackpack(Player player, boolean showMessage) {
        Backpack backpack = getBackpack(player);
        if (backpack == null || backpack.isEmpty()) {
            if (showMessage) {
                ChatUtil.sendError(player, "Your backpack is empty!");
            }
            return;
        }

        double totalValue = 0;
        int totalBlocks = 0;

        // Calculate total value
        for (Map.Entry<String, Integer> entry : backpack.getBlocks().entrySet()) {
            String blockType = entry.getKey();
            int amount = entry.getValue();
            double blockValue = getBlockValue(blockType);
            totalValue += blockValue * amount * backpack.getSellMultiplier();
            totalBlocks += amount;
        }

        // Add money to player
        var currencyManager = MythicPrison.getInstance().getCurrencyManager();
        currencyManager.addBalance(player, "money", totalValue);

        // Clear backpack
        backpack.clear();

        // Save to database
        saveBackpackToDatabase(backpack);

        if (showMessage) {
            ChatUtil.sendSuccess(player, "Sold " + totalBlocks + " blocks for $" + ChatUtil.formatMoney(totalValue) + "!");
        }
    }

    public void upgradeCapacity(Player player) {
        Backpack backpack = getBackpack(player);
        if (backpack == null) return;

        int currentCapacity = backpack.getMaxVolume();
        int newCapacity = currentCapacity + 500; // Increase by 500
        double cost = calculateUpgradeCost("capacity", currentCapacity);

        var currencyManager = MythicPrison.getInstance().getCurrencyManager();
        if (currencyManager.getBalance(player, "money") >= cost) {
            if (currencyManager.removeBalance(player, "money", cost)) {
                backpack.setMaxVolume(newCapacity);
                saveBackpackToDatabase(backpack);
                ChatUtil.sendSuccess(player, "Backpack capacity upgraded to " + newCapacity + " blocks!");
                ChatUtil.sendMessage(player, "§7Cost: §c-$" + ChatUtil.formatMoney(cost));
            }
        } else {
            ChatUtil.sendError(player, "You need $" + ChatUtil.formatMoney(cost) + " to upgrade capacity!");
        }
    }

    public void upgradeSellMultiplier(Player player) {
        Backpack backpack = getBackpack(player);
        if (backpack == null) return;

        double currentMultiplier = backpack.getSellMultiplier();
        double newMultiplier = currentMultiplier + 0.1; // Increase by 0.1x
        double cost = calculateUpgradeCost("multiplier", (int)(currentMultiplier * 10));

        var currencyManager = MythicPrison.getInstance().getCurrencyManager();
        if (currencyManager.getBalance(player, "money") >= cost) {
            if (currencyManager.removeBalance(player, "money", cost)) {
                backpack.setSellMultiplier(newMultiplier);
                saveBackpackToDatabase(backpack);
                ChatUtil.sendSuccess(player, "Sell multiplier upgraded to " + String.format("%.1fx", newMultiplier) + "!");
                ChatUtil.sendMessage(player, "§7Cost: §c-$" + ChatUtil.formatMoney(cost));
            }
        } else {
            ChatUtil.sendError(player, "You need $" + ChatUtil.formatMoney(cost) + " to upgrade sell multiplier!");
        }
    }

    public void upgradeAutoSell(Player player) {
        Backpack backpack = getBackpack(player);
        if (backpack == null) return;

        if (!backpack.isAutoSellEnabled()) {
            // Enable auto-sell
            double cost = 50000; // Base cost to enable auto-sell
            var currencyManager = MythicPrison.getInstance().getCurrencyManager();

            if (currencyManager.getBalance(player, "money") >= cost) {
                if (currencyManager.removeBalance(player, "money", cost)) {
                    backpack.setAutoSellEnabled(true);
                    saveBackpackToDatabase(backpack);
                    ChatUtil.sendSuccess(player, "Auto-sell enabled!");
                    ChatUtil.sendMessage(player, "§7Cost: §c-$" + ChatUtil.formatMoney(cost));
                }
            } else {
                ChatUtil.sendError(player, "You need $" + ChatUtil.formatMoney(cost) + " to enable auto-sell!");
            }
        } else {
            // Upgrade auto-sell interval
            int currentInterval = backpack.getAutoSellInterval();
            int newInterval = Math.max(10, currentInterval - 5); // Reduce by 5 seconds, minimum 10
            double cost = calculateUpgradeCost("autosell", 60 - currentInterval);

            var currencyManager = MythicPrison.getInstance().getCurrencyManager();
            if (currencyManager.getBalance(player, "money") >= cost) {
                if (currencyManager.removeBalance(player, "money", cost)) {
                    backpack.setAutoSellInterval(newInterval);
                    saveBackpackToDatabase(backpack);
                    ChatUtil.sendSuccess(player, "Auto-sell interval reduced to " + newInterval + " seconds!");
                    ChatUtil.sendMessage(player, "§7Cost: §c-$" + ChatUtil.formatMoney(cost));
                }
            } else {
                ChatUtil.sendError(player, "You need $" + ChatUtil.formatMoney(cost) + " to upgrade auto-sell!");
            }
        }
    }

    public void toggleAutoSell(Player player) {
        Backpack backpack = getBackpack(player);
        if (backpack == null) return;

        if (backpack.isAutoSellEnabled()) {
            backpack.setAutoSellEnabled(false);
            saveBackpackToDatabase(backpack);
            ChatUtil.sendMessage(player, "§c§lAuto-sell disabled!");
        } else {
            ChatUtil.sendError(player, "Auto-sell is not unlocked! Use §f/backpack upgrade autosell §c to unlock it.");
        }
    }

    
    private CompletableFuture<Backpack> loadBackpackFromDatabase(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (backpackCollection == null) return null;
                
                Document doc = backpackCollection.find(Filters.eq("playerUUID", uuid)).first();
                if (doc != null) {
                    Backpack backpack = new Backpack(uuid);
                    
                    // Use proper MongoDB Document methods with null checks
                    backpack.setMaxVolume(doc.getInteger("maxVolume") != null ? doc.getInteger("maxVolume") : 1000);
                    backpack.setCurrentVolume(doc.getInteger("currentVolume") != null ? doc.getInteger("currentVolume") : 0);
                    backpack.setSellMultiplier(doc.getDouble("sellMultiplier") != null ? doc.getDouble("sellMultiplier") : 1.0);
                    backpack.setAutoSellEnabled(doc.getBoolean("autoSellEnabled") != null ? doc.getBoolean("autoSellEnabled") : false);
                    backpack.setAutoSellInterval(doc.getInteger("autoSellInterval") != null ? doc.getInteger("autoSellInterval") : 60);
                    
                    // Load blocks
                    Document blocksDoc = doc.get("blocks", Document.class);
                    if (blocksDoc != null) {
                        for (Map.Entry<String, Object> entry : blocksDoc.entrySet()) {
                            if (entry.getValue() instanceof Integer) {
                                backpack.addBlock(entry.getKey(), (Integer) entry.getValue());
                            }
                        }
                    }
                    
                    return backpack;
                }
            } catch (Exception e) {
                System.err.println("[BackpackManager] Error loading backpack for " + uuid + ": " + e.getMessage());
            }
            return null;
        });
    }

    private void saveBackpackToDatabase(Backpack backpack) {
        if (backpackCollection == null) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                Document doc = new Document("playerUUID", backpack.getPlayerUUID())
                    .append("maxVolume", backpack.getMaxVolume())
                    .append("currentVolume", backpack.getCurrentVolume())
                    .append("sellMultiplier", backpack.getSellMultiplier())
                    .append("autoSellEnabled", backpack.isAutoSellEnabled())
                    .append("autoSellInterval", backpack.getAutoSellInterval())
                    .append("blocks", new Document(backpack.getBlocks()));
                
                backpackCollection.replaceOne(
                    Filters.eq("playerUUID", backpack.getPlayerUUID()),
                    doc,
                    new com.mongodb.client.model.ReplaceOptions().upsert(true)
                );
            } catch (Exception e) {
                System.err.println("[BackpackManager] Error saving backpack for " + backpack.getPlayerUUID() + ": " + e.getMessage());
            }
        });
    }

    public void saveAllBackpacks() {
        for (Backpack backpack : playerBackpacks.values()) {
            saveBackpackToDatabase(backpack);
        }
    }

    private double getBlockValue(String blockType) {
        // Base block values - you can expand this
        return switch (blockType.toLowerCase()) {
            case "cobblestone" -> 1.0;
            case "stone" -> 1.5;
            case "coal_ore" -> 5.0;
            case "iron_ore" -> 10.0;
            case "gold_ore" -> 25.0;
            case "diamond_ore" -> 100.0;
            case "emerald_ore" -> 250.0;
            default -> 1.0;
        };
    }

    private double calculateUpgradeCost(String upgradeType, int currentLevel) {
        return switch (upgradeType) {
            case "capacity" -> 10000 * Math.pow(1.5, currentLevel / 1000);
            case "multiplier" -> 25000 * Math.pow(2.0, currentLevel - 10);
            case "autosell" -> 100000 * Math.pow(1.8, currentLevel);
            default -> 10000;
        };
    }

    // Legacy class for compatibility
    public static class PlayerBackpack {
        private final String playerUUID;
        private int size;
        private int usedSlots;

        public PlayerBackpack(String playerUUID) {
            this.playerUUID = playerUUID;
            this.size = 27; // Default backpack size
            this.usedSlots = 0;
        }

        // Getters and setters
        public String getPlayerUUID() { return playerUUID; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getUsedSlots() { return usedSlots; }
        public void setUsedSlots(int usedSlots) { this.usedSlots = usedSlots; }
    }

    // Ensure this method exists for scoreboard updates
    private void updateScoreboard(Player player) {
        try {
            ScoreboardManager scoreboardManager = MythicPrison.getInstance().getScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.updatePlayerScoreboard(player);
            }
        } catch (Exception e) {
            System.err.println("[BackpackManager] Error updating scoreboard: " + e.getMessage());
        }
    }

    // Add method to sync backpack with profile
    public void saveBackpackToProfile(Player player, Backpack backpack) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Profile profile = Profile.loadAsync(player.getUuid().toString()).join();
                if (profile != null) {
                    profile.updateFromBackpack(backpack);
                    profile.saveAsync().join();
                }
                return null;
            } catch (Exception e) {
                System.err.println("Error saving backpack to profile: " + e.getMessage());
                return null;
            }
        });
    }

    // Add method to load backpack from profile
    public Backpack loadBackpackFromProfile(Player player) {
        try {
            Profile profile = Profile.loadAsync(player.getUuid().toString()).join();
            if (profile != null) {
                return profile.toBackpack();
            }
        } catch (Exception e) {
            System.err.println("Error loading backpack from profile: " + e.getMessage());
        }
        return new Backpack(player.getUuid().toString());
    }
}