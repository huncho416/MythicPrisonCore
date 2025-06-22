package mythic.prison.listeners;

import mythic.prison.MythicPrison;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.MinecraftServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatListener {

    public static void register() {
        var globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Handle chat messages
        globalEventHandler.addListener(PlayerChatEvent.class, event -> {
            handlePlayerChat(event);
        });

        // Update tab list when player spawns
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            // Keep only essential spawn logic, remove tab list updates
        });
    }

    private static void handlePlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getRawMessage();

        // Cancel the default chat message
        event.setCancelled(true);

        // Check if player has chat enabled (if you have this feature)
        try {
            var profileManager = MythicPrison.getInstance().getProfileManager();
            if (profileManager != null) {
                var profile = profileManager.getProfile(player);
                if (profile != null && !profile.isChatEnabled()) {
                    player.sendMessage("§cYour chat is disabled! Use §f/chat §cto enable it.");
                    return;
                }
            }
        } catch (Exception e) {
            // Profile system might not be ready
        }

        // Format and broadcast the message
        String formattedMessage = formatChatMessage(player, message);

        // Send to all online players
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(formattedMessage);
        }

        // Log to console
        System.out.println("[Chat] " + player.getUsername() + ": " + message);
    }

    /**
     * Formats a chat message with rank letter, prestige, rebirth, and ascension
     * Format: [A✦5⚡3⭐2] PlayerName: message
     */
    public static String formatChatMessage(Player player, String message) {
        try {
            var rankingManager = MythicPrison.getInstance().getRankingManager();
            if (rankingManager == null) {
                return "§f" + player.getUsername() + "§7: §f" + message;
            }

            // Get player info
            String rank = rankingManager.getRank(player);
            int prestige = rankingManager.getPrestige(player);
            int rebirth = rankingManager.getRebirth(player);
            int ascension = rankingManager.getAscension(player);

            // Build the prefix with brackets
            StringBuilder prefix = new StringBuilder("§8[");

            // Add rank with color
            String rankColor = getRankColor(rank);
            prefix.append(rankColor).append(rank);

            // Add prestige if > 0
            if (prestige > 0) {
                prefix.append("§6✦").append(prestige);
            }

            // Add rebirth if > 0
            if (rebirth > 0) {
                prefix.append("§c⚡").append(rebirth);
            }

            // Add ascension if > 0
            if (ascension > 0) {
                prefix.append("§5⭐").append(ascension);
            }

            prefix.append("§8] ");

            // Format the complete message with white player name
            return prefix + "§f" + player.getUsername() + "§7: §f" + message;

        } catch (Exception e) {
            System.err.println("Error formatting chat message: " + e.getMessage());
            // Return basic format if something fails
            return "§f" + player.getUsername() + "§7: §f" + message;
        }
    }

    /**
     * Updates a single player's tab list display name
     * Format: [A✦5⚡3⭐2] PlayerName
     */
    public static void updatePlayerTabList(Player player) {
        try {
            var rankingManager = MythicPrison.getInstance().getRankingManager();
            if (rankingManager == null) {
                // Fallback - just set the original username in white
                Component fallbackName = Component.text("§f" + player.getUsername());
                player.setDisplayName(fallbackName);
                System.out.println("[TabList] RankingManager null, using fallback for: " + player.getUsername());
                return;
            }

            // Get player info
            String rank = rankingManager.getRank(player);
            int prestige = rankingManager.getPrestige(player);
            int rebirth = rankingManager.getRebirth(player);
            int ascension = rankingManager.getAscension(player);

            // Debug: Print what we're getting
            System.out.println("[TabList Debug] " + player.getUsername() + " - Rank: " + rank + ", Prestige: " + prestige + ", Rebirth: " + rebirth + ", Ascension: " + ascension);

            // Build the rank prefix in brackets - ALWAYS show brackets with rank info
            StringBuilder rankPrefix = new StringBuilder();
            rankPrefix.append("§8[");

            // Add rank with color (but don't let it affect player name)
            String rankColor = getRankColor(rank);
            rankPrefix.append(rankColor).append(rank);

            // Add prestige if > 0
            if (prestige > 0) {
                rankPrefix.append("§6✦").append(prestige);
            }

            // Add rebirth if > 0
            if (rebirth > 0) {
                rankPrefix.append("§c⚡").append(rebirth);
            }

            // Add ascension if > 0
            if (ascension > 0) {
                rankPrefix.append("§5⭐").append(ascension);
            }

            rankPrefix.append("§8] ");

            // IMPORTANT: Always preserve white color for player name, reset color before name
            rankPrefix.append("§f").append(player.getUsername());

            String finalDisplayName = rankPrefix.toString();

            // Convert to component while preserving formatting
            Component displayComponent = LegacyComponentSerializer.legacySection().deserialize(finalDisplayName);

            // Set display name (this affects both chat and tab list in Minestom)
            player.setDisplayName(displayComponent);

            // Debug output
            System.out.println("[TabList] Updated " + player.getUsername() + " to: " + finalDisplayName);

        } catch (Exception e) {
            System.err.println("Error updating tab list for " + player.getUsername() + ": " + e.getMessage());
            e.printStackTrace();

            // Fallback - just set the original username in white
            try {
                Component fallbackName = Component.text("§f" + player.getUsername());
                player.setDisplayName(fallbackName);
            } catch (Exception fallbackException) {
                System.err.println("Fallback also failed: " + fallbackException.getMessage());
            }
        }
    }

    /**
     * Updates tab header and footer for a specific player
     */
    public static void updateTabHeaderFooter(Player player) {
        try {
            // Get server info
            String serverName = "Prison"; // You can make this configurable
            int currentServerPlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            int globalPlayers = currentServerPlayers; // For now, same as current server. You can implement network-wide counting later
            int playerPing = player.getLatency();

            // Build header
            StringBuilder header = new StringBuilder();
            header.append("§d§l                    Mythic§f§lPvP                    \n"); // Bold mythic in light pink, PvP in bold white, centered
            header.append("\n"); // Blank line
            header.append("§d                Connected to: §f").append(serverName).append("                \n"); // Light pink + white
            header.append("§7                play.mythicpvp.net                "); // Grey

            // Build footer
            StringBuilder footer = new StringBuilder();
            footer.append("§d").append(serverName).append(" §8* §f").append(playerPing).append("ms §8* §dstore.mythicpvp.net\n"); // Pink + dark grey + white + dark grey + light pink
            footer.append("§d").append(globalPlayers).append(" §fGlobal §7").append(currentServerPlayers).append(" on server"); // Light pink + white + grey

            // Convert to components
            Component headerComponent = LegacyComponentSerializer.legacySection().deserialize(header.toString());
            Component footerComponent = LegacyComponentSerializer.legacySection().deserialize(footer.toString());

            // Set tab header and footer
            player.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);

        } catch (Exception e) {
            System.err.println("Error updating tab header/footer for " + player.getUsername() + ": " + e.getMessage());
        }
    }

    /**
     * Updates tab header and footer for all online players
     */
    public static void updateAllTabHeadersFooters() {
        try {
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                updateTabHeaderFooter(onlinePlayer);
            }
        } catch (Exception e) {
            System.err.println("Error updating all tab headers/footers: " + e.getMessage());
        }
    }

    /**
     * Updates tab list for all online players
     */
    public static void updateAllTabLists() {
        try {
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                updatePlayerTabList(onlinePlayer);
            }
        } catch (Exception e) {
            System.err.println("Error updating all tab lists: " + e.getMessage());
        }
    }

    /**
     * Gets rank color based on rank letter
     */
    public static String getRankColor(String rank) {
        if (rank == null) return "§7";

        return switch (rank.toUpperCase()) {
            case "A", "B", "C" -> "§a"; // Green for starter ranks
            case "D", "E", "F" -> "§2"; // Dark green
            case "G", "H", "I" -> "§e"; // Yellow
            case "J", "K", "L" -> "§6"; // Gold
            case "M", "N", "O" -> "§c"; // Red
            case "P", "Q", "R" -> "§4"; // Dark red
            case "S", "T", "U" -> "§d"; // Light purple
            case "V", "W", "X" -> "§5"; // Purple
            case "Y" -> "§b"; // Aqua
            case "Z" -> "§f"; // White for max rank
            default -> "§7"; // Gray fallback
        };
    }
}