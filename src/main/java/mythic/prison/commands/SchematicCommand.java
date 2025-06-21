package mythic.prison.commands;

import mythic.prison.MythicPrison;
import mythic.prison.utils.ChatUtil;
import mythic.prison.player.Profile;
import mythic.prison.managers.SchematicWorldManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.coordinate.Pos;

public class SchematicCommand extends Command {

    public SchematicCommand() {
        super("schematic", "schem");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;

            // Check admin permission
            if (!hasAdminPermission(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            showHelp(player);
        });

        // Action argument (load, reload, list, tp)
        ArgumentString actionArg = ArgumentType.String("action");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return;
            }

            Player player = (Player) sender;

            if (!hasAdminPermission(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            String action = context.get(actionArg);
            executeAction(player, action, null);
        }, actionArg);

        // Action + world name arguments
        ArgumentString worldArg = ArgumentType.String("world");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) {
                // Console execution
                String action = context.get(actionArg);
                String worldName = context.get(worldArg);
                executeActionConsole(action, worldName);
                return;
            }

            Player player = (Player) sender;

            if (!hasAdminPermission(player)) {
                ChatUtil.sendError(player, "You don't have permission to use this command!");
                return;
            }

            String action = context.get(actionArg);
            String worldName = context.get(worldArg);
            executeAction(player, action, worldName);
        }, actionArg, worldArg);
    }

    private void showHelp(Player player) {
        ChatUtil.sendMessage(player, "§6§l=== Schematic Commands ===");
        ChatUtil.sendMessage(player, "§e/schematic load <world> §7- Load a schematic world");
        ChatUtil.sendMessage(player, "§e/schematic reload <world> §7- Reload a schematic world");
        ChatUtil.sendMessage(player, "§e/schematic list §7- List all loaded schematic worlds");
        ChatUtil.sendMessage(player, "§e/schematic tp <world> §7- Teleport to a schematic world");
        ChatUtil.sendMessage(player, "§e/schematic info <world> §7- Get info about a world");
        ChatUtil.sendMessage(player, "§7Available worlds: §aspawn§7, §amine");
    }

    private void executeAction(Player player, String action, String worldName) {
        SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();

        if (schematicManager == null) {
            ChatUtil.sendError(player, "SchematicWorldManager is not available!");
            return;
        }

        switch (action.toLowerCase()) {
            case "load" -> {
                if (worldName == null) {
                    ChatUtil.sendError(player, "Usage: /schematic load <world>");
                    return;
                }
                loadWorld(player, worldName, schematicManager);
            }
            case "reload" -> {
                if (worldName == null) {
                    ChatUtil.sendError(player, "Usage: /schematic reload <world>");
                    return;
                }
                reloadWorld(player, worldName, schematicManager);
            }
            case "list" -> listWorlds(player, schematicManager);
            case "tp", "teleport" -> {
                if (worldName == null) {
                    ChatUtil.sendError(player, "Usage: /schematic tp <world>");
                    return;
                }
                teleportToWorld(player, worldName, schematicManager);
            }
            case "info" -> {
                if (worldName == null) {
                    ChatUtil.sendError(player, "Usage: /schematic info <world>");
                    return;
                }
                showWorldInfo(player, worldName, schematicManager);
            }
            default -> {
                ChatUtil.sendError(player, "Unknown action: " + action);
                showHelp(player);
            }
        }
    }

    private void executeActionConsole(String action, String worldName) {
        SchematicWorldManager schematicManager = MythicPrison.getInstance().getSchematicWorldManager();

        if (schematicManager == null) {
            System.err.println("[Schematic] SchematicWorldManager is not available!");
            return;
        }

        switch (action.toLowerCase()) {
            case "load" -> loadWorldConsole(worldName, schematicManager);
            case "reload" -> reloadWorldConsole(worldName, schematicManager);
            case "list" -> listWorldsConsole(schematicManager);
            default -> System.err.println("[Schematic] Unknown action: " + action);
        }
    }

    private void loadWorld(Player player, String worldName, SchematicWorldManager schematicManager) {
        ChatUtil.sendMessage(player, "§7Loading schematic world: §e" + worldName + "§7...");

        schematicManager.createSchematicWorld(worldName, worldName)
                .thenAccept(instance -> {
                    if (instance != null) {
                        ChatUtil.sendSuccess(player, "Successfully loaded schematic world: §e" + worldName);

                        // If it's the spawn world, update the main instance
                        if ("spawn".equals(worldName)) {
                            // Note: You might want to update the main instance reference in MythicPrison
                            ChatUtil.sendMessage(player, "§7Spawn world has been loaded. Players will spawn here on next join.");
                        }
                    } else {
                        ChatUtil.sendError(player, "Failed to load schematic world: " + worldName);
                        ChatUtil.sendMessage(player, "§7Make sure the schematic file exists in the schematics folder.");
                    }
                })
                .exceptionally(throwable -> {
                    ChatUtil.sendError(player, "Error loading world: " + throwable.getMessage());
                    return null;
                });
    }

    private void reloadWorld(Player player, String worldName, SchematicWorldManager schematicManager) {
        ChatUtil.sendMessage(player, "§7Reloading schematic world: §e" + worldName + "§7...");

        // First check if world exists
        var existingWorld = schematicManager.getWorld(worldName);
        if (existingWorld == null) {
            ChatUtil.sendError(player, "World " + worldName + " is not currently loaded!");
            ChatUtil.sendMessage(player, "§7Use §e/schematic load " + worldName + " §7to load it first.");
            return;
        }

        // Get players in the world before reloading
        Instance worldInstance = existingWorld.getInstance();
        var playersInWorld = worldInstance.getPlayers();

        // Remove the world and recreate it
        schematicManager.removeWorld(worldName);

        schematicManager.createSchematicWorld(worldName, worldName)
                .thenAccept(instance -> {
                    if (instance != null) {
                        ChatUtil.sendSuccess(player, "Successfully reloaded schematic world: §e" + worldName);

                        // Move players back to the reloaded world
                        if (!playersInWorld.isEmpty()) {
                            var newWorld = schematicManager.getWorld(worldName);
                            Pos spawnPoint = newWorld != null ? newWorld.getSpawnPoint() : new Pos(0, 5, 0);

                            for (Player worldPlayer : playersInWorld) {
                                worldPlayer.setInstance(instance, spawnPoint);
                                ChatUtil.sendMessage(worldPlayer, "§7The world has been reloaded!");
                            }

                            ChatUtil.sendMessage(player, "§7Moved §e" + playersInWorld.size() + " §7players to the reloaded world.");
                        }
                    } else {
                        ChatUtil.sendError(player, "Failed to reload schematic world: " + worldName);
                    }
                })
                .exceptionally(throwable -> {
                    ChatUtil.sendError(player, "Error reloading world: " + throwable.getMessage());
                    return null;
                });
    }

    private void listWorlds(Player player, SchematicWorldManager schematicManager) {
        var worlds = schematicManager.getAllWorlds();

        if (worlds.isEmpty()) {
            ChatUtil.sendMessage(player, "§7No schematic worlds are currently loaded.");
            return;
        }

        ChatUtil.sendMessage(player, "§6§lLoaded Schematic Worlds:");
        for (var entry : worlds.entrySet()) {
            String worldName = entry.getKey();
            var world = entry.getValue();
            int playerCount = world.getInstance().getPlayers().size();

            ChatUtil.sendMessage(player, "§e" + worldName + " §7- §a" + playerCount + " §7players");
        }
    }

    private void teleportToWorld(Player player, String worldName, SchematicWorldManager schematicManager) {
        var world = schematicManager.getWorld(worldName);

        if (world == null) {
            ChatUtil.sendError(player, "World " + worldName + " is not loaded!");
            ChatUtil.sendMessage(player, "§7Use §e/schematic load " + worldName + " §7to load it first.");
            return;
        }

        Instance instance = world.getInstance();
        Pos spawnPoint = world.getSpawnPoint();

        player.setInstance(instance, spawnPoint);
        ChatUtil.sendSuccess(player, "Teleported to world: §e" + worldName);

        // Track player in the world
        schematicManager.trackPlayerInWorld(player, worldName);
    }

    private void showWorldInfo(Player player, String worldName, SchematicWorldManager schematicManager) {
        var world = schematicManager.getWorld(worldName);

        if (world == null) {
            ChatUtil.sendError(player, "World " + worldName + " is not loaded!");
            return;
        }

        Instance instance = world.getInstance();
        Pos spawnPoint = world.getSpawnPoint();
        int playerCount = instance.getPlayers().size();

        ChatUtil.sendMessage(player, "§6§lWorld Info: §e" + worldName);
        ChatUtil.sendMessage(player, "§7Players: §a" + playerCount);
        ChatUtil.sendMessage(player, "§7Spawn Point: §e" + spawnPoint.x() + ", " + spawnPoint.y() + ", " + spawnPoint.z());
        ChatUtil.sendMessage(player, "§7World Bounds: §e" + world.getMinX() + "," + world.getMinY() + "," + world.getMinZ() + 
                        " §7to §e" + world.getMaxX() + "," + world.getMaxY() + "," + world.getMaxZ());
    }

    // Console versions
    private void loadWorldConsole(String worldName, SchematicWorldManager schematicManager) {
        System.out.println("[Schematic] Loading world: " + worldName);

        schematicManager.createSchematicWorld(worldName, worldName)
                .thenAccept(instance -> {
                    if (instance != null) {
                        System.out.println("[Schematic] Successfully loaded world: " + worldName);
                    } else {
                        System.err.println("[Schematic] Failed to load world: " + worldName);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("[Schematic] Error loading world: " + throwable.getMessage());
                    return null;
                });
    }

    private void reloadWorldConsole(String worldName, SchematicWorldManager schematicManager) {
        System.out.println("[Schematic] Reloading world: " + worldName);

        schematicManager.removeWorld(worldName);

        schematicManager.createSchematicWorld(worldName, worldName)
                .thenAccept(instance -> {
                    if (instance != null) {
                        System.out.println("[Schematic] Successfully reloaded world: " + worldName);
                    } else {
                        System.err.println("[Schematic] Failed to reload world: " + worldName);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("[Schematic] Error reloading world: " + throwable.getMessage());
                    return null;
                });
    }

    private void listWorldsConsole(SchematicWorldManager schematicManager) {
        var worlds = schematicManager.getAllWorlds();

        if (worlds.isEmpty()) {
            System.out.println("[Schematic] No worlds currently loaded.");
            return;
        }

        System.out.println("[Schematic] Loaded worlds:");
        for (var entry : worlds.entrySet()) {
            String worldName = entry.getKey();
            var world = entry.getValue();
            int playerCount = world.getInstance().getPlayers().size();

            System.out.println("[Schematic] - " + worldName + " (" + playerCount + " players)");
        }
    }

    private boolean hasAdminPermission(Player player) {
        try {
            // Load the permission profile from database using the Profile class
            Profile profile = Profile.loadAsync(player.getUuid().toString()).join();

            if (profile != null) {
                return profile.hasPermission("mythicprison.admin") ||
                        profile.hasPermission("mythicprison.schematic");
            }

            // Default: deny access for security
            return false;
        } catch (Exception e) {
            System.err.println("Error checking permission for " + player.getUsername() + ": " + e.getMessage());
            return false;
        }
    }

    public static void register() {
        MinecraftServer.getCommandManager().register(new SchematicCommand());
    }
}