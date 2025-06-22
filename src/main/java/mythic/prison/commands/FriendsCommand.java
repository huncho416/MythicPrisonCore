package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.managers.FriendsManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class FriendsCommand extends Command {
    private final FriendsManager friendsManager;

    public FriendsCommand() {
        super("friend", "friends", "f");
        this.friendsManager = MythicPrison.getInstance().getFriendsManager();

        ArgumentWord actionArg = ArgumentType.Word("action")
                .from("add", "remove", "accept", "deny", "list", "requests", "help");
        ArgumentWord playerArg = ArgumentType.Word("player");

        // /friend (shows help)
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player) {
                showHelp((Player) sender);
            }
        });

        // /friend <action>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) return;
            Player player = (Player) sender;
            String action = context.get(actionArg);

            switch (action.toLowerCase()) {
                case "list":
                    friendsManager.showFriendsList(player);
                    break;
                case "requests":
                    friendsManager.showPendingRequests(player);
                    break;
                case "help":
                    showHelp(player);
                    break;
                default:
                    player.sendMessage(Component.text("§cUsage: /friend " + action + " <player>"));
            }
        }, actionArg);

        // /friend <action> <player>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) return;
            Player player = (Player) sender;
            String action = context.get(actionArg);
            String targetPlayer = context.get(playerArg);

            switch (action.toLowerCase()) {
                case "add":
                    friendsManager.sendFriendRequest(player, targetPlayer);
                    break;
                case "remove":
                    friendsManager.removeFriend(player, targetPlayer);
                    break;
                case "accept":
                    friendsManager.acceptFriendRequest(player, targetPlayer);
                    break;
                case "deny":
                    friendsManager.denyFriendRequest(player, targetPlayer);
                    break;
                default:
                    showHelp(player);
            }
        }, actionArg, playerArg);
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
        player.sendMessage(Component.text("§e§lFRIENDS SYSTEM HELP"));
        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
        player.sendMessage(Component.text("§e/friend list §7- View your friends list"));
        player.sendMessage(Component.text("§e/friend add <player> §7- Send a friend request"));
        player.sendMessage(Component.text("§e/friend remove <player> §7- Remove a friend"));
        player.sendMessage(Component.text("§e/friend accept <player> §7- Accept a friend request"));
        player.sendMessage(Component.text("§e/friend deny <player> §7- Deny a friend request"));
        player.sendMessage(Component.text("§e/friend requests §7- View pending requests"));
        player.sendMessage(Component.text("§6§l═══════════════════════════════════════"));
    }
}