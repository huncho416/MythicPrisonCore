package mythic.prison.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.coordinate.Pos;
import java.time.Duration;

public class ItemInteractionListener {

    public void onItemDrop(ItemDropEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemStack();

        if (droppedItem.material() != Material.AIR && droppedItem.amount() > 0) {
            // Create item entity in the world
            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setInstance(player.getInstance(), player.getPosition().add(0, 1, 0));
            itemEntity.setPickupDelay(Duration.ofMillis(500)); // 0.5 second pickup delay
        }
    }

    public void onItemPickup(PickupItemEvent event) {
        // Get the entity that's picking up the item
        if (!(event.getEntity() instanceof Player player)) {
            return; // Only handle player pickups
        }

        ItemEntity itemEntity = event.getItemEntity();
        ItemStack itemStack = itemEntity.getItemStack();

        // Check if pickup delay has passed (Minecraft-like behavior)
        if (itemEntity.getAliveTicks() < 10) { // 10 ticks = 0.5 seconds pickup delay
            event.setCancelled(true);
            return;
        }

        // Check if player has inventory space
        if (canPickupItem(player, itemStack)) {
            // Add to player inventory
            boolean added = player.getInventory().addItemStack(itemStack);
            if (added) {
                // Remove the item entity from the world
                itemEntity.remove();
            } else {
                // Cancel pickup if inventory is full
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    public void onBlockBreak(PlayerBlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Pos position = Pos.fromPoint(event.getBlockPosition());

        // Allow the block to break immediately - no timing restrictions
        // This provides a smooth mining experience
        event.setCancelled(false);

        // Drop item based on block type
        ItemStack droppedItem = getDropFromBlock(block);
        if (droppedItem != null && droppedItem.material() != Material.AIR) {
            ItemEntity itemEntity = new ItemEntity(droppedItem);
            // Spawn item slightly above the broken block position
            Pos spawnPos = position.add(0.5, 0.5, 0.5);
            itemEntity.setInstance(player.getInstance(), spawnPos);
            itemEntity.setPickupDelay(Duration.ofMillis(300)); // 0.3 second pickup delay
        }

        // Call mining logic for stats, backpack, etc.
        handleMiningLogic(player, block, position);
    }

    private void handleMiningLogic(Player player, Block block, Pos position) {
        try {
            // Get the MythicPrison instance and handle mining logic
            var plugin = mythic.prison.MythicPrison.getInstance();
            
            // Add to player stats - Fixed method name
            if (plugin.getStatsManager() != null) {
                plugin.getStatsManager().addBlocksMined(player, 1);
            }

            // Add blocks to backpack
            if (plugin.getBackpackManager() != null) {
                plugin.getBackpackManager().addBlocks(player, block.name(), 1);
            }

            // Check for milestone progress
            if (plugin.getMilestoneManager() != null) {
                plugin.getMilestoneManager().checkMilestones(player);
            }

            // Add experience to active pet if any
            if (plugin.getPetManager() != null) {
                plugin.getPetManager().addExperienceToActivePet(player, 1);
            }

            // Update pickaxe durability/experience
            if (plugin.getPickaxeManager() != null) {
                plugin.getPickaxeManager().addExperience(player, 1);
            }

        } catch (Exception e) {
            // Silently handle errors - removed debug output
        }
    }

    private boolean canPickupItem(Player player, ItemStack itemStack) {
        // Check if player has space in inventory
        return player.getInventory().addItemStack(itemStack.withAmount(0)) ||
                hasSpaceForItem(player, itemStack);
    }

    private boolean hasSpaceForItem(Player player, ItemStack itemStack) {
        int remainingAmount = itemStack.amount();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack slotItem = player.getInventory().getItemStack(i);

            if (slotItem.material() == Material.AIR) {
                // Empty slot can fit the entire stack
                return true;
            } else if (slotItem.material() == itemStack.material() &&
                    slotItem.amount() < slotItem.material().maxStackSize()) {
                // Existing stack has room
                int availableSpace = slotItem.material().maxStackSize() - slotItem.amount();
                remainingAmount -= availableSpace;
                if (remainingAmount <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private ItemStack getDropFromBlock(Block block) {
        // Define what items drop from each block type
        Material dropMaterial = switch (block.name()) {
            case "stone" -> Material.COBBLESTONE;
            case "grass_block" -> Material.DIRT;
            case "iron_ore" -> Material.RAW_IRON;
            case "gold_ore" -> Material.RAW_GOLD;
            case "diamond_ore" -> Material.DIAMOND;
            case "coal_ore" -> Material.COAL;
            case "emerald_ore" -> Material.EMERALD;
            default -> {
                // Try to get material from block material, fallback to cobblestone
                Material material = block.registry().material();
                yield material != null ? material : Material.COBBLESTONE;
            }
        };

        return ItemStack.of(dropMaterial, 1);
    }
}