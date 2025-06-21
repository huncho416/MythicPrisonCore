package mythic.prison.listeners;

import mythic.prison.MythicPrison;
import mythic.prison.managers.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;

public class MiningListener implements EventListener<PlayerBlockBreakEvent> {

    @Override
    public Class<PlayerBlockBreakEvent> eventType() {
        return PlayerBlockBreakEvent.class;
    }

    @Override
    public Result run(PlayerBlockBreakEvent event) {
        onBlockBreak(event);
        return Result.SUCCESS;
    }

    public void onBlockBreak(PlayerBlockBreakEvent event) {
        // Only process if the event hasn't been cancelled by the main handler
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Point position = event.getBlockPosition();
        
        String blockType = getBlockType(block);
        // Only handle additional rewards/stats, not permissions
        handleAdditionalRewards(player, blockType, position);
    }

    private void handleAdditionalRewards(Player player, String blockType, Object position) {
        // This method only handles extra rewards, not permissions or main logic
        // The main permission checking is done in MythicPrison.handleBlockBreak()
        
        // You can add any additional reward logic here that doesn't conflict
        // with the main mining system
    }

    private String getBlockType(Object block) {
        if (block instanceof Block minestomBlock) {
            return minestomBlock.name().toLowerCase();
        }
        return "stone"; // Default fallback
    }

    private String determineMineFromPosition(Object position) {
        if (position instanceof Point point) {
            int y = (int) point.y();
            
            // Determine mine based on Y coordinate
            if (y >= 60) return "mine_a";
            else if (y >= 40) return "mine_b";
            else if (y >= 20) return "mine_c";
            else if (y >= 0) return "mine_d";
            else if (y >= -20) return "mine_e";
            else return "mine_f";
        }
        return "mine_a"; // Default
    }

    private int getYCoordinate(Object position) {
        if (position instanceof Point point) {
            return (int) point.y();
        }
        return 64; // Default
    }
    
    private double getBlockValue(String blockType) {
        return switch (blockType.toLowerCase()) {
            case "stone" -> 1.0;
            case "cobblestone" -> 1.5;
            case "coal_ore" -> 5.0;
            case "iron_ore" -> 10.0;
            case "gold_ore" -> 25.0;
            case "diamond_ore" -> 100.0;
            case "emerald_ore" -> 250.0;
            case "netherite_scrap" -> 500.0;
            default -> 1.0;
        };
    }
}