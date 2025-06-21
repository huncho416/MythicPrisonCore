package mythic.prison.managers;

import net.minestom.server.entity.Player;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import mythic.prison.MythicPrison;
import mythic.prison.data.perks.PickaxePerk;
import mythic.prison.data.perks.PerkRarity;
import mythic.prison.utils.ChatUtil;

import java.util.*;

public class PerkManager {

    private final Map<String, Map<String, Integer>> playerPerks = new ConcurrentHashMap<>();
    private final Map<String, PickaxePerk> availablePerks = new HashMap<>();
    private final Map<String, Integer> perkRolls = new ConcurrentHashMap<>();

    public PerkManager() {
        initializePerks();
    }

    private void initializePerks() {
        // Normal tier perks (60% chance)
        availablePerks.put("tokens1", new PickaxePerk("Tokens I", PerkRarity.NORMAL, 1.05, "tokens", 1));
        availablePerks.put("essence1", new PickaxePerk("Essence I", PerkRarity.NORMAL, 1.05, "essence", 1));
        availablePerks.put("greed1", new PickaxePerk("Greed I", PerkRarity.NORMAL, 1.03, "money", 1));

        // Rare tier perks (25% chance)
        availablePerks.put("tokens2", new PickaxePerk("Tokens II", PerkRarity.RARE, 1.1, "tokens", 2));
        availablePerks.put("essence2", new PickaxePerk("Essence II", PerkRarity.RARE, 1.1, "essence", 2));
        availablePerks.put("prestige1", new PickaxePerk("Prestige I", PerkRarity.RARE, 1.05, "prestige", 1));
        availablePerks.put("glamor1", new PickaxePerk("Glamor I", PerkRarity.RARE, 1.05, "universal", 1));

        // Elite tier perks (10% chance)
        availablePerks.put("tokens3", new PickaxePerk("Tokens III", PerkRarity.ELITE, 1.15, "tokens", 3));
        availablePerks.put("rainbow1", new PickaxePerk("Rainbow I", PerkRarity.ELITE, 1.08, "universal", 2));
        availablePerks.put("gold1", new PickaxePerk("Gold I", PerkRarity.ELITE, 1.1, "money", 2));
        availablePerks.put("blockgreed1", new PickaxePerk("Block Greed I", PerkRarity.ELITE, 1.05, "pickaxe", 1));

        // Legendary tier perks (3.5% chance)
        availablePerks.put("tokens4", new PickaxePerk("Tokens IV", PerkRarity.LEGENDARY, 1.2, "tokens", 4));
        availablePerks.put("diamond1", new PickaxePerk("Diamond I", PerkRarity.LEGENDARY, 1.15, "money", 3));
        availablePerks.put("valor1", new PickaxePerk("Valor I", PerkRarity.LEGENDARY, 1.12, "universal", 3));
        availablePerks.put("intensity1", new PickaxePerk("Intensity I", PerkRarity.LEGENDARY, 1.1, "enchants", 1));

        // Mythical tier perks (1% chance)
        availablePerks.put("tokens5", new PickaxePerk("Tokens V", PerkRarity.MYTHICAL, 1.25, "tokens", 5));
        availablePerks.put("godlymultiplier", new PickaxePerk("Godly Multiplier", PerkRarity.MYTHICAL, 1.2, "universal", 4));
        availablePerks.put("creditboost", new PickaxePerk("Credit Boost", PerkRarity.MYTHICAL, 1.15, "credits", 1));

        // Godly tier perks (0.4% chance)
        availablePerks.put("omegaboost", new PickaxePerk("Omega Boost", PerkRarity.GODLY, 1.3, "universal", 5));
        availablePerks.put("enchantmaster", new PickaxePerk("Enchant Master", PerkRarity.GODLY, 1.25, "enchants", 2));

        // Ultimate tier perks (0.1% chance)
        availablePerks.put("ultimatepower", new PickaxePerk("Ultimate Power", PerkRarity.ULTIMATE, 1.5, "universal", 10));
        availablePerks.put("godmode", new PickaxePerk("God Mode", PerkRarity.ULTIMATE, 2.0, "all", 1));
    }

    public void initializePlayer(Player player) {
        String uuid = player.getUuid().toString();
        playerPerks.putIfAbsent(uuid, new HashMap<>());
    }

    public void initializePlayer(Object player) {
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public boolean hasPerk(Object player, String perkId) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return false;
        
        return playerPerks.getOrDefault(uuid, new HashMap<>()).containsKey(perkId);
    }

    public void addPerk(Object player, String perkId) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return;
        
        if (player instanceof Player p) {
            initializePlayer(p);
        } else {
            playerPerks.putIfAbsent(uuid, new HashMap<>());
        }
        
        Map<String, Integer> perks = playerPerks.get(uuid);
        perks.put(perkId, perks.getOrDefault(perkId, 0) + 1);
    }

    public void removePerk(Object player, String perkId) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return;
        
        Map<String, Integer> perks = playerPerks.get(uuid);
        if (perks != null) {
            perks.remove(perkId);
        }
    }

    public Set<String> getPlayerPerks(Object player) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return new HashSet<>();
        
        return new HashSet<>(playerPerks.getOrDefault(uuid, new HashMap<>()).keySet());
    }

    public void clearPerks(Object player) {
        String uuid = getPlayerUUID(player);
        if (uuid == null) return;
        
        playerPerks.remove(uuid);
        if (player instanceof Player p) {
            initializePlayer(p);
        }
    }

    public boolean hasPerkRolls(Object player) {
        String uuid = getPlayerUUID(player);
        return perkRolls.getOrDefault(uuid, 0) > 0;
    }

    public int getPerkRolls(Object player) {
        String uuid = getPlayerUUID(player);
        return perkRolls.getOrDefault(uuid, 0);
    }

    public void addPerkRolls(Object player, int amount) {
        String uuid = getPlayerUUID(player);
        perkRolls.put(uuid, getPerkRolls(player) + amount);
    }

    public void removePerkRoll(Object player) {
        int current = getPerkRolls(player);
        if (current > 0) {
            String uuid = getPlayerUUID(player);
            perkRolls.put(uuid, current - 1);
        }
    }

    public PickaxePerk rollPerk() {
        double random = Math.random() * 100;

        if (random <= 0.1) {
            return getRandomPerkOfRarity(PerkRarity.ULTIMATE);
        } else if (random <= 0.5) {
            return getRandomPerkOfRarity(PerkRarity.GODLY);
        } else if (random <= 1.5) {
            return getRandomPerkOfRarity(PerkRarity.MYTHICAL);
        } else if (random <= 5.0) {
            return getRandomPerkOfRarity(PerkRarity.LEGENDARY);
        } else if (random <= 15.0) {
            return getRandomPerkOfRarity(PerkRarity.ELITE);
        } else if (random <= 40.0) {
            return getRandomPerkOfRarity(PerkRarity.RARE);
        } else {
            return getRandomPerkOfRarity(PerkRarity.NORMAL);
        }
    }

    private PickaxePerk getRandomPerkOfRarity(PerkRarity rarity) {
        List<PickaxePerk> perksOfRarity = new ArrayList<>();
        for (PickaxePerk perk : availablePerks.values()) {
            if (perk.getRarity() == rarity) {
                perksOfRarity.add(perk);
            }
        }

        if (perksOfRarity.isEmpty()) {
            return availablePerks.values().iterator().next(); // Fallback
        }

        return perksOfRarity.get(new Random().nextInt(perksOfRarity.size()));
    }

    public boolean rollPerkForPlayer(Object player) {
        if (!hasPerkRolls(player)) {
            ChatUtil.sendError(player, "You don't have any perk rolls!");
            return false;
        }

        removePerkRoll(player);
        PickaxePerk rolledPerk = rollPerk();

        // Add perk to player
        String uuid = getPlayerUUID(player);
        Map<String, Integer> perks = playerPerks.get(uuid);
        String perkKey = findPerkKey(rolledPerk);

        int currentLevel = perks.getOrDefault(perkKey, 0);
        perks.put(perkKey, currentLevel + 1);

        // Apply perk effect
        applyPerkEffect(player, rolledPerk);

        // Send success message
        String rarityColor = rolledPerk.getRarity().getColor();
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m            §r §d§lPERK ROLLED! §r§d§l§m            ");
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§fYou rolled: " + rarityColor + "§l" + rolledPerk.getName());
        ChatUtil.sendMessage(player, "§fRarity: " + rarityColor + "§l" + rolledPerk.getRarity().name());
        ChatUtil.sendMessage(player, "§fEffect: §a+" + String.format("%.1f", (rolledPerk.getMultiplier() - 1) * 100) + "% " + rolledPerk.getBoostType());
        ChatUtil.sendMessage(player, "");
        ChatUtil.sendMessage(player, "§d§l§m                                                    ");
        ChatUtil.sendMessage(player, "");

        return true;
    }

    private void applyPerkEffect(Object player, PickaxePerk perk) {
        MultiplierManager multiplierManager = MythicPrison.getInstance().getMultiplierManager();

        switch (perk.getBoostType().toLowerCase()) {
            case "universal":
                multiplierManager.addMultiplier(player, "money", perk.getMultiplier() - 1);
                multiplierManager.addMultiplier(player, "tokens", perk.getMultiplier() - 1);
                multiplierManager.addMultiplier(player, "souls", perk.getMultiplier() - 1);
                break;
            case "all":
                String[] multiplierTypes = {"money", "tokens", "souls", "experience", "damage"};
                for (String type : multiplierTypes) {
                    multiplierManager.addMultiplier(player, type, perk.getMultiplier() - 1);
                }
                break;
            default:
                multiplierManager.addMultiplier(player, perk.getBoostType(), perk.getMultiplier() - 1);
                break;
        }
    }

    private String findPerkKey(PickaxePerk perk) {
        for (Map.Entry<String, PickaxePerk> entry : availablePerks.entrySet()) {
            if (entry.getValue().equals(perk)) {
                return entry.getKey();
            }
        }
        return "unknown";
    }

    public Map<String, PickaxePerk> getAvailablePerks() {
        return availablePerks;
    }

    private String getPlayerUUID(Object player) {
        if (player instanceof Player p) {
            return p.getUuid().toString();
        }
        return player != null ? player.toString() : null;
    }
}