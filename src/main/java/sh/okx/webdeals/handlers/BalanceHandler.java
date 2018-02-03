package sh.okx.webdeals.handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import sh.okx.webdeals.Webdeals;

import java.util.function.Consumer;

public class BalanceHandler implements Consumer<InventoryClickEvent> {
    private Webdeals plugin;

    public BalanceHandler(Webdeals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        plugin.sendMessage(player, "command.balance.success.self", player.getName(),
                plugin.getManager().format(plugin.getManager().getBalance(player)));
        player.closeInventory();
    }
}
