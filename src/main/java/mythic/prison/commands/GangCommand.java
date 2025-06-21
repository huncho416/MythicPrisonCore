package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.gangs.Gang;
import mythic.prison.managers.GangManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class GangCommand extends Command {

    public GangCommand() {
        super("gang", "g");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeGangInfo(sender);
        });

        // /gang create <name>
        ArgumentWord createArg = ArgumentType.Word("create").from("create");
        ArgumentString nameArg = ArgumentType.String("name");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String gangName = context.get(nameArg);
            executeCreate(sender, gangName);
        }, createArg, nameArg);

        // /gang disband
        ArgumentWord disbandArg = ArgumentType.Word("disband").from("disband");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeDisband(sender);
        }, disbandArg);

        // /gang invite <player>
        ArgumentWord inviteArg = ArgumentType.Word("invite").from("invite");
        ArgumentString playerArg = ArgumentType.String("player");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String targetName = context.get(playerArg);
            executeInvite(sender, targetName);
        }, inviteArg, playerArg);

        // /gang accept <gangName>
        ArgumentWord acceptArg = ArgumentType.Word("accept").from("accept");
        ArgumentString gangNameArg = ArgumentType.String("gangName");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String gangName = context.get(gangNameArg);
            executeAccept(sender, gangName);
        }, acceptArg, gangNameArg);

        // /gang leave
        ArgumentWord leaveArg = ArgumentType.Word("leave").from("leave");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeLeave(sender);
        }, leaveArg);

        // /gang kick <player>
        ArgumentWord kickArg = ArgumentType.Word("kick").from("kick");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String targetName = context.get(playerArg);
            executeKick(sender, targetName);
        }, kickArg, playerArg);

        // /gang promote <player>
        ArgumentWord promoteArg = ArgumentType.Word("promote").from("promote");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String targetName = context.get(playerArg);
            executePromote(sender, targetName);
        }, promoteArg, playerArg);

        // /gang demote <player>
        ArgumentWord demoteArg = ArgumentType.Word("demote").from("demote");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String targetName = context.get(playerArg);
            executeDemote(sender, targetName);
        }, demoteArg, playerArg);

        // /gang deposit <amount>
        ArgumentWord depositArg = ArgumentType.Word("deposit").from("deposit");
        ArgumentString amountArg = ArgumentType.String("amount");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String amountStr = context.get(amountArg);
            try {
                double amount = Double.parseDouble(amountStr);
                executeDeposit(sender, amount);
            } catch (NumberFormatException e) {
                Player player = (Player) sender;
                ChatUtil.sendError(player, "Invalid amount: " + amountStr);
            }
        }, depositArg, amountArg);

        // /gang withdraw <amount>
        ArgumentWord withdrawArg = ArgumentType.Word("withdraw").from("withdraw");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String amountStr = context.get(amountArg);
            try {
                double amount = Double.parseDouble(amountStr);
                executeWithdraw(sender, amount);
            } catch (NumberFormatException e) {
                Player player = (Player) sender;
                ChatUtil.sendError(player, "Invalid amount: " + amountStr);
            }
        }, withdrawArg, amountArg);

        // /gang list
        ArgumentWord listArg = ArgumentType.Word("list").from("list");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeList(sender);
        }, listArg);

        // /gang top
        ArgumentWord topArg = ArgumentType.Word("top").from("top");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeTop(sender);
        }, topArg);

        // /gang chat <message...>
        ArgumentWord chatArg = ArgumentType.Word("chat").from("chat", "c");
        ArgumentStringArray messageArg = ArgumentType.StringArray("message");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            String[] messageArray = context.get(messageArg);
            executeChat(sender, messageArray);
        }, chatArg, messageArg);
    }

    private static void executeGangInfo(Object player) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        Gang gang = gangManager.getPlayerGang(p);

        if (gang == null) {
            ChatUtil.sendMessage(p, "§d§l§m            §r §d§lGANG COMMANDS §r§d§l§m            ");
            ChatUtil.sendMessage(p, "");
            ChatUtil.sendMessage(p, "§d/gang create <name> §7- Create a new gang");
            ChatUtil.sendMessage(p, "§d/gang invite <player> §7- Invite a player");
            ChatUtil.sendMessage(p, "§d/gang accept <gang> §7- Accept gang invitation");
            ChatUtil.sendMessage(p, "§d/gang leave §7- Leave your gang");
            ChatUtil.sendMessage(p, "§d/gang list §7- List all gangs");
            ChatUtil.sendMessage(p, "§d/gang top §7- Show top gangs");
            ChatUtil.sendMessage(p, "");
            ChatUtil.sendMessage(p, "§d§l§m                                                    ");
            return;
        }

        // Show gang info using the manager's method
        gangManager.showGangInfo(p, gang.getName());
    }

    private static void executeCreate(Object player, String gangName) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.createGang(p, gangName);
    }

    private static void executeDisband(Object player) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.disbandGang(p);
    }

    private static void executeInvite(Object player, String targetName) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();

        Object target = findPlayerByName(targetName);
        if (target == null) {
            ChatUtil.sendError(p, "Player '" + targetName + "' not found!");
            return;
        }

        gangManager.invitePlayer(p, target);
    }

    private static void executeAccept(Object player, String gangName) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.acceptInvite(p, gangName);
    }

    private static void executeLeave(Object player) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.leaveGang(p);
    }

    private static void executeKick(Object player, String targetName) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.kickPlayer(p, targetName);
    }

    private static void executePromote(Object player, String targetName) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.promotePlayer(p, targetName);
    }

    private static void executeDemote(Object player, String targetName) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        gangManager.demotePlayer(p, targetName);
    }

    private static void executeDeposit(Object player, double amount) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        Gang gang = gangManager.getPlayerGang(p);

        if (gang == null) {
            ChatUtil.sendError(p, "You are not in a gang!");
            return;
        }

        if (amount <= 0) {
            ChatUtil.sendError(p, "Amount must be positive!");
            return;
        }

        double playerBalance = MythicPrison.getInstance().getCurrencyManager().getBalance(p, "money");
        if (playerBalance < amount) {
            ChatUtil.sendError(p, "You don't have enough money!");
            return;
        }

        MythicPrison.getInstance().getCurrencyManager().removeBalance(p, "money", amount);
        // Note: This assumes the Gang class has bank functionality - you may need to add this

        String formattedAmount = ChatUtil.formatMoney(amount);
        ChatUtil.sendSuccess(p, "Deposited " + formattedAmount + " to the gang bank!");
    }

    private static void executeWithdraw(Object player, double amount) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        Gang gang = gangManager.getPlayerGang(p);

        if (gang == null) {
            ChatUtil.sendError(p, "You are not in a gang!");
            return;
        }

        if (amount <= 0) {
            ChatUtil.sendError(p, "Amount must be positive!");
            return;
        }

        // Note: This assumes the Gang class has bank functionality - you may need to add this
        MythicPrison.getInstance().getCurrencyManager().addBalance(p, "money", amount);

        String formattedAmount = ChatUtil.formatMoney(amount);
        ChatUtil.sendSuccess(p, "Withdrew " + formattedAmount + " from the gang bank!");
    }

    private static void executeList(Object player) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        var gangs = gangManager.getAllGangs();

        if (gangs.isEmpty()) {
            ChatUtil.sendMessage(p, "§cNo gangs exist yet!");
            return;
        }

        ChatUtil.sendMessage(p, "§d§l§m            §r §d§lGANG LIST §r§d§l§m            ");
        ChatUtil.sendMessage(p, "");

        int count = 0;
        for (Gang gang : gangs) {
            if (count >= 10) break;
            ChatUtil.sendMessage(p, "§f" + (count + 1) + ". §d" + gang.getName() + " §f(" + gang.getMembers().size() + "/" + gang.getMaxMembers() + ")");
            count++;
        }

        ChatUtil.sendMessage(p, "");
        ChatUtil.sendMessage(p, "§d§l§m                                                    ");
    }

    private static void executeTop(Object player) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        var topGangs = gangManager.getTopGangs(10);

        if (topGangs.isEmpty()) {
            ChatUtil.sendMessage(p, "§cNo gangs exist yet!");
            return;
        }

        ChatUtil.sendMessage(p, "§d§l§m            §r §d§lTOP GANGS §r§d§l§m            ");
        ChatUtil.sendMessage(p, "");

        for (int i = 0; i < topGangs.size(); i++) {
            Gang gang = topGangs.get(i);
            String positionColor = getPositionColor(i + 1);
            ChatUtil.sendMessage(p, positionColor + (i + 1) + ". §d" + gang.getName() + " §fLevel " + gang.getLevel() + " §7(" + gang.getMembers().size() + "/" + gang.getMaxMembers() + ")");
        }

        ChatUtil.sendMessage(p, "");
        ChatUtil.sendMessage(p, "§d§l§m                                                    ");
    }

    private static void executeChat(Object player, String[] messageArray) {
        Player p = (Player) player;
        GangManager gangManager = MythicPrison.getInstance().getGangManager();
        String message = String.join(" ", messageArray);

        if (message.trim().isEmpty()) {
            ChatUtil.sendError(p, "Message cannot be empty!");
            return;
        }

        gangManager.sendGangChat(p, message);
    }

    private static String getPositionColor(int position) {
        return switch (position) {
            case 1 -> "§6"; // Gold
            case 2 -> "§7"; // Silver
            case 3 -> "§c"; // Bronze
            default -> "§f"; // White
        };
    }

    private static Object findPlayerByName(String playerName) {
        try {
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            return connectionManager.getOnlinePlayers().stream()
                    .filter(p -> p.getUsername().equalsIgnoreCase(playerName))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding player: " + e.getMessage());
            return null;
        }
    }

    private static String getPlayerUsername(Object player) {
        if (player instanceof Player p) {
            return p.getUsername();
        }
        return "Unknown";
    }

    private static UUID getPlayerUUID(Object player) {
        if (player instanceof Player p) {
            return p.getUuid();
        }
        return null;
    }

    private static boolean isPlayerOnline(UUID uuid) {
        try {
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            return connectionManager.getOnlinePlayers().stream()
                    .anyMatch(p -> p.getUuid().equals(uuid));
        } catch (Exception e) {
            return false;
        }
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new GangCommand());
    }
}