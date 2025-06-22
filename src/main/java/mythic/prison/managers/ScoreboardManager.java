package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.backpack.Backpack;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final Map<String, Sidebar> playerScoreboards = new ConcurrentHashMap<>();

    public void initializePlayer(Player player) {
        createScoreboard(player);
    }

public void updatePlayerScoreboard(Player player) {
    try {
        String playerUUID = player.getUuid().toString();
        
        // Remove existing scoreboard
        Sidebar oldSidebar = playerScoreboards.get(playerUUID);
        if (oldSidebar != null) {
            oldSidebar.removeViewer(player);
            playerScoreboards.remove(playerUUID);
        }
        
        // Create new scoreboard
        createScoreboard(player);
        
    } catch (Exception e) {
        System.err.println("[ScoreboardManager] Error updating scoreboard for " + player.getUsername() + ": " + e.getMessage());
        e.printStackTrace();
    }
}

public void createScoreboard(Player player) {
    Sidebar sidebar = new Sidebar(Component.text("§d§lPRISON", NamedTextColor.LIGHT_PURPLE));
    
    // Build the scoreboard content
    buildScoreboardContent(player, sidebar);
    
    sidebar.addViewer(player);
    playerScoreboards.put(player.getUuid().toString(), sidebar);
}

private void buildScoreboardContent(Player player, Sidebar sidebar) {
    try {
        // Clear existing lines
        clearExistingLines(sidebar);

        // Get all required data
        CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
        BackpackManager backpackManager = MythicPrison.getInstance().getBackpackManager();

        if (currencyManager == null || rankingManager == null || backpackManager == null) {
            sidebar.createLine(new Sidebar.ScoreboardLine("error", Component.text("§cData loading..."), 15));
            return;
        }

        // Initialize backpack manager for player (only this one needs initialization)
        backpackManager.initializePlayer(player);

        int lineNumber = 15;

        // Get player data
        double money = currencyManager.getBalance(player, "money");
        double tokens = currencyManager.getBalance(player, "tokens");
        double souls = currencyManager.getBalance(player, "souls");
        
        String currentRank = rankingManager.getRank(player);
        int prestiges = rankingManager.getPrestige(player);
        int rebirths = rankingManager.getRebirth(player);
        int ascensions = rankingManager.getAscension(player);

        Backpack backpack = backpackManager.getBackpack(player);

        // Empty line at top
        sidebar.createLine(new Sidebar.ScoreboardLine("empty1", Component.text(""), lineNumber--));

        // Currencies section (Added $ sign for money)
        sidebar.createLine(new Sidebar.ScoreboardLine("money", Component.text("§fMoney: §a$" + formatNumber(money)), lineNumber--));
        sidebar.createLine(new Sidebar.ScoreboardLine("tokens", Component.text("§fTokens: §6" + formatNumber(tokens)), lineNumber--));
        sidebar.createLine(new Sidebar.ScoreboardLine("souls", Component.text("§fSouls: §5" + formatNumber(souls)), lineNumber--));

        // Empty line separator
        sidebar.createLine(new Sidebar.ScoreboardLine("empty2", Component.text(""), lineNumber--));

        // Progression section (Changed §7 to §f for white text)
        sidebar.createLine(new Sidebar.ScoreboardLine("rank", Component.text("§fRank: §f" + currentRank), lineNumber--));
        sidebar.createLine(new Sidebar.ScoreboardLine("prestiges", Component.text("§f✦ Prestiges: §b" + prestiges), lineNumber--));
        sidebar.createLine(new Sidebar.ScoreboardLine("rebirths", Component.text("§f⚡ Rebirths: §d" + rebirths), lineNumber--));
        sidebar.createLine(new Sidebar.ScoreboardLine("ascensions", Component.text("§f⭐ Ascensions: §e" + ascensions), lineNumber--));

        // Empty line separator
        sidebar.createLine(new Sidebar.ScoreboardLine("empty3", Component.text(""), lineNumber--));

        // Backpack section (Changed §7 to §f for white text)
        if (backpack != null) {
            int currentVolume = backpack.getCurrentVolume();
            int maxVolume = backpack.getMaxVolume();
            sidebar.createLine(new Sidebar.ScoreboardLine("backpack", Component.text("§f🎒 Backpack: §f" + currentVolume + "§f/§f" + maxVolume), lineNumber--));
        } else {
            sidebar.createLine(new Sidebar.ScoreboardLine("backpack", Component.text("§f🎒 Backpack: §cNot loaded"), lineNumber--));
        }

        // Online players (Changed §7 to §f for white text)
        int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
        sidebar.createLine(new Sidebar.ScoreboardLine("online", Component.text("§f👥 Online: §a" + onlinePlayers), lineNumber--));

        // Empty line separator
        sidebar.createLine(new Sidebar.ScoreboardLine("empty4", Component.text(""), lineNumber--));

        // Server website at bottom (Last line - no empty line after)
        sidebar.createLine(new Sidebar.ScoreboardLine("website", Component.text("§7play.mythicpvp.net"), lineNumber--));

    } catch (Exception e) {
        System.err.println("[ScoreboardManager] Error building scoreboard content: " + e.getMessage());
        e.printStackTrace();
        sidebar.createLine(new Sidebar.ScoreboardLine("error", Component.text("§cError loading data"), 15));
    }
}

private void clearExistingLines(Sidebar sidebar) {
    try {
        // Updated list of all possible line IDs that we might have created
        String[] lineIds = {
            "empty_15", "balance", "tokens", "souls", "blocks", "empty_10", "rank", "prestige", 
            "rebirth", "ascension", "empty_5", "online", "empty_3", "server", "empty_1"
        };
        
        // Remove each line individually (ignore errors if line doesn't exist)
        for (String lineId : lineIds) {
            try {
                sidebar.removeLine(lineId);
            } catch (Exception e) {
                // Ignore if line doesn't exist - this is expected
            }
        }
    } catch (Exception e) {
        // If all else fails, just log and continue
        System.err.println("[ScoreboardManager] Warning: Could not clear existing lines: " + e.getMessage());
    }
}

public void updateAllScoreboards() {
    try {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            updatePlayerScoreboard(player);
        }
    } catch (Exception e) {
        System.err.println("[ScoreboardManager] Error updating all scoreboards: " + e.getMessage());
    }
}

    private void clearAllLines(Sidebar sidebar) {
        // Clear all possible lines to prevent conflicts
        String[] lineIds = {"empty1", "money", "tokens", "souls", "beacons", "empty2", "backpack", 
                           "blocks", "empty3", "rank", "prestige", "rebirth", "ascension", "empty4", "online", 
                           "empty5", "website", "empty6", "error"};
        
        for (String lineId : lineIds) {
            try {
                sidebar.removeLine(lineId);
            } catch (Exception e) {
                // Ignore if line doesn't exist
            }
        }
    }


    public void removeScoreboard(Player player) {
        try {
            Sidebar sidebar = playerScoreboards.remove(player.getUuid().toString());
            if (sidebar != null) {
                sidebar.removeViewer(player);
            }
        } catch (Exception e) {
            System.err.println("[ScoreboardManager] Error removing scoreboard for " + player.getUsername() + ": " + e.getMessage());
        }
    }

public void removePlayerScoreboard(Player player) {
    String playerUuid = player.getUuid().toString();
    Sidebar sidebar = playerScoreboards.get(playerUuid);
    
    if (sidebar != null) {
        sidebar.removeViewer(player);
        playerScoreboards.remove(playerUuid);
    }
}
private String formatNumber(double number) {
    // Handle whole numbers without decimal places
    if (number == Math.floor(number) && !Double.isInfinite(number)) {
        // It's a whole number, format without decimals
        if (number >= 1e30) { // Nonillions
            double nonillions = number / 1e30;
            if (nonillions == Math.floor(nonillions)) {
                return String.format("%.0fn", nonillions);
            } else {
                return String.format("%.1fn", nonillions);
            }
        } else if (number >= 1e27) { // Octillions
            double octillions = number / 1e27;
            if (octillions == Math.floor(octillions)) {
                return String.format("%.0fo", octillions);
            } else {
                return String.format("%.1fo", octillions);
            }
        } else if (number >= 1e24) { // Septillions
            double septillions = number / 1e24;
            if (septillions == Math.floor(septillions)) {
                return String.format("%.0fss", septillions);
            } else {
                return String.format("%.1fss", septillions);
            }
        } else if (number >= 1e21) { // Sextillions
            double sextillions = number / 1e21;
            if (sextillions == Math.floor(sextillions)) {
                return String.format("%.0fs", sextillions);
            } else {
                return String.format("%.1fs", sextillions);
            }
        } else if (number >= 1e18) { // Quintillions
            double quintillions = number / 1e18;
            if (quintillions == Math.floor(quintillions)) {
                return String.format("%.0fqq", quintillions);
            } else {
                return String.format("%.1fqq", quintillions);
            }
        } else if (number >= 1_000_000_000_000_000L) { // Quadrillions
            double quadrillions = number / 1_000_000_000_000_000L;
            if (quadrillions == Math.floor(quadrillions)) {
                return String.format("%.0fqd", quadrillions);
            } else {
                return String.format("%.1fqd", quadrillions);
            }
        } else if (number >= 1_000_000_000_000L) { // Trillions
            double trillions = number / 1_000_000_000_000L;
            if (trillions == Math.floor(trillions)) {
                return String.format("%.0ft", trillions);
            } else {
                return String.format("%.1ft", trillions);
            }
        } else if (number >= 1_000_000_000) { // Billions
            double billions = number / 1_000_000_000;
            if (billions == Math.floor(billions)) {
                return String.format("%.0fb", billions);
            } else {
                return String.format("%.1fb", billions);
            }
        } else if (number >= 1_000_000) { // Millions
            double millions = number / 1_000_000;
            if (millions == Math.floor(millions)) {
                return String.format("%.0fm", millions);
            } else {
                return String.format("%.1fm", millions);
            }
        } else if (number >= 1_000) { // Thousands
            double thousands = number / 1_000;
            if (thousands == Math.floor(thousands)) {
                return String.format("%.0fk", thousands);
            } else {
                return String.format("%.1fk", thousands);
            }
        } else {
            return String.format("%.0f", number);
        }
    } else {
        // It has decimal places, format normally
        if (number >= 1e30) {
            return String.format("%.1fn", number / 1e30);
        } else if (number >= 1e27) {
            return String.format("%.1fo", number / 1e27);
        } else if (number >= 1e24) {
            return String.format("%.1fss", number / 1e24);
        } else if (number >= 1e21) {
            return String.format("%.1fs", number / 1e21);
        } else if (number >= 1e18) {
            return String.format("%.1fqq", number / 1e18);
        } else if (number >= 1_000_000_000_000_000L) {
            return String.format("%.1fqd", number / 1_000_000_000_000_000L);
        } else if (number >= 1_000_000_000_000L) {
            return String.format("%.1ft", number / 1_000_000_000_000L);
        } else if (number >= 1_000_000_000) {
            return String.format("%.1fb", number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.1fm", number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.1fk", number / 1_000);
        } else {
            return String.format("%.2f", number);
        }
    }
}
}