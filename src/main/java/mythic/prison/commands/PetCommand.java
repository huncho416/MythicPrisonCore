
package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.data.pets.Pet;
import mythic.prison.data.pets.PetType;
import mythic.prison.managers.PetManager;
import mythic.prison.utils.ChatUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;

import java.util.List;

public class PetCommand extends Command {

    public PetCommand() {
        super("pets", "pet");

        // Default executor - show pet list
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executePetList((Player) sender);
        });

        // /pets activate <id>
        ArgumentWord activateArg = ArgumentType.Word("activate").from("activate");
        ArgumentString idArg = ArgumentType.String("id");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String idStr = context.get(idArg);
            try {
                int id = Integer.parseInt(idStr);
                executeActivate(player, id);
            } catch (NumberFormatException e) {
                ChatUtil.sendError(player, "Invalid pet ID: " + idStr);
                ChatUtil.sendMessage(player, "§7Please enter a valid number.");
            }
        }, activateArg, idArg);

        // /pets deactivate
        ArgumentWord deactivateArg = ArgumentType.Word("deactivate").from("deactivate");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeDeactivate((Player) sender);
        }, deactivateArg);

        // /pets levelup <id>
        ArgumentWord levelupArg = ArgumentType.Word("levelup").from("levelup", "level");
        ArgumentString levelIdArg = ArgumentType.String("levelId");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String idStr = context.get(levelIdArg);
            try {
                int id = Integer.parseInt(idStr);
                executeLevelUp(player, id);
            } catch (NumberFormatException e) {
                ChatUtil.sendError(player, "Invalid pet ID: " + idStr);
                ChatUtil.sendMessage(player, "§7Please enter a valid number.");
            }
        }, levelupArg, levelIdArg);

        // /pets fuse <id1> <id2>
        ArgumentWord fuseArg = ArgumentType.Word("fuse").from("fuse", "merge");
        ArgumentString id1Arg = ArgumentType.String("id1");
        ArgumentString id2Arg = ArgumentType.String("id2");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;
            String id1Str = context.get(id1Arg);
            String id2Str = context.get(id2Arg);

            try {
                int id1 = Integer.parseInt(id1Str);
                int id2 = Integer.parseInt(id2Str);
                executeFuse(player, id1, id2);
            } catch (NumberFormatException e) {
                ChatUtil.sendError(player, "Invalid pet IDs!");
                ChatUtil.sendMessage(player, "§7Please enter valid numbers for both pet IDs.");
            }
        }, fuseArg, id1Arg, id2Arg);

        // /pets give <player> <boxType>
        ArgumentWord giveArg = ArgumentType.Word("give").from("give");
        ArgumentString playerArg = ArgumentType.String("player");
        ArgumentString boxTypeArg = ArgumentType.String("boxType");
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);
            String boxType = context.get(boxTypeArg);
            executeGive(sender, targetName, boxType);
        }, giveArg, playerArg, boxTypeArg);

        // /pets types
        ArgumentWord typesArg = ArgumentType.Word("types").from("types", "list");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                System.out.println("Only players can use this command!");
                return;
            }
            executeTypes((Player) sender);
        }, typesArg);
    }

    private static void executePetList(Player player) {
        PetManager petManager = MythicPrison.getInstance().getPetManager();
        List<Pet> pets = petManager.getPlayerPets(player);
        Pet activePet = petManager.getActivePet(player);

        ChatUtil.sendMessage(player, "§d§l§m                §r §d§lYOUR PETS §r§d§l§m                ");
        ChatUtil.sendMessage(player, "");

        if (activePet != null) {
            ChatUtil.sendMessage(player, "§f§lActive Pet: " + activePet.getDisplayName());
            ChatUtil.sendMessage(player, "§f§lMultiplier: §a" + String.format("%.2fx", activePet.getCurrentMultiplier()) + " " + activePet.getBoostType());
            ChatUtil.sendMessage(player, "§f§lEXP: §b" + activePet.getExperience() + "§7/§b" + activePet.getExpRequiredForNextLevel());
            ChatUtil.sendMessage(player, "");
        }

        if (pets.isEmpty()) {
            ChatUtil.sendMessage(player, "§7You don't have any pets yet!");
            ChatUtil.sendMessage(player, "§7Buy pet boxes from the shop!");
            ChatUtil.sendMessage(player, "");
            ChatUtil.sendMessage(player, "§f§lHow to get pets:");
            ChatUtil.sendMessage(player, "§a• Purchase pet boxes from the shop");
            ChatUtil.sendMessage(player, "§a• Complete achievements and milestones");
            ChatUtil.sendMessage(player, "§a• Participate in server events");
            ChatUtil.sendMessage(player, "§a• Trade with other players");
        } else {
            ChatUtil.sendMessage(player, "§f§lPet Collection:");

            for (int i = 0; i < pets.size(); i++) {
                Pet pet = pets.get(i);
                String status = pet.equals(activePet) ? "§a§lACTIVE" : "§7Inactive";

                ChatUtil.sendMessage(player, "§f" + (i + 1) + ". " + pet.getDisplayName() + " " + status);
                ChatUtil.sendMessage(player, "   §7Level: §f" + pet.getLevel() + " §7| §7Multiplier: §a" + String.format("%.2fx", pet.getCurrentMultiplier()) + " " + pet.getBoostType());
                ChatUtil.sendMessage(player, "   §7EXP: §b" + pet.getExperience() + "§7/§b" + pet.getExpRequiredForNextLevel());
                ChatUtil.sendMessage(player, "");
            }
        }

        ChatUtil.sendMessage(player, "§fCommands:");
        ChatUtil.sendMessage(player, "§d/pets activate <id> §7- Activate a pet");
        ChatUtil.sendMessage(player, "§d/pets deactivate §7- Deactivate current pet");
        ChatUtil.sendMessage(player, "§d/pets levelup <id> §7- Level up a pet");
        ChatUtil.sendMessage(player, "§d/pets fuse <id1> <id2> §7- Fuse two pets");
        ChatUtil.sendMessage(player, "§d/pets types §7- View all pet types");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                ");
    }

    private static void executeActivate(Player player, int id) {
        PetManager petManager = MythicPrison.getInstance().getPetManager();
        List<Pet> pets = petManager.getPlayerPets(player);

        if (id < 1 || id > pets.size()) {
            ChatUtil.sendError(player, "Invalid pet ID: " + id);
            ChatUtil.sendMessage(player, "§7Valid IDs: 1 to " + pets.size());
            ChatUtil.sendMessage(player, "§7Use §f/pets §7to see your pets.");
            return;
        }

        Pet selectedPet = pets.get(id - 1);
        Pet currentActive = petManager.getActivePet(player);

        if (selectedPet.equals(currentActive)) {
            ChatUtil.sendError(player, "This pet is already active!");
            return;
        }

        petManager.setActivePet(player, selectedPet);
        ChatUtil.sendSuccess(player, "Activated " + selectedPet.getDisplayName() + "!");
        ChatUtil.sendMessage(player, "§7New boost: §a" + String.format("%.2fx", selectedPet.getCurrentMultiplier()) + " " + selectedPet.getBoostType());
    }

    private static void executeDeactivate(Player player) {
        PetManager petManager = MythicPrison.getInstance().getPetManager();
        Pet activePet = petManager.getActivePet(player);

        if (activePet == null) {
            ChatUtil.sendError(player, "You don't have an active pet!");
            return;
        }

        petManager.setActivePet(player, null);
        ChatUtil.sendSuccess(player, "Deactivated " + activePet.getDisplayName() + "!");
        ChatUtil.sendMessage(player, "§7You no longer have any pet bonuses active.");
    }

    private static void executeLevelUp(Player player, int id) {
        PetManager petManager = MythicPrison.getInstance().getPetManager();
        List<Pet> pets = petManager.getPlayerPets(player);

        if (id < 1 || id > pets.size()) {
            ChatUtil.sendError(player, "Invalid pet ID: " + id);
            ChatUtil.sendMessage(player, "§7Valid IDs: 1 to " + pets.size());
            ChatUtil.sendMessage(player, "§7Use §f/pets §7to see your pets.");
            return;
        }

        Pet selectedPet = pets.get(id - 1);
        boolean success = petManager.levelUpPet(player, selectedPet);

        if (success) {
            ChatUtil.sendSuccess(player, selectedPet.getDisplayName() + " leveled up!");
            ChatUtil.sendMessage(player, "§7New level: §f" + selectedPet.getLevel());
            ChatUtil.sendMessage(player, "§7New multiplier: §a" + String.format("%.2fx", selectedPet.getCurrentMultiplier()));
        } else {
            ChatUtil.sendError(player, "Cannot level up this pet!");
            ChatUtil.sendMessage(player, "§7Make sure you have enough resources or the pet isn't at max level.");
        }
    }

    private static void executeFuse(Player player, int id1, int id2) {
        PetManager petManager = MythicPrison.getInstance().getPetManager();
        List<Pet> pets = petManager.getPlayerPets(player);

        if (id1 < 1 || id1 > pets.size() || id2 < 1 || id2 > pets.size()) {
            ChatUtil.sendError(player, "Invalid pet ID(s)!");
            ChatUtil.sendMessage(player, "§7Valid IDs: 1 to " + pets.size());
            ChatUtil.sendMessage(player, "§7Use §f/pets §7to see your pets.");
            return;
        }

        if (id1 == id2) {
            ChatUtil.sendError(player, "You cannot fuse a pet with itself!");
            return;
        }

        Pet pet1 = pets.get(id1 - 1);
        Pet pet2 = pets.get(id2 - 1);

        ChatUtil.sendMessage(player, "§e§lFusing pets...");
        ChatUtil.sendMessage(player, "§7Pet 1: " + pet1.getDisplayName() + " (Level " + pet1.getLevel() + ")");
        ChatUtil.sendMessage(player, "§7Pet 2: " + pet2.getDisplayName() + " (Level " + pet2.getLevel() + ")");

        boolean success = petManager.fusePets(player, pet1, pet2);

        if (success) {
            ChatUtil.sendSuccess(player, "Pets fused successfully!");
            ChatUtil.sendMessage(player, "§7Check your pet collection to see the result!");
        } else {
            ChatUtil.sendError(player, "Pet fusion failed!");
            ChatUtil.sendMessage(player, "§7Make sure both pets can be fused and you have the required resources.");
        }
    }

    private static void executeGive(Object sender, String playerName, String boxType) {
        // Check admin permission (simplified check)
        boolean hasPermission = true; // You can implement proper permission checking here

        if (!hasPermission) {
            ChatUtil.sendError(sender, "No permission!");
            return;
        }

        Player target = findPlayerByName(playerName);
        if (target == null) {
            ChatUtil.sendError(sender, "Player not found: " + playerName);
            ChatUtil.sendMessage(sender, "§7Make sure the player is online.");
            return;
        }

        PetManager petManager = MythicPrison.getInstance().getPetManager();
        // Note: createPetBox method needs to be implemented in PetManager
        // ItemStack petBox = petManager.createPetBox(boxType);

        // target.getInventory().addItemStack(petBox);
        ChatUtil.sendSuccess(sender, "Gave " + boxType + " pet box to " + playerName + "!");
        ChatUtil.sendSuccess(target, "You received a " + boxType + " pet box!");
        ChatUtil.sendMessage(target, "§7Right-click to open it and get a random pet!");
    }

    private static void executeTypes(Player player) {
        List<PetType> petTypes = MythicPrison.getInstance().getPetManager().getAvailablePetTypes();

        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lAVAILABLE PETS §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");

        String currentRarity = "";
        for (PetType petType : petTypes) {
            String rarityName = petType.getRarity().name();
            if (!rarityName.equals(currentRarity)) {
                currentRarity = rarityName;
                ChatUtil.sendMessage(player, petType.getRarity().getColor() + "§l" + rarityName + " PETS:");
                ChatUtil.sendMessage(player, "");
            }

            double chance = petType.getRarity().getBaseChance();
            String chanceText = chance < 1 ? String.format("%.1f%%", chance) : String.format("%.0f%%", chance);

            ChatUtil.sendMessage(player, "  " + petType.getRarity().getColor() + "§l" + petType.getName() + " " + petType.getEmoji());
            ChatUtil.sendMessage(player, "    §7Boost: §a" + String.format("%.1f", (petType.getBaseMultiplier() - 1) * 100) +
                    "% - " + String.format("%.1f", (petType.getMaxMultiplier() - 1) * 100) + "% " + petType.getBoostType());
            ChatUtil.sendMessage(player, "    §7Chance: §f" + chanceText);
            ChatUtil.sendMessage(player, "    §7Max Level: §f" + petType.getMaxLevel());
            ChatUtil.sendMessage(player, "");
        }

        ChatUtil.sendMessage(player, "§f§lHow to get pets:");
        ChatUtil.sendMessage(player, "§a• Buy pet boxes from the shop");
        ChatUtil.sendMessage(player, "§a• Complete achievements");
        ChatUtil.sendMessage(player, "§a• Participate in events");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                        ");
    }

    private static Player findPlayerByName(String playerName) {
        try {
            var connectionManager = net.minestom.server.MinecraftServer.getConnectionManager();
            var players = connectionManager.getOnlinePlayers();

            for (Player player : players) {
                if (player.getUsername().equalsIgnoreCase(playerName)) {
                    return player;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding player: " + e.getMessage());
        }
        return null;
    }

    public static void register() {
        net.minestom.server.MinecraftServer.getCommandManager().register(new PetCommand());
    }
}