package sh.okx.webdeals.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class HelpHandler implements Consumer<InventoryClickEvent> {
    @Override
    public void accept(InventoryClickEvent e) {
        HumanEntity who = e.getWhoClicked();
        who.closeInventory();
        Bukkit.dispatchCommand(who, "webdeals help");
    }
}
