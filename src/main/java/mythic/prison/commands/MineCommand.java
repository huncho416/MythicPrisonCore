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
            showMineHelp(player);
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
                case "go" -> executeGo(player);
                case "info" -> showMineInfo(player);
                case "upgrade" -> showUpgradeMenu(player);
                case "public" -> executeTogglePublic(player);
                case "list" -> executeList(player);
                case "visit" -> ChatUtil.sendError(player, "Usage: /mine visit <player>");
                case "help" -> showMineHelp(player);
                default -> {
                    ChatUtil.sendError(player, "Unknown command! Use /mine help for available commands.");
                    showMineHelp(player);
                }
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
                default -> {
                    ChatUtil.sendError(player, "Unknown command! Use /mine help for available commands.");
                    showMineHelp(player);
                }
            }
        }, actionArg, stringArg);
    }
    
    private void showMineHelp(Player player) {
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§f§l§m            §r §6§lMINE COMMANDS §r§f§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lNavigation:");
        ChatUtil.sendMessage(player, "§d/mine go §7- Teleport to your mine");
        ChatUtil.sendMessage(player, "§d/mine visit <player> §7- Visit another player's mine");
        ChatUtil.sendMessage(player, "§d/mine list §7- List all public mines");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lManagement:");
        ChatUtil.sendMessage(player, "§d/mine info §7- View your mine information");
        ChatUtil.sendMessage(player, "§d/mine rename <name> §7- Rename your mine");
        ChatUtil.sendMessage(player, "§d/mine public §7- Toggle public/private access");
        ChatUtil.sendMessage(player, "§d/mine tax <percent> §7- Set visitor tax rate (0-100%)");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lPlayer Access:");
        ChatUtil.sendMessage(player, "§d/mine add <player> §7- Allow player to access your mine");
        ChatUtil.sendMessage(player, "§d/mine remove <player> §7- Remove player access");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§e§lUpgrades:");
        ChatUtil.sendMessage(player, "§d/mine upgrade §7- View available upgrades");
        ChatUtil.sendMessage(player, "§d/mine upgrade size §7- Upgrade mine size");
        ChatUtil.sendMessage(player, "§d/mine upgrade beacons §7- Upgrade beacon multiplier");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§7§oNote: Use §d/mine go §7§oto teleport to your mine!");
        ChatUtil.sendMessage(player, "§f§l§m                                                    ");
    }
    
    private void executeGo(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();
        
        // Auto-teleport to player's mine (create if doesn't exist)
        if (!mineManager.hasPlayerMine(player)) {
            mineManager.createPrivateMine(player);
            ChatUtil.sendSuccess(player, "Created your personal mine!");
            
            // Add a longer delay to ensure mine is fully created before teleporting
            MythicPrison.getInstance().getScheduler().schedule(() -> {
                // Get the mine after creation
                PrivateMine mine = mineManager.getPlayerMine(player);
                if (mine != null && schematicManager != null) {
                    String mineWorldName = "mine_" + player.getUsername().toLowerCase();
                    
                    // Track player in world BEFORE teleporting
                    schematicManager.trackPlayerInWorld(player, mineWorldName);

                    
                    // Now teleport
                    mineManager.teleportToMine(player);
                    
                    // Add another small delay to ensure teleportation completed, then track again
                    MythicPrison.getInstance().getScheduler().schedule(() -> {
                        schematicManager.trackPlayerInWorld(player, mineWorldName);

                        
                        // Force update the world tracking
                        String currentWorld = schematicManager.getPlayerWorld(player);

                    }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            }, 200, java.util.concurrent.TimeUnit.MILLISECONDS); // Increased delay
        } else {
            // Get the mine first
            PrivateMine mine = mineManager.getPlayerMine(player);
            if (mine != null && schematicManager != null) {
                String mineWorldName = "mine_" + player.getUsername().toLowerCase();
                
                // Track player in world BEFORE teleporting
                schematicManager.trackPlayerInWorld(player, mineWorldName);

                
                // Now teleport
                mineManager.teleportToMine(player);
                
                // Add a small delay to ensure teleportation completed, then track again
                MythicPrison.getInstance().getScheduler().schedule(() -> {
                    schematicManager.trackPlayerInWorld(player, mineWorldName);

                    
                    // Force update the world tracking
                    String currentWorld = schematicManager.getPlayerWorld(player);

                }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
            } else {
                // Fallback - just teleport
                mineManager.teleportToMine(player);
            }
            
            ChatUtil.sendSuccess(player, "Teleported to your mine!");
        }
    }
    
    private void showMineInfo(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
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
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
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
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
            return;
        }
        
        mine.setMineName(name);
        ChatUtil.sendSuccess(player, "Renamed your mine to: §e" + name);
    }
    
    private void executeTogglePublic(Player player) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
            return;
        }
        
        mine.setPublic(!mine.isPublic());
        ChatUtil.sendSuccess(player, "Your mine is now " + (mine.isPublic() ? "§apublic" : "§cprivate") + "§a!");
    }
    
    private void executeSetTax(Player player, String percentStr) {
        MineManager mineManager = MythicPrison.getInstance().getMineManager();
        PrivateMine mine = mineManager.getPlayerMine(player);
        
        if (mine == null) {
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
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
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
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
            ChatUtil.sendError(player, "You don't have a mine! Use /mine go to create one.");
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
        ChatUtil.sendSuccess(player, "Teleported to " + targetPlayerName + "'s mine!");
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
        ChatUtil.sendMessage(player, "§7Use §d/mine visit <player> §7to visit their mine!");
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