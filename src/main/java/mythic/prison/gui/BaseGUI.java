package mythic.prison.gui;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGUI {
    
    protected Player player;
    protected Inventory inventory;
    protected String title;
    protected InventoryType type;
    
    public BaseGUI(Player player, String title, InventoryType type) {
        this.player = player;
        this.title = title;
        this.type = type;
        this.inventory = new Inventory(type, Component.text(title));
        setupClickHandler();
    }
    
    private void setupClickHandler() {
        // Use global event handler for inventory clicks
        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            // Only handle events for this specific inventory and player
            if (event.getInventory() == this.inventory && event.getPlayer() == this.player) {
                event.setCancelled(true);
                handleClick(event.getSlot(), event.getClick());
            }
        });
    }
    
    protected abstract void handleClick(int slot, Click click);
    
    protected abstract void populateItems();
    
    public void open() {
        populateItems();
        player.openInventory(inventory);
    }
    
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        List<Component> loreComponents = new ArrayList<>();
        if (lore != null) {
            for (String line : lore) {
                loreComponents.add(Component.text(line));
            }
        }
        
        return ItemStack.builder(material)
                .customName(Component.text(name))
                .lore(loreComponents)
                .build();
    }
    
    protected ItemStack createItem(Material material, String name, String... loreLines) {
        List<String> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(line);
        }
        return createItem(material, name, lore);
    }
    
    protected void fillEmpty(Material material) {
        ItemStack filler = ItemStack.builder(material)
                .customName(Component.text(" "))
                .build();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItemStack(i).isAir()) {
                inventory.setItemStack(i, filler);
            }
        }
    }
    
    public void close() {
        player.closeInventory();
    }
}