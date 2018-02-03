package sh.okx.webdeals.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.webdeals.Webdeals;

import java.util.*;
import java.util.function.Consumer;

public class Gui {
    private Set<UUID> viewing = new HashSet<>();
    private Inventory inventory;

    private Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    private GuiListener listener;

    protected void handle(InventoryClickEvent event) {
        int slot = event.getSlot();
        if(handlers.containsKey(slot)) {
            handlers.get(slot).accept(event);
        }
    }

    public Gui(String name, int rows) {
        this.inventory = Bukkit.createInventory(null, rows*9, name);

        Bukkit.getPluginManager().registerEvents(listener = new GuiListener(this), JavaPlugin.getPlugin(Webdeals.class));
    }

    public boolean isFor(HumanEntity entity) {
        return viewing.contains(entity.getUniqueId());
    }

    public void register(ItemStack itemStack, int slot) {
        inventory.setItem(slot, itemStack);
    }

    public void register(ItemStack item, int slot, Consumer<InventoryClickEvent> handler) {
        register(item, slot);
        handlers.put(slot, handler);
    }

    public void open(HumanEntity entity) {
        entity.openInventory(inventory);
        viewing.add(entity.getUniqueId());
    }

    protected void close(HumanEntity entity) {
        viewing.remove(entity.getUniqueId());
    }

    public void unregister() {
        HandlerList.unregisterAll(listener);
    }
}
