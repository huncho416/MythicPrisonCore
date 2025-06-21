package mythic.prison.managers;

import mythic.prison.MythicPrison;
import mythic.prison.data.backpack.Backpack;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final Map<String, Sidebar> playerScoreboards = new ConcurrentHashMap<>();

    public void initializePlayer(Player player) {
        createScoreboard(player);
    }

    public void createScoreboard(Player player) {
        Sidebar sidebar = new Sidebar(Component.text("§d§lPRISON", NamedTextColor.LIGHT_PURPLE));
        
        // Remove the setNumberFormat line - it doesn't exist in this version
        updateScoreboard(player, sidebar);
        
        sidebar.addViewer(player);
        playerScoreboards.put(player.getUuid().toString(), sidebar);
    }

    public void updateScoreboard(Player player, Sidebar sidebar) {
        if (sidebar == null) return;

        try {
            CurrencyManager currencyManager = MythicPrison.getInstance().getCurrencyManager();
            StatsManager statsManager = MythicPrison.getInstance().getStatsManager();
            RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();
            BackpackManager backpackManager = MythicPrison.getInstance().getBackpackManager();

        // Clear existing lines - remove all first to prevent conflicts
        clearAllLines(sidebar);

        // Using invisible Unicode characters to hide score numbers
        // Each line uses a different invisible character combination
        String[] invisibleChars = {
            "§r", "§r§0", "§r§1", "§r§2", "§r§3", "§r§4", "§r§5", "§r§6", "§r§7", 
            "§r§8", "§r§9", "§r§a", "§r§b", "§r§c", "§r§d", "§r§e", "§r§f"
        };

        // Add updated lines with unique invisible suffixes
        sidebar.createLine(new Sidebar.ScoreboardLine("empty1", Component.text("" + invisibleChars[0]), 14));
        
        // Money
        double money = currencyManager.getBalance(player, "money");
        sidebar.createLine(new Sidebar.ScoreboardLine("money", 
            Component.text("§a💰 Money: §f$" + formatNumber(money) + invisibleChars[1]), 13));
        
        // Tokens
        double tokens = currencyManager.getBalance(player, "tokens");
        sidebar.createLine(new Sidebar.ScoreboardLine("tokens", 
            Component.text("§b⚡ Tokens: §f" + formatNumber(tokens) + invisibleChars[2]), 12));
        
        // Souls
        double souls = currencyManager.getBalance(player, "souls");
        sidebar.createLine(new Sidebar.ScoreboardLine("souls", 
            Component.text("§d👻 Souls: §f" + formatNumber(souls) + invisibleChars[3]), 11));
        
        // Beacons
        double beacons = currencyManager.getBalance(player, "beacons");
        sidebar.createLine(new Sidebar.ScoreboardLine("beacons", 
            Component.text("§e🔶 Beacons: §f" + formatNumber(beacons) + invisibleChars[4]), 10));
        
        sidebar.createLine(new Sidebar.ScoreboardLine("empty2", Component.text("" + invisibleChars[5]), 9));
        
        // Backpack information
        Backpack backpack = backpackManager.getBackpack(player);
        if (backpack != null) {
            sidebar.createLine(new Sidebar.ScoreboardLine("backpack", 
                Component.text("§6📦 Backpack: §f" + backpack.getCurrentVolume() + "§7/§f" + backpack.getMaxVolume() + invisibleChars[6]), 8));
        } else {
            sidebar.createLine(new Sidebar.ScoreboardLine("backpack", 
                Component.text("§6📦 Backpack: §f0§7/§f1000" + invisibleChars[6]), 8));
        }
        
        // Blocks Mined
        long blocksMined = statsManager.getBlocksMined(player);
        sidebar.createLine(new Sidebar.ScoreboardLine("blocks", 
            Component.text("§7⛏ Blocks: §f" + formatNumber(blocksMined) + invisibleChars[7]), 7));
        
        sidebar.createLine(new Sidebar.ScoreboardLine("empty3", Component.text("" + invisibleChars[8]), 6));
        
        // Rank - ONLY show base rank (A-Z), not formatted rank with prefixes
        String baseRank = rankingManager.getRank(player); // This gets just the letter rank
        sidebar.createLine(new Sidebar.ScoreboardLine("rank", 
            Component.text("§e⭐ Rank: §f" + baseRank + invisibleChars[9]), 5));
        
        // Prestige
        int prestige = rankingManager.getPrestige(player);
        sidebar.createLine(new Sidebar.ScoreboardLine("prestige", 
            Component.text("§6🏆 Prestige: §f" + prestige + invisibleChars[10]), 4));
        
        // Rebirth
        int rebirth = rankingManager.getRebirth(player);
        sidebar.createLine(new Sidebar.ScoreboardLine("rebirth", 
            Component.text("§c⚡ Rebirth: §f" + rebirth + invisibleChars[11]), 3));
        
        // Ascension
        int ascension = rankingManager.getAscension(player);
        sidebar.createLine(new Sidebar.ScoreboardLine("ascension", 
            Component.text("§5✨ Ascension: §f" + ascension + invisibleChars[12]), 2));
        
        sidebar.createLine(new Sidebar.ScoreboardLine("empty4", Component.text("" + invisibleChars[13]), 1));
        
        // Online players
        int onlinePlayers = net.minestom.server.MinecraftServer.getConnectionManager().getOnlinePlayers().size();
        sidebar.createLine(new Sidebar.ScoreboardLine("online", 
            Component.text("§a👥 Online: §f" + onlinePlayers + invisibleChars[14]), 0));
        
    } catch (Exception e) {
        // Fallback to basic scoreboard
        clearAllLines(sidebar);
        sidebar.createLine(new Sidebar.ScoreboardLine("error", 
            Component.text("§cLoading..."), 0));
        System.err.println("[ScoreboardManager] Error updating scoreboard for " + player.getUsername() + ": " + e.getMessage());
        e.printStackTrace();
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

    public void updateAllScoreboards() {
        try {
            for (Player player : net.minestom.server.MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                updatePlayerScoreboard(player);
            }
        } catch (Exception e) {
            System.err.println("[ScoreboardManager] Error updating all scoreboards: " + e.getMessage());
        }
    }

    public void updatePlayerScoreboard(Player player) {
        try {
            Sidebar sidebar = playerScoreboards.get(player.getUuid().toString());
            if (sidebar != null) {
                updateScoreboard(player, sidebar);
            } else {
                createScoreboard(player);
            }
        } catch (Exception e) {
            System.err.println("[ScoreboardManager] Error updating scoreboard for " + player.getUsername() + ": " + e.getMessage());
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