package sh.okx.webdeals.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.webdeals.Webdeals;

public class GuiListener implements Listener {
    private Gui gui;

    public GuiListener(Gui gui) {
        this.gui = gui;
    }

    @EventHandler
    public void on(InventoryClickEvent e) {
        HumanEntity entity = e.getWhoClicked();
        if(!gui.isFor(e.getWhoClicked())) {
            return;
        }

        e.setCancelled(true);

        if(e.getClickedInventory() == null) {
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Webdeals.class), entity::closeInventory);
            return;
        } else if(!e.getInventory().equals(e.getClickedInventory())) {
            // if the inventory clicked is not the upper one
            return;
        }

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Webdeals.class), () -> gui.handle(e));
    }

    @EventHandler
    public void on(InventoryCloseEvent e) {
        if(!gui.isFor(e.getPlayer())) {
            return;
        }

        gui.close(e.getPlayer());
    }
}
