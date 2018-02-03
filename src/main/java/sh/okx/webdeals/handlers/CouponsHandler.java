package sh.okx.webdeals.handlers;

import org.bukkit.event.inventory.InventoryClickEvent;
import sh.okx.webdeals.Webdeals;

import java.util.function.Consumer;

public class CouponsHandler implements Consumer<InventoryClickEvent> {
    private Webdeals plugin;

    public CouponsHandler(Webdeals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(InventoryClickEvent inventoryClickEvent) {
        //Gui coupons = new Gui("Coupons", 1);
        throw new UnsupportedOperationException(); // TODO Add coupons
    }
}
