package mythic.prison.commands;

import mythic.prison.MythicPrison;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

public class AscensionCommand extends Command {

    public AscensionCommand() {
        super("ascension", "ascend");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof net.minestom.server.entity.Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            net.minestom.server.entity.Player player = (net.minestom.server.entity.Player) sender;
            MythicPrison.getInstance().getRankingManager().showAscensionInfo(player);
        });

        // Add "confirm" argument
        ArgumentWord confirmArg = ArgumentType.Word("confirm").from("confirm");

        addSyntax((sender, context) -> {
            if (!(sender instanceof net.minestom.server.entity.Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            net.minestom.server.entity.Player player = (net.minestom.server.entity.Player) sender;
            String confirm = context.get(confirmArg);

            if (confirm != null && confirm.equalsIgnoreCase("confirm")) {
                MythicPrison.getInstance().getRankingManager().ascend(player);
            } else {
                MythicPrison.getInstance().getRankingManager().showAscensionInfo(player);
            }
        }, confirmArg);
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new AscensionCommand());
    }
}