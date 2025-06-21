package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.RankingManager;
import mythic.prison.managers.CurrencyManager;
import mythic.prison.data.player.PlayerProfile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class AdminCommand extends Command {

    public AdminCommand() {
        super("admin");

        // Arguments
        ArgumentWord actionArg = ArgumentType.Word("action").from("resetrank", "resetprestige", "resetrebirth", "resetascension", "resetall", "help");
        ArgumentString playerArg = ArgumentType.String("player");
        ArgumentString valueArg = ArgumentType.String("value");

        // /admin help
        setDefaultExecutor((sender, context) -> {
            showHelp(sender);
        });

        // /admin <action>
        addSyntax((sender, context) -> {
            String action = context.get(actionArg);
            if ("help".equals(action)) {
                showHelp(sender);
            } else {
                if (sender instanceof Player) {
                    ChatUtil.sendError((Player) sender, "Usage: /admin " + action + " <player> [value]");
                } else {
                    System.out.println("Usage: /admin " + action + " <player> [value]");
                }
            }
        }, actionArg);

        // /admin <action> <player>
        addSyntax((sender, context) -> {
            String action = context.get(actionArg);
            String playerName = context.get(playerArg);

            if (sender instanceof Player) {
                Player admin = (Player) sender;
                executeAction(admin, action, playerName, null);
            } else {
                executeActionConsole(action, playerName, null);
            }
        }, actionArg, playerArg);

        // /admin <action> <player> <value>
        addSyntax((sender, context) -> {
            String action = context.get(actionArg);
            String playerName = context.get(playerArg);
            String value = context.get(valueArg);

            if (sender instanceof Player) {
                Player admin = (Player) sender;
                executeAction(admin, action, playerName, value);
            } else {
                executeActionConsole(action, playerName, value);
            }
        }, actionArg, playerArg, valueArg);
    }

    private void showHelp(Object sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ChatUtil.sendMessage(player, "§c§l§m        §r §c§lADMIN COMMANDS §r§c§l§m        ");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lRank Management:");
            ChatUtil.sendMessage(player, "§7/admin resetrank <player> [rank] §8- Reset rank (default: A)");
            ChatUtil.sendMessage(player, "§7/admin resetprestige <player> [level] §8- Reset prestige (default: 0)");
            ChatUtil.sendMessage(player, "§7/admin resetrebirth <player> [level] §8- Reset rebirth (default: 0)");
            ChatUtil.sendMessage(player, "§7/admin resetascension <player> [level] §8- Reset ascension (default: 0)");
            ChatUtil.sendMessage(player, "§7/admin resetall <player> §8- Reset everything to defaults");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lExamples:");
            ChatUtil.sendMessage(player, "§e/admin resetrank Player Z §8- Set Players rank to Z");
            ChatUtil.sendMessage(player, "§e/admin resetprestige Player 5 §8- Set Players prestige to 5");
            ChatUtil.sendMessage(player, "§e/admin resetall Player §8- Reset all of Players progress");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§c§l§m                                        ");
        } else {
            // Handle console sender
            System.out.println("========== ADMIN COMMANDS ==========");
            System.out.println("Available commands:");
            System.out.println("- /admin resetrank <player> [rank] - Reset rank (default: A)");
            System.out.println("- /admin resetprestige <player> [level] - Reset prestige (default: 0)");
            System.out.println("- /admin resetrebirth <player> [level] - Reset rebirth (default: 0)");
            System.out.println("- /admin resetascension <player> [level] - Reset ascension (default: 0)");
            System.out.println("- /admin resetall <player> - Reset everything to defaults");
            System.out.println("===================================");
        }
    }

    private void executeAction(Player admin, String action, String targetName, String value) {
        Player targetPlayer = findPlayerByName(targetName);

        if (targetPlayer == null) {
            ChatUtil.sendError(admin, "Player not found: " + targetName);
            return;
        }

        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(targetPlayer);
        if (profile == null) {
            ChatUtil.sendError(admin, "Could not load player profile!");
            return;
        }

        RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

        switch (action.toLowerCase()) {
            case "resetrank":
                resetPlayerRank(admin, targetPlayer, profile, rankingManager, value);
                break;
            case "resetprestige":
                resetPlayerPrestige(admin, targetPlayer, profile, rankingManager, value);
                break;
            case "resetrebirth":
                resetPlayerRebirth(admin, targetPlayer, profile, rankingManager, value);
                break;
            case "resetascension":
                resetPlayerAscension(admin, targetPlayer, profile, rankingManager, value);
                break;
            case "resetall":
                resetPlayerAll(admin, targetPlayer, profile, rankingManager);
                break;
            default:
                ChatUtil.sendError(admin, "Unknown action: " + action);
                showHelp(admin);
                break;
        }
    }

    private void resetPlayerRank(Player admin, Player target, PlayerProfile profile, RankingManager rankingManager, String newRank) {
        if (newRank == null) {
            newRank = "A"; // Default rank
        }

        newRank = newRank.toUpperCase();

        if (!isValidRank(newRank)) {
            ChatUtil.sendError(admin, "Invalid rank! Valid ranks: A-Z");
            return;
        }

        String oldRank = profile.getCurrentRank();
        profile.setCurrentRank(newRank);
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        rankingManager.updatePlayerPrefix(target);

        ChatUtil.sendSuccess(admin, "Reset " + target.getUsername() + "'s rank from " + oldRank + " to " + newRank);
        ChatUtil.sendMessage(target, "§6Your rank has been reset to " + newRank + " by an admin!");
    }

    private void resetPlayerPrestige(Player admin, Player target, PlayerProfile profile, RankingManager rankingManager, String prestigeValue) {
        int newPrestige = 0; // Default prestige

        if (prestigeValue != null) {
            try {
                newPrestige = Integer.parseInt(prestigeValue);
                if (newPrestige < 0) {
                    ChatUtil.sendError(admin, "Prestige level must be 0 or higher!");
                    return;
                }
            } catch (NumberFormatException e) {
                ChatUtil.sendError(admin, "Invalid prestige level: " + prestigeValue);
                return;
            }
        }

        int oldPrestige = profile.getPrestige();
        profile.setPrestige(newPrestige);
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        rankingManager.updatePlayerPrefix(target);

        ChatUtil.sendSuccess(admin, "Reset " + target.getUsername() + "'s prestige from " + oldPrestige + " to " + newPrestige);
        ChatUtil.sendMessage(target, "§6Your prestige has been reset to " + newPrestige + " by an admin!");
    }

    private void resetPlayerRebirth(Player admin, Player target, PlayerProfile profile, RankingManager rankingManager, String rebirthValue) {
        int newRebirth = 0; // Default rebirth

        if (rebirthValue != null) {
            try {
                newRebirth = Integer.parseInt(rebirthValue);
                if (newRebirth < 0) {
                    ChatUtil.sendError(admin, "Rebirth level must be 0 or higher!");
                    return;
                }
            } catch (NumberFormatException e) {
                ChatUtil.sendError(admin, "Invalid rebirth level: " + rebirthValue);
                return;
            }
        }

        int oldRebirth = profile.getRebirth();
        profile.setRebirth(newRebirth);
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        rankingManager.updatePlayerPrefix(target);

        ChatUtil.sendSuccess(admin, "Reset " + target.getUsername() + "'s rebirth from " + oldRebirth + " to " + newRebirth);
        ChatUtil.sendMessage(target, "§cYour rebirth has been reset to " + newRebirth + " by an admin!");
    }

    private void resetPlayerAscension(Player admin, Player target, PlayerProfile profile, RankingManager rankingManager, String ascensionValue) {
        int newAscension = 0; // Default ascension

        if (ascensionValue != null) {
            try {
                newAscension = Integer.parseInt(ascensionValue);
                if (newAscension < 0) {
                    ChatUtil.sendError(admin, "Ascension level must be 0 or higher!");
                    return;
                }
            } catch (NumberFormatException e) {
                ChatUtil.sendError(admin, "Invalid ascension level: " + ascensionValue);
                return;
            }
        }

        int oldAscension = profile.getAscension();
        profile.setAscension(newAscension);
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        rankingManager.updatePlayerPrefix(target);

        ChatUtil.sendSuccess(admin, "Reset " + target.getUsername() + "'s ascension from " + oldAscension + " to " + newAscension);
        ChatUtil.sendMessage(target, "§5Your ascension has been reset to " + newAscension + " by an admin!");
    }

    private void resetPlayerAll(Player admin, Player target, PlayerProfile profile, RankingManager rankingManager) {
        String oldRank = profile.getCurrentRank();
        int oldPrestige = profile.getPrestige();
        int oldRebirth = profile.getRebirth();
        int oldAscension = profile.getAscension();

        // Reset everything to defaults
        profile.setCurrentRank("A");
        profile.setPrestige(0);
        profile.setRebirth(0);
        profile.setAscension(0);

        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
        rankingManager.updatePlayerPrefix(target);

        // Success messages
        ChatUtil.sendSuccess(admin, "§a§lFull Reset Complete!");
        ChatUtil.sendMessage(admin, "§7Player: §f" + target.getUsername());
        ChatUtil.sendMessage(admin, "§7Rank: §e" + oldRank + " §8→ §eA");
        ChatUtil.sendMessage(admin, "§7Prestige: §6" + oldPrestige + " §8→ §60");
        ChatUtil.sendMessage(admin, "§7Rebirth: §c" + oldRebirth + " §8→ §c0");
        ChatUtil.sendMessage(admin, "§7Ascension: §5" + oldAscension + " §8→ §50");

        // Notify target player
        ChatUtil.sendMessage(target, "§c§l[ADMIN RESET]");
        ChatUtil.sendMessage(target, "§7Your entire progression has been reset by an admin!");
        ChatUtil.sendMessage(target, "§7You can now start fresh with /rankup");
    }

    private Player findPlayerByName(String name) {
        var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
        for (Player player : connectionManager.getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private boolean isValidRank(String rank) {
        if (rank == null || rank.length() != 1) return false;
        char c = rank.toUpperCase().charAt(0);
        return c >= 'A' && c <= 'Z';
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new AdminCommand());
    }

private void executeActionConsole(String action, String targetName, String value) {
    Player targetPlayer = findPlayerByName(targetName);

    if (targetPlayer == null) {
        System.out.println("Player not found: " + targetName);
        return;
    }

    PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(targetPlayer);
    if (profile == null) {
        System.out.println("Could not load player profile!");
        return;
    }

    RankingManager rankingManager = MythicPrison.getInstance().getRankingManager();

    switch (action.toLowerCase()) {
        case "resetrank":
            resetPlayerRankConsole(targetPlayer, profile, rankingManager, value);
            break;
        case "resetprestige":
            resetPlayerPrestigeConsole(targetPlayer, profile, rankingManager, value);
            break;
        case "resetrebirth":
            resetPlayerRebirthConsole(targetPlayer, profile, rankingManager, value);
            break;
        case "resetascension":
            resetPlayerAscensionConsole(targetPlayer, profile, rankingManager, value);
            break;
        case "resetall":
            resetPlayerAllConsole(targetPlayer, profile, rankingManager);
            break;
        default:
            System.out.println("Unknown action: " + action);
            showHelp(null);
            break;
    }
}

private void resetPlayerRankConsole(Player target, PlayerProfile profile, RankingManager rankingManager, String newRank) {
    if (newRank == null) {
        newRank = "A"; // Default rank
    }

    newRank = newRank.toUpperCase();
    
    if (!isValidRank(newRank)) {
        System.out.println("Invalid rank! Valid ranks: A-Z");
        return;
    }

    String oldRank = profile.getCurrentRank();
    profile.setCurrentRank(newRank);
    MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    rankingManager.updatePlayerPrefix(target);

    System.out.println("Reset " + target.getUsername() + "'s rank from " + oldRank + " to " + newRank);
    ChatUtil.sendMessage(target, "§6Your rank has been reset to " + newRank + " by console!");
}

// Add similar console methods for prestige, rebirth, ascension, and resetall...
private void resetPlayerPrestigeConsole(Player target, PlayerProfile profile, RankingManager rankingManager, String prestigeValue) {
    int newPrestige = 0;
    if (prestigeValue != null) {
        try {
            newPrestige = Integer.parseInt(prestigeValue);
            if (newPrestige < 0) {
                System.out.println("Prestige level must be 0 or higher!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid prestige level: " + prestigeValue);
            return;
        }
    }

    int oldPrestige = profile.getPrestige();
    profile.setPrestige(newPrestige);
    MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    rankingManager.updatePlayerPrefix(target);

    System.out.println("Reset " + target.getUsername() + "'s prestige from " + oldPrestige + " to " + newPrestige);
    ChatUtil.sendMessage(target, "§6Your prestige has been reset to " + newPrestige + " by console!");
}

private void resetPlayerRebirthConsole(Player target, PlayerProfile profile, RankingManager rankingManager, String rebirthValue) {
    int newRebirth = 0;
    if (rebirthValue != null) {
        try {
            newRebirth = Integer.parseInt(rebirthValue);
            if (newRebirth < 0) {
                System.out.println("Rebirth level must be 0 or higher!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid rebirth level: " + rebirthValue);
            return;
        }
    }

    int oldRebirth = profile.getRebirth();
    profile.setRebirth(newRebirth);
    MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    rankingManager.updatePlayerPrefix(target);

    System.out.println("Reset " + target.getUsername() + "'s rebirth from " + oldRebirth + " to " + newRebirth);
    ChatUtil.sendMessage(target, "§cYour rebirth has been reset to " + newRebirth + " by console!");
}

private void resetPlayerAscensionConsole(Player target, PlayerProfile profile, RankingManager rankingManager, String ascensionValue) {
    int newAscension = 0;
    if (ascensionValue != null) {
        try {
            newAscension = Integer.parseInt(ascensionValue);
            if (newAscension < 0) {
                System.out.println("Ascension level must be 0 or higher!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ascension level: " + ascensionValue);
            return;
        }
    }

    int oldAscension = profile.getAscension();
    profile.setAscension(newAscension);
    MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    rankingManager.updatePlayerPrefix(target);

    System.out.println("Reset " + target.getUsername() + "'s ascension from " + oldAscension + " to " + newAscension);
    ChatUtil.sendMessage(target, "§5Your ascension has been reset to " + newAscension + " by console!");
}

private void resetPlayerAllConsole(Player target, PlayerProfile profile, RankingManager rankingManager) {
    String oldRank = profile.getCurrentRank();
    int oldPrestige = profile.getPrestige();
    int oldRebirth = profile.getRebirth();
    int oldAscension = profile.getAscension();

    // Reset everything to defaults
    profile.setCurrentRank("A");
    profile.setPrestige(0);
    profile.setRebirth(0);
    profile.setAscension(0);

    MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    rankingManager.updatePlayerPrefix(target);

    // Console output
    System.out.println("=== FULL RESET COMPLETE ===");
    System.out.println("Player: " + target.getUsername());
    System.out.println("Rank: " + oldRank + " → A");
    System.out.println("Prestige: " + oldPrestige + " → 0");
    System.out.println("Rebirth: " + oldRebirth + " → 0");
    System.out.println("Ascension: " + oldAscension + " → 0");

    // Notify target player
    ChatUtil.sendMessage(target, "§c§l[CONSOLE RESET]");
    ChatUtil.sendMessage(target, "§7Your entire progression has been reset by console!");
    ChatUtil.sendMessage(target, "§7You can now start fresh with /rankup");
}
}