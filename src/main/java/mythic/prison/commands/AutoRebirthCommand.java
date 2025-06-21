package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class AutoRebirthCommand extends Command {

    public AutoRebirthCommand() {
        super("autorebirth", "ar");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            toggleAutoRebirth(player);
        });
    }

    private void toggleAutoRebirth(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) {
            ChatUtil.sendError(player, "Could not load your profile!");
            return;
        }

        boolean currentState = profile.isAutoRebirthEnabled();
        profile.setAutoRebirthEnabled(!currentState);

        if (!currentState) {
            ChatUtil.sendSuccess(player, "§5Auto-Rebirth §aENABLED§7!");
            ChatUtil.sendMessage(player, "§7You will automatically rebirth when you reach prestige §610§7.");
            ChatUtil.sendMessage(player, "§7Use §5/autorebirth §7again to disable.");
        } else {
            ChatUtil.sendSuccess(player, "§5Auto-Rebirth §cDISABLED§7!");
            ChatUtil.sendMessage(player, "§7You will no longer automatically rebirth.");
            ChatUtil.sendMessage(player, "§7Use §5/rebirth §7or §5/rebirthmax §7to rebirth manually.");
        }

        // Save the profile
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    }

    public static void register() {
        MinecraftServer.getCommandManager().register(new AutoRebirthCommand());
    }
}