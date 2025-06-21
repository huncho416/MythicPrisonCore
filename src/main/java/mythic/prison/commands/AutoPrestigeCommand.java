package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.player.PlayerProfile;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class AutoPrestigeCommand extends Command {

    public AutoPrestigeCommand() {
        super("autoprestige", "ap");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            toggleAutoPrestige(player);
        });
    }

    private void toggleAutoPrestige(Player player) {
        PlayerProfile profile = MythicPrison.getInstance().getProfileManager().getProfile(player);
        if (profile == null) {
            ChatUtil.sendError(player, "Could not load your profile!");
            return;
        }

        boolean currentState = profile.isAutoPrestigeEnabled();
        profile.setAutoPrestigeEnabled(!currentState);

        if (!currentState) {
            ChatUtil.sendSuccess(player, "§6Auto-Prestige §aENABLED§7!");
            ChatUtil.sendMessage(player, "§7You will automatically prestige when you reach rank §eZZ§7.");
            ChatUtil.sendMessage(player, "§7Use §6/autoprestige §7again to disable.");
        } else {
            ChatUtil.sendSuccess(player, "§6Auto-Prestige §cDISABLED§7!");
            ChatUtil.sendMessage(player, "§7You will no longer automatically prestige.");
            ChatUtil.sendMessage(player, "§7Use §6/prestige §7or §6/prestigemax §7to prestige manually.");
        }

        // Save the profile
        MythicPrison.getInstance().getProfileManager().saveProfile(profile);
    }

    public static void register() {
        MinecraftServer.getCommandManager().register(new AutoPrestigeCommand());
    }
}