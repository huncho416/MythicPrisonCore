package mythic.prison;

import mythic.prison.commands.*;
import mythic.prison.listeners.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import mythic.prison.managers.*;
import mythic.prison.database.MongoManager;
import mythic.prison.database.RedisManager;
import mythic.prison.data.backpack.Backpack;
import mythic.prison.managers.SchematicWorldManager.SchematicWorld;
import mythic.prison.data.mine.PrivateMine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.ItemStack;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MythicPrison {
    private static MythicPrison instance;

    // Database managers
    private MongoManager mongoManager;
    private RedisManager redisManager;

    // Core managers
    private ProfileManager profileManager;
    private CurrencyManager currencyManager;
    private RankingManager rankingManager;
    private MultiplierManager multiplierManager;
    private BackpackManager backpackManager;
    private PickaxeManager pickaxeManager;
    private PetManager petManager;
    private GangManager gangManager;
    private StatsManager statsManager;
    private MilestoneManager milestoneManager;
    private ScoreboardManager scoreboardManager;
    private MineManager mineManager;
    private SchematicWorldManager schematicWorldManager;
    // private WorldManager worldManager;

    // System components
    private Instance mainInstance;
    private ScheduledExecutorService scheduler;
    private File dataFolder;

    // Add this field to your MythicPrison class
    private FriendsManager friendsManager;

    public static void main(String[] args) {
        new MythicPrison().start();
    }

    public void start() {
        try {
            instance = this;

            System.out.println("[MythicPrison] Starting server initialization...");

            // Initialize data folder
            initializeDataFolder();

            // Initialize MinecraftServer
            MinecraftServer minecraftServer = MinecraftServer.init();

            // Setup authentication BEFORE instance setup for proper skin loading
            MojangAuth.init();
            System.out.println("[MythicPrison] ✓ Authentication system initialized");

            // Connect to databases FIRST
            initializeDatabases();

            // Initialize managers AFTER databases
            initializeManagers();

            // Setup worlds using schematic system
            setupWorlds();

            // Register event listeners
            registerEventListeners();

            // Register commands
            registerCommands();

            // Start updaters
            startUpdaters();

            // Start the server
            minecraftServer.start("0.0.0.0", 25565);

            System.out.println("[MythicPrison] ✓ Server started successfully on port 25565!");

            // Shutdown hook for cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Critical error during startup: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeDataFolder() {
        try {
            // Create data folder in the current working directory
            dataFolder = new File(System.getProperty("user.dir"), "data");

            if (!dataFolder.exists()) {
                boolean created = dataFolder.mkdirs();
                if (created) {
                    System.out.println("[MythicPrison] ✓ Data folder created: " + dataFolder.getAbsolutePath());
                } else {
                    System.err.println("[MythicPrison] ✗ Failed to create data folder: " + dataFolder.getAbsolutePath());
                }
            } else {
                System.out.println("[MythicPrison] ✓ Data folder found: " + dataFolder.getAbsolutePath());
            }

            // Create subdirectories
            createSubDirectories();

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error initializing data folder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSubDirectories() {
        try {
            // Create necessary subdirectories
            String[] subDirs = {"schematics", "worlds", "configs", "logs"};

            for (String subDir : subDirs) {
                File dir = new File(dataFolder, subDir);
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (created) {
                        System.out.println("[MythicPrison] ✓ Created subdirectory: " + subDir);
                    } else {
                        System.err.println("[MythicPrison] ✗ Failed to create subdirectory: " + subDir);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error creating subdirectories: " + e.getMessage());
        }
    }

    private void initializeDatabases() {
        try {
            System.out.println("[MythicPrison] Connecting to databases...");

            // Initialize MongoDB
            mongoManager = new MongoManager();
            mongoManager.connect();
            System.out.println("[MythicPrison] ✓ MongoDB connection attempted");

            // Initialize Redis
            redisManager = new RedisManager();
            redisManager.connect();
            System.out.println("[MythicPrison] ✓ Redis connection attempted");

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeManagers() {
        try {
            System.out.println("[MythicPrison] Initializing managers...");

            // Initialize scheduler first
            scheduler = Executors.newScheduledThreadPool(4);
            System.out.println("[MythicPrison] ✓ Scheduler initialized");

            // Initialize world manager early (needed for other systems)
            // worldManager = new WorldManager();
            // worldManager.loadWorlds();
            // System.out.println("[MythicPrison] ✓ WorldManager initialized");

            // Initialize core managers in dependency order
            profileManager = new ProfileManager();
            System.out.println("[MythicPrison] ✓ ProfileManager initialized");

            currencyManager = new CurrencyManager();
            System.out.println("[MythicPrison] ✓ CurrencyManager initialized");

            rankingManager = new RankingManager();
            System.out.println("[MythicPrison] ✓ RankingManager initialized");

            multiplierManager = new MultiplierManager();
            System.out.println("[MythicPrison] ✓ MultiplierManager initialized");

            backpackManager = new BackpackManager();
            System.out.println("[MythicPrison] ✓ BackpackManager initialized");

            pickaxeManager = new PickaxeManager();
            System.out.println("[MythicPrison] ✓ PickaxeManager initialized");

            petManager = new PetManager();
            System.out.println("[MythicPrison] ✓ PetManager initialized");

            gangManager = new GangManager();
            System.out.println("[MythicPrison] ✓ GangManager initialized");

            statsManager = new StatsManager();
            System.out.println("[MythicPrison] ✓ StatsManager initialized");

            milestoneManager = new MilestoneManager();
            System.out.println("[MythicPrison] ✓ MilestoneManager initialized");

            scoreboardManager = new ScoreboardManager();
            System.out.println("[MythicPrison] ✓ ScoreboardManager initialized");

            mineManager = new MineManager();
            System.out.println("[MythicPrison] ✓ MineManager initialized");

            schematicWorldManager = new SchematicWorldManager();
            System.out.println("[MythicPrison] ✓ SchematicWorldManager initialized");

            // In your onEnable() method, add:
            this.friendsManager = new FriendsManager();

            System.out.println("[MythicPrison] ✓ All managers initialized successfully!");

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Failed to initialize managers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Manager initialization failed", e);
        }
    }

    private void setupWorlds() {
        try {
            System.out.println("[MythicPrison] Setting up worlds...");

            // Use WorldManager's spawn world as main instance if available
            // if (worldManager != null && worldManager.getSpawnWorld() != null) {
            //     this.mainInstance = worldManager.getSpawnWorld();
            //     System.out.println("[MythicPrison] ✓ Using WorldManager spawn world as main instance");
            // } else {
                // Fallback to schematic system
                System.out.println("[MythicPrison] WorldManager not available, using schematic system...");

                // Create spawn world and set as main instance
                schematicWorldManager.createSchematicWorld("spawn", "spawn")
                        .thenAccept(spawnInstance -> {
                            if (spawnInstance != null) {
                                this.mainInstance = spawnInstance;
                                System.out.println("[MythicPrison] ✓ Spawn world loaded from schematic");
                            } else {
                                System.err.println("[MythicPrison] ✗ Failed to load spawn world schematic");
                            }
                        })
                        .exceptionally(throwable -> {
                            System.err.println("[MythicPrison] ✗ Error loading spawn world: " + throwable.getMessage());
                            return null;
                        });

                // Create mine world
                schematicWorldManager.createSchematicWorld("mine", "mine")
                        .thenAccept(mineInstance -> {
                            if (mineInstance != null) {
                                System.out.println("[MythicPrison] ✓ Mine world loaded from schematic");
                            } else {
                                System.err.println("[MythicPrison] ✗ Failed to load mine world schematic");
                            }
                        })
                        .exceptionally(throwable -> {
                            System.err.println("[MythicPrison] ✗ Error loading mine world: " + throwable.getMessage());
                            return null;
                        });

                // Wait a bit for async loading to complete
                Thread.sleep(2000);
            // }

            // Create a fallback world if neither system worked
            if (mainInstance == null) {
                System.out.println("[MythicPrison] Creating emergency fallback spawn world...");
                mainInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
                mainInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK));
                System.out.println("[MythicPrison] ✓ Emergency fallback spawn world created");
            }

            System.out.println("[MythicPrison] ✓ World setup completed!");

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error setting up worlds: " + e.getMessage());
            e.printStackTrace();

            // Create emergency fallback world
            mainInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
            mainInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK));
            System.out.println("[MythicPrison] ✓ Emergency fallback world created");
        }
    }

private void registerEventListeners() {
    var globalEventHandler = MinecraftServer.getGlobalEventHandler();

    // Initialize the JoinQuitListener
    JoinQuitListener joinQuitListener = new JoinQuitListener();
    
    // Register player configuration event (REQUIRED for player spawning)
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, joinQuitListener::onPlayerConfiguration);
    
    // Register join/quit events
    globalEventHandler.addListener(PlayerSpawnEvent.class, joinQuitListener::onJoin);
    globalEventHandler.addListener(PlayerDisconnectEvent.class, joinQuitListener::onQuit);

    // Chat formatting
    globalEventHandler.addListener(PlayerChatEvent.class, event -> {
        Player player = event.getPlayer();
        String message = event.getRawMessage(); // Fix: Use getRawMessage() instead of getMessage()
        
        // Format the chat message using ChatListener
        String formattedMessage = mythic.prison.listeners.ChatListener.formatChatMessage(player, message);
        
        // Cancel the original event and send formatted message to all players
        event.setCancelled(true);
        
        // Send to all players
        Component chatComponent = Component.text(formattedMessage);
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(chatComponent);
        }
        
        // Log to console
        System.out.println("[CHAT] " + formattedMessage);
    });

    // Block break event for mining mechanics
    globalEventHandler.addListener(PlayerBlockBreakEvent.class, this::handleBlockBreak);

    // Right-click handler for pickaxe enchant menu
    globalEventHandler.addListener(PlayerUseItemEvent.class, event -> {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        
        if (pickaxeManager != null && pickaxeManager.isSoulboundPickaxe(item)) {
            // Open pickaxe enchant GUI
            pickaxeManager.handlePickaxeRightClick(player, item);
            event.setCancelled(true); // Prevent default use behavior
        }
    });

    // Item drop prevention for soulbound pickaxe
    globalEventHandler.addListener(ItemDropEvent.class, event -> {
        if (pickaxeManager != null && pickaxeManager.isSoulboundPickaxe(event.getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop your soulbound pickaxe!");
        }
    });

    // Inventory protection events - simplified approach
    globalEventHandler.addListener(InventoryPreClickEvent.class, event -> {
        Player player = (Player) event.getPlayer();
        int slot = event.getSlot();
        PickaxeManager pickaxeManager = getPickaxeManager();
        
        // Check for valid slot range to prevent ArrayIndexOutOfBoundsException
        if (slot < 0 || slot >= player.getInventory().getSize()) {
            return; // Skip processing for invalid slots
        }
        
        if (pickaxeManager != null) {
            try {
                // Get the item in the clicked slot
                ItemStack slotItem = player.getInventory().getItemStack(slot);
                
                // If clicking on slot 0 and it contains a soulbound pickaxe, prevent movement
                if (slot == 0 && pickaxeManager.isSoulboundPickaxe(slotItem)) {
                    event.setCancelled(true);
                    // Don't show message here since right-clicks will be handled by PlayerUseItemEvent
                }
                
                // Prevent placing non-pickaxe items in slot 0
                if (slot == 0 && !slotItem.isAir() && !pickaxeManager.isSoulboundPickaxe(slotItem)) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou can only have your pickaxe in this slot!");
                }
            } catch (Exception e) {
                System.err.println("[MythicPrison] Error in inventory click handler: " + e.getMessage());
                // Don't print full stack trace for common inventory errors
            }
        }
    });

    globalEventHandler.addListener(PlayerSwapItemEvent.class, event -> {
        PickaxeManager pickaxeManager = getPickaxeManager();
        if (pickaxeManager != null && pickaxeManager.preventItemSwap(event)) {
            event.setCancelled(true);
        }
    });

    // Remove the PlayerChangeHeldSlotEvent listener to allow hotbar scrolling
    /*
    globalEventHandler.addListener(PlayerChangeHeldSlotEvent.class, event -> {
        Player player = event.getPlayer();
        PickaxeManager pickaxeManager = getPickaxeManager();
        
        if (pickaxeManager != null) {
            if (event.getNewSlot() == 0) {
                ItemStack slotItem = player.getInventory().getItemStack(0);
                if (pickaxeManager.isSoulboundPickaxe(slotItem)) {
                    event.setCancelled(true);
                }
            }
        }
    });
    */
}

    private void handleBlockBreak(PlayerBlockBreakEvent event) {
        try {
            Player player = event.getPlayer();
            Block block = event.getBlock();

            // Get current world player is in
            String currentWorld = schematicWorldManager.getPlayerWorld(player);
            
            // Remove these debug lines:
            
            // Determine if player can break blocks in this location
            boolean canBreakBlocks = false;
            
            if (currentWorld != null) {
                // Check if player is in their own mine
                String expectedMineWorld = "mine_" + player.getUsername().toLowerCase();
                if (currentWorld.equals(expectedMineWorld)) {
                    canBreakBlocks = true;
                    // Remove this debug line:
                    
                }
                // Check if player is in someone else's mine and has permission
                else if (currentWorld.startsWith("mine_")) {
                    // Extract the mine owner's name from the world name
                    String mineOwnerName = currentWorld.substring(5); // Remove "mine_" prefix
                    Player mineOwner = findPlayerByName(mineOwnerName);
                    
                    if (mineOwner != null) {
                        PrivateMine mine = mineManager.getPlayerMine(mineOwner);
                        if (mine != null && mine.canPlayerAccess(player.getUuid().toString())) {
                            canBreakBlocks = true;
                            // Remove this debug line:
                            
                        }
                    }
                }
                // Deny breaking blocks in spawn or any other world
                else if (currentWorld.equals("spawn") || !currentWorld.startsWith("mine_")) {
                    canBreakBlocks = false;
                    // Remove this debug line:
                    
                }
            } else {
                // If currentWorld is null, check if player might be in a mine instance directly
                Instance playerInstance = player.getInstance();
                if (playerInstance != null) {
                    // Check all mines to see if this instance belongs to any mine
                    for (var mine : mineManager.getAllMines()) {
                        if (mine.getMineInstance() == playerInstance) {
                            if (mine.getOwnerUUID().equals(player.getUuid().toString()) || 
                                mine.canPlayerAccess(player.getUuid().toString())) {
                                canBreakBlocks = true;
                                // Remove this debug line:
                                
                                // Update the world tracking
                                schematicWorldManager.trackPlayerInWorld(player, mine.getWorldName());
                                break;
                            }
                        }
                    }
                }
                
                if (!canBreakBlocks) {
                    // Remove this debug line:
                    
                }
            }

            // Cancel the event if player cannot break blocks here
            if (!canBreakBlocks) {
                event.setCancelled(true);
                // Send message only if player is trying to break blocks in spawn
                if (currentWorld != null && currentWorld.equals("spawn")) {
                    player.sendMessage("§cYou cannot break blocks at spawn! Use /mine to go to your mine.");
                } else if (currentWorld != null && currentWorld.startsWith("mine_")) {
                    player.sendMessage("§cYou don't have permission to break blocks in this mine!");
                } else {
                    player.sendMessage("§cYou cannot break blocks here!");
                }
                return;
            }

            // Allow the block break and process mining logic
            String blockType = block.registry().material().name();

            // Call mine-specific logic if player is in their own mine
            String expectedMineWorld = "mine_" + player.getUsername().toLowerCase();
            if (currentWorld != null && currentWorld.equals(expectedMineWorld)) {
                mineManager.onBlockBreak(player, blockType);
            }

            // Add block to backpack
            backpackManager.initializePlayer(player);
            Backpack backpack = backpackManager.getBackpack(player);

            if (backpack != null) {
                if (backpack.getAvailableSpace() > 0) {
                    backpack.addBlock(blockType, 1);

                    // Auto-sell if backpack is full or auto-sell is enabled
                    if (backpack.isFull() || backpack.isAutoSellEnabled()) {
                        backpackManager.sellBackpack(player, false);
                    }
                } else {
                    // Backpack is full, auto-sell and then add the block
                    backpackManager.sellBackpack(player, true);
                    backpack.addBlock(blockType, 1);
                }
            }

            // Give pickaxe XP for breaking blocks
            long xpGained = getBlockXP(block);
            pickaxeManager.addPickaxeExp(player, xpGained);

            // Update stats
            statsManager.addBlocksMined(player, 1);

            // Give base money reward with mine multipliers
            double baseReward = getBlockReward(block);
            double multiplier = multiplierManager.getTotalMultiplier(player, "money");
            
            // Apply mine-specific multiplier if player is in their own mine
            PrivateMine playerMine = mineManager.getPlayerMine(player);
            if (playerMine != null && currentWorld != null && currentWorld.equals(expectedMineWorld)) {
                multiplier *= playerMine.getMultiplier();
                // Apply beacon bonus
                multiplier += (playerMine.getBeaconLevel() * 0.1);
            }
            
            double finalReward = baseReward * multiplier;

            currencyManager.addBalance(player, "money", finalReward);

            // Check milestones
            milestoneManager.checkMilestones(player);

            // Update scoreboard
            scoreboardManager.updatePlayerScoreboard(player);

        } catch (Exception e) {
            System.err.println("[MythicPrison] Error handling block break: " + e.getMessage());
            e.printStackTrace();
        }
    }

// Add this helper method to find players by name
private Player findPlayerByName(String playerName) {
    for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
        if (onlinePlayer.getUsername().equalsIgnoreCase(playerName)) {
            return onlinePlayer;
        }
    }
    return null;
}

    private double getBlockReward(Block block) {
        return switch (block.registry().material().name()) {
            case "STONE" -> 1.0;
            case "COAL_ORE" -> 5.0;
            case "IRON_ORE" -> 10.0;
            case "GOLD_ORE" -> 25.0;
            case "DIAMOND_ORE" -> 100.0;
            case "EMERALD_ORE" -> 250.0;
            default -> 0.5;
        };
    }

    private long getBlockXP(Block block) {
        return switch (block.registry().material().name()) {
            case "STONE" -> 1L;
            case "COAL_ORE" -> 5L;
            case "IRON_ORE" -> 10L;
            case "GOLD_ORE" -> 25L;
            case "DIAMOND_ORE" -> 50L;
            case "EMERALD_ORE" -> 100L;
            case "OBSIDIAN" -> 75L;
            case "NETHERITE_BLOCK" -> 200L;
            default -> 1L;
        };
    }

    private void registerCommands() {
    System.out.println("[MythicPrison] Registering commands...");

    try {
        // Register core commands with error handling
        registerCommandSafely("EconomyCommand", EconomyCommand::register);
        registerCommandSafely("PayCommand", PayCommand::register);
        registerCommandSafely("MilestoneCommand", MilestoneCommand::register);
        registerCommandSafely("StatsCommand", StatsCommand::register);
        registerCommandSafely("RebirthCommand", RebirthCommand::register);
        registerCommandSafely("ResetRankCommand", ResetRankCommand::register);
        registerCommandSafely("AdminCommand", AdminCommand::register);
        registerCommandSafely("RankupCommand", RankupCommand::register);
        registerCommandSafely("RankCommand", RankCommand::register);
        registerCommandSafely("PrestigeCommand", PrestigeCommand::register);
        registerCommandSafely("AscensionCommand", AscensionCommand::register);
        registerCommandSafely("AutoRankupCommand", AutoRankupCommand::register);
        registerCommandSafely("AutoPrestigeCommand", AutoPrestigeCommand::register);
        registerCommandSafely("AutoRebirthCommand", AutoRebirthCommand::register);
        registerCommandSafely("BackpackCommand", BackpackCommand::register);

        // Register MineCommand directly (it doesn't use static register method)
        try {
            MinecraftServer.getCommandManager().register(new MineCommand());
            System.out.println("[MythicPrison] ✓ MineCommand registered");
        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error registering MineCommand: " + e.getMessage());
        }

        // Register FriendsCommand directly (it doesn't use static register method)
        try {
            MinecraftServer.getCommandManager().register(new FriendsCommand());
            System.out.println("[MythicPrison] ✓ FriendsCommand registered");
        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error registering FriendsCommand: " + e.getMessage());
        }

        // Remove this duplicate line that's causing the error
        // MinecraftServer.getCommandManager().register(new FriendCommand());

        // Register other commands that may or may not exist
        registerCommandIfExists("RankupMaxCommand");
        registerCommandIfExists("GameModeCommand");
        registerCommandIfExists("RebirthMaxCommand");
        registerCommandIfExists("PrestigeMaxCommand");
        registerCommandIfExists("FlyCommand");
        registerCommandIfExists("HelpCommand");
        registerCommandIfExists("BalanceCommand");
        registerCommandIfExists("ShopCommand");
        registerCommandIfExists("GangCommand");
        registerCommandIfExists("PetCommand");
        registerCommandIfExists("PickaxeCommand");
        registerCommandIfExists("MultiplierCommand");
        registerCommandIfExists("MultiplyersCommand");
        registerCommandIfExists("AscendCommand");
        registerCommandIfExists("LeaderboardCommand");
        registerCommandIfExists("MilestonesCommand");
        registerCommandIfExists("VisitCommand");
        registerCommandIfExists("SpawnCommand");
        registerCommandIfExists("CreativeCommand");
        registerCommandIfExists("SurvivalCommand");
        registerCommandIfExists("SchematicCommand");

        System.out.println("[MythicPrison] ✓ All available commands registered successfully!");

    } catch (Exception e) {
        System.err.println("[MythicPrison] ✗ Error registering commands: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void registerCommandSafely(String commandName, Runnable registerMethod) {
        try {
            registerMethod.run();
            System.out.println("[MythicPrison] ✓ " + commandName + " registered");
        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error registering " + commandName + ": " + e.getMessage());
        }
    }

    private void registerCommandIfExists(String commandClassName) {
        try {
            Class<?> commandClass = Class.forName("mythic.prison.commands." + commandClassName);
            var registerMethod = commandClass.getDeclaredMethod("register");
            registerMethod.invoke(null);
            System.out.println("[MythicPrison] ✓ " + commandClassName + " registered");
        } catch (ClassNotFoundException e) {
            System.out.println("[MythicPrison] ⚠ " + commandClassName + " not found, skipping...");
        } catch (NoSuchMethodException e) {
            System.out.println("[MythicPrison] ⚠ " + commandClassName + " found but no register() method, skipping...");
        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error registering " + commandClassName + ": " + e.getMessage());
        }
    }

private void startUpdaters() {
    try {
        System.out.println("[MythicPrison] Starting background updaters...");

        // Scoreboard updater
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (scoreboardManager != null) {
                    scoreboardManager.updateAllScoreboards();
                }
            } catch (Exception e) {
                System.err.println("[MythicPrison] Error in scoreboard updater: " + e.getMessage());
            }
        }, 1, 3, TimeUnit.SECONDS); // Update every 3 seconds

        // Pickaxe validation updater - check every 10 seconds (more frequent)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (pickaxeManager != null) {
                    for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        pickaxeManager.validatePlayerPickaxe(player);
                    }
                }
            } catch (Exception e) {
                System.err.println("[MythicPrison] Error in pickaxe validation: " + e.getMessage());
            }
        }, 5, 10, TimeUnit.SECONDS); // Check every 10 seconds

        // Tab updater for ping and player counts
        scheduler.scheduleAtFixedRate(() -> {
            try {
                mythic.prison.listeners.ChatListener.updateAllTabHeadersFooters();
            } catch (Exception e) {
                System.err.println("Error in tab updater: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);

        System.out.println("[MythicPrison] ✓ Updaters started!");

    } catch (Exception e) {
        System.err.println("[MythicPrison] ✗ Error starting updaters: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void shutdown() {
        try {
            System.out.println("[MythicPrison] Shutting down gracefully...");

            // Save worlds
            // if (worldManager != null) {
            //     worldManager.shutdown();
            //     System.out.println("[MythicPrison] ✓ WorldManager shutdown");
            // }

            // Close database connections
            if (mongoManager != null) {
                mongoManager.disconnect();
                System.out.println("[MythicPrison] ✓ MongoDB disconnected");
            }

            if (redisManager != null) {
                redisManager.disconnect();
                System.out.println("[MythicPrison] ✓ Redis disconnected");
            }

            // Shutdown scheduler
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                System.out.println("[MythicPrison] ✓ Scheduler shutdown");
            }

            System.out.println("[MythicPrison] ✓ Shutdown complete!");

        } catch (Exception e) {
            System.err.println("[MythicPrison] ✗ Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters for all managers
    public static MythicPrison getInstance() {
        return instance;
    }

    public MongoManager getMongoManager() {
        return mongoManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public RankingManager getRankingManager() {
        return rankingManager;
    }

    public MultiplierManager getMultiplierManager() {
        return multiplierManager;
    }

    public BackpackManager getBackpackManager() {
        return backpackManager;
    }

    public PickaxeManager getPickaxeManager() {
        return pickaxeManager;
    }

    public PetManager getPetManager() {
        return petManager;
    }

    public GangManager getGangManager() {
        return gangManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public MilestoneManager getMilestoneManager() {
        return milestoneManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public SchematicWorldManager getSchematicWorldManager() {
        return schematicWorldManager;
    }

    // public WorldManager getWorldManager() {
    //     return worldManager;
    // }

    public Instance getMainInstance() {
        return mainInstance;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Gets the data folder for the MythicPrison plugin
     * @return File representing the data folder
     */
    public File getDataFolder() {
        return dataFolder;
    }

    // Add this getter method
    public FriendsManager getFriendsManager() {
        return friendsManager;
    }

// Replace the findPlayerByUsername method with this corrected version:
private Player findPlayerByUsername(String username) {
    return MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
            .filter(p -> p.getUsername().equalsIgnoreCase(username))
            .findFirst().orElse(null);
}
}