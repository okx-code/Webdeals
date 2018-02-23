package sh.okx.webdeals.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Gui implements InventoryHolder {
    private Inventory inventory;

    private Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    private GuiListener listener;
    private Plugin plugin;
    private boolean closeIfClickOutsideOfWindow = false;
    private boolean unregisterOnClose = false;

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isCloseIfClickOutsideOfWindow() {
        return closeIfClickOutsideOfWindow;
    }

    /**
     * Whether to close the GUI if a player clicks outside of the GUI window.
     */
    public void setCloseIfClickOutsideOfWindow(boolean closeIfClickOutsideOfWindow) {
        this.closeIfClickOutsideOfWindow = closeIfClickOutsideOfWindow;
    }

    public boolean isUnregisterOnClose() {
        return unregisterOnClose;
    }

    /**
     * If set, InventoryClickListener will unregister itself immediately when the GUI is closed.
     * Use this when you are only showing the GUI to a single player, or you will face memory leaks.
     */
    public void setUnregisterOnClose(boolean unregisterOnClose) {
        this.unregisterOnClose = unregisterOnClose;
    }

    protected void handle(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (handlers.containsKey(slot)) {
            handlers.get(slot).accept(event);
        }
    }

    public Gui(Plugin plugin, String name, int rows) {
        this.plugin = plugin;

        this.inventory = Bukkit.createInventory(this, rows * 9, name);
        Bukkit.getPluginManager().registerEvents(listener = new GuiListener(this), plugin);
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
    }

    public void unregister() {
        HandlerList.unregisterAll(listener);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
