package mythic.prison.data.mine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MineWorld {

    private final Map<String, Mine> mines = new ConcurrentHashMap<>();
    private final Map<String, Object> worlds = new HashMap<>();

    public MineWorld() {
        initializeDefaultMines();
    }

    private void initializeDefaultMines() {
        // Initialize default mines
        addMine(new Mine("a", "A", "§aA Mine"));
        addMine(new Mine("b", "B", "§bB Mine"));
        addMine(new Mine("c", "C", "§cC Mine"));
        addMine(new Mine("d", "D", "§dD Mine"));
        addMine(new Mine("e", "E", "§eE Mine"));
        addMine(new Mine("f", "F", "§fF Mine"));
        addMine(new Mine("g", "G", "§2G Mine"));
        addMine(new Mine("h", "H", "§5H Mine"));
        addMine(new Mine("i", "I", "§6I Mine"));
        addMine(new Mine("j", "J", "§9J Mine"));
        addMine(new Mine("spawn", "spawn", "§fSpawn"));
    }

    public void addMine(Mine mine) {
        mines.put(mine.getId().toLowerCase(), mine);
    }

    public Mine getMine(String id) {
        return mines.get(id.toLowerCase());
    }

    public Map<String, Mine> getAllMines() {
        return new HashMap<>(mines);
    }

    public boolean hasMine(String id) {
        return mines.containsKey(id.toLowerCase());
    }

    public void removeMine(String id) {
        mines.remove(id.toLowerCase());
    }

    public void addWorld(String name, Object world) {
        worlds.put(name, world);
    }

    public Object getWorld(String name) {
        return worlds.get(name);
    }

    public boolean hasWorld(String name) {
        return worlds.containsKey(name);
    }

    public void removeWorld(String name) {
        worlds.remove(name);
    }

    public Map<String, Object> getAllWorlds() {
        return new HashMap<>(worlds);
    }

    public void regenerateAllMines() {
        for (Mine mine : mines.values()) {
            if (mine.needsRegeneration()) {
                mine.regenerate();
            }
        }
    }

    public void onBlockBreak(String mineId, String blockType) {
        Mine mine = getMine(mineId);
        if (mine != null) {
            mine.onBlockBreak(blockType);
        }
    }

    public int getTotalMines() {
        return mines.size();
    }

    public int getRegeneratingMines() {
        return (int) mines.values().stream()
                .filter(Mine::isRegenerating)
                .count();
    }
}