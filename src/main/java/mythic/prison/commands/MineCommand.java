package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.mine.PrivateMine;
import mythic.prison.managers.MineManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;
import net.minestom.server.MinecraftServer;
import mythic.prison.managers.SchematicWorldManager;

public class MineCommand extends Command {
    
    public MineCommand() {
        super("mine");
        
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }
            
            Player player = (Player) sender;
            MineManager mineManager = MythicPrison.getInstance().getMineManager();
            SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();
            
            // Auto-teleport to player's mine (create if doesn't exist)
            if (!mineManager.hasPlayerMine(player)) {
                mineManager.createPrivateMine(player);
                ChatUtil.sendSuccess(player, "Created your personal mine!");
                
                // Add a small delay to ensure mine is fully created before teleporting
                MythicPrison.getInstance().getScheduler().schedule(() -> {
                    mineManager.teleportToMine(player);
                    
                    // Ensure player is properly tracked in their mine world
                    PrivateMine mine = mineManager.getPlayerMine(player);
                    if (mine != null && schematicManager != null) {
                        String mineWorldName = "mine_" + player.getUsername().toLowerCase();
                        schematicManager.trackPlayerInWorld(player, mineWorldName);
                        System.out.println("[MineCommand] Tracked player " + player.getUsername() + " in world: " + mineWorldName);
                    }
                }, 100, java.util.concurrent.TimeUnit.MILLISECONDS);
            } else {
                mineManager.teleportToMine(player);
                
                // Ensure player is properly tracked in their mine world
                PrivateMine mine = mineManager.getPlayerMine(player);
                if (mine != null && schematicManager != null) {
                    String mineWorldName = "mine_" + player.getUsername().toLowerCase();
                    schematicManager.trackPlayerInWorld(player, mineWorldName);
                    System.out.println("[MineCommand] Tracked player " + player.getUsername() + " in world: " + mineWorldName);
                }
            }
        });
        
        // Subcommands
        ArgumentWord actionArg = ArgumentType.Word("action");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }
            
            Player player = (Player) sender;
            String action = context.get(actionArg);
            
            switch (action.toLowerCase()) {
                case "info" -> showMineInfo(player);
                case "upgrade" -> showUpgradeMenu(player);
                case "public" -> executeTogglePublic(player);
                case "list" -> executeList(player);
                case "visit" -> ChatUtil.sendError(player, "Usage: /mine visit <player>");
                default -> ChatUtil.sendError(player, "Unknown command! Use /mine help for available commands.");
            }
        }, actionArg);
        
        // Commands with string arguments
        ArgumentString stringArg = ArgumentType.String("value");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }
            
            Player player = (Player) sender;
            String action = context.get(actionArg);
            String value = context.get(stringArg);
            
            switch (action.toLowerCase()) {
                case "rename" -> executeRename(player, value);
                case "tax" -> executeSetTax(player, value);
                case "add" -> executeAddPlayer(player, value);
                case "remove" -> executeRemovePlayer(player, value);
                case "visit" -> executeVisit(player, value);
                case "upgrade" -> executeUpgrade(player, value);
                default -> ChatUtil.sendError(player, "Unknown command! Use /mine help for available commands.");
            }
        }, actionArg, stringArg);
    }
    
    private void showMineInfo(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine! Use /mine to create one.");
            return;
        }
        
        ChatUtil.sendMessage(player, "§6§l=== Mine Information ===");
        ChatUtil.sendMessage(player, "§7Name: §e" + mine.getMineName());
        ChatUtil.sendMessage(player, "§7Size Level: §a" + mine.getSizeLevel() + " §7(§e" + mine.getMineSize() + "x" + mine.getMineSize() + "§7)");
        ChatUtil.sendMessage(player, "§7Beacon Level: §d" + mine.getBeaconLevel() + " §7(§e+" + (mine.getBeaconLevel() * 10) + "% §7multiplier)");
        ChatUtil.sendMessage(player, "§7Public: " + (mine.isPublic() ? "§aYes" : "§cNo"));
        ChatUtil.sendMessage(player, "§7Tax Rate: §e" + (mine.getTaxRate() * 100) + "%");
        ChatUtil.sendMessage(player, "§7Allowed Players: §e" + mine.getAllowedPlayers().size());
        ChatUtil.sendMessage(player, "§7Multiplier: §a" + mine.getMultiplier() + "x");
    }
    
    private void showUpgradeMenu(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine! Use /mine to create one.");
            return;
        }
        
        ChatUtil.sendMessage(player, "§6§l=== Mine Upgrades ===");
        
        if (mine.canUpgradeSize()) {
            ChatUtil.sendMessage(player, "§e/mine upgrade size §7- Upgrade size (Cost: §a$" + 
                String.format("%.0f", mine.getSizeUpgradeCost()) + "§7)");
        } else {
            ChatUtil.sendMessage(player, "§7Size: §cMax Level Reached");
        }
        
        if (mine.canUpgradeBeacons()) {
            ChatUtil.sendMessage(player, "§e/mine upgrade beacons §7- Upgrade beacons (Cost: §a$" + 
                String.format("%.0f", mine.getBeaconUpgradeCost()) + "§7)");
        } else {
            ChatUtil.sendMessage(player, "§7Beacons: §cMax Level Reached");
        }
    }
    
    private void executeUpgrade(Player player, String type) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        
        switch (type.toLowerCase()) {
            case "size" -> {
                if (mineManager.upgradeSize(player)) {
                    ChatUtil.sendSuccess(player, "Successfully upgraded your mine size!");
                } else {
                    ChatUtil.sendError(player, "Cannot upgrade! Check if you have enough money or reached max level.");
                }
            }
            case "beacons", "beacon" -> {
                if (mineManager.upgradeBeacons(player)) {
                    ChatUtil.sendSuccess(player, "Successfully upgraded your mine beacons!");
                } else {
                    ChatUtil.sendError(player, "Cannot upgrade! Check if you have enough money or reached max level.");
                }
            }
            default -> ChatUtil.sendError(player, "Invalid upgrade type! Use 'size' or 'beacons'.");
        }
    }
    
    private void executeRename(Player player, String name) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine!");
            return;
        }
        
        mine.setMineName(name);
        ChatUtil.sendSuccess(player, "Renamed your mine to: §e" + name);
    }
    
    private void executeTogglePublic(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine!");
            return;
        }
        
        mine.setPublic(!mine.isPublic());
        ChatUtil.sendSuccess(player, "Your mine is now " + (mine.isPublic() ? "§apublic" : "§cprivate") + "§a!");
    }
    
    private void executeSetTax(Player player, String percentStr) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine!");
            return;
        }
        
        try {
            double percent = Double.parseDouble(percentStr);
            if (percent < 0 || percent > 100) {
                ChatUtil.sendError(player, "Tax rate must be between 0 and 100!");
                return;
            }
            
            mine.setTaxRate(percent / 100.0);
            ChatUtil.sendSuccess(player, "Set tax rate to " + percent + "%");
        } catch (NumberFormatException e) {
            ChatUtil.sendError(player, "Invalid number format!");
        }
    }
    
    private void executeAddPlayer(Player player, String playerName) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine!");
            return;
        }
        
        Player target = findPlayerByName(playerName);
        if (target == null) {
            ChatUtil.sendError(player, "Player '" + playerName + "' not found!");
            return;
        }
        
        mine.addAllowedPlayer(target.getUuid().toString());
        ChatUtil.sendSuccess(player, "Added " + target.getUsername() + " to your mine!");
        ChatUtil.sendMessage(target, "§aYou've been added to " + player.getUsername() + "'s mine!");
    }
    
    private void executeRemovePlayer(Player player, String playerName) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine!");
            return;
        }
        
        Player target = findPlayerByName(playerName);
        if (target == null) {
            ChatUtil.sendError(player, "Player '" + playerName + "' not found!");
            return;
        }
        
        mine.removeAllowedPlayer(target.getUuid().toString());
        ChatUtil.sendSuccess(player, "Removed " + target.getUsername() + " from your mine!");
        ChatUtil.sendMessage(target, "§cYou've been removed from " + player.getUsername() + "'s mine!");
    }
    
    private void executeVisit(Player player, String targetPlayerName) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        
        Player targetPlayer = findPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            ChatUtil.sendError(player, "Player '" + targetPlayerName + "' not found!");
            return;
        }
        
        PrivateMine targetMine = mineManager.getPlayerMine(targetPlayer);
        if (targetMine == null) {
            ChatUtil.sendError(player, targetPlayerName + " doesn't have a mine!");
            return;
        }
        
        if (!targetMine.canPlayerAccess(player.getUuid().toString())) {
            ChatUtil.sendError(player, "You don't have permission to access " + targetPlayerName + "'s mine!");
            return;
        }
        
        targetMine.teleportPlayer(player);
    }
    
    private void executeList(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        var publicMines = mineManager.getPublicMines();
        
        if (publicMines.isEmpty()) {
            ChatUtil.sendMessage(player, "§7No public mines available.");
            return;
        }
        
        ChatUtil.sendMessage(player, "§6§lPublic Mines:");
        for (PrivateMine mine : publicMines) {
            ChatUtil.sendMessage(player, "§e" + mine.getOwnerName() + " §7- §a" + mine.getMineName());
        }
    }
    
    private Player findPlayerByName(String playerName) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equalsIgnoreCase(playerName)) {
                return onlinePlayer;
            }
        }
        return null;
    }
}