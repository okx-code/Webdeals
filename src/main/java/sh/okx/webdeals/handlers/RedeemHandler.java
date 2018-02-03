package sh.okx.webdeals.handlers;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import sh.okx.webdeals.Webdeals;
import sh.okx.webdeals.api.WebdealManager;

import java.util.function.Consumer;

public class RedeemHandler implements Consumer<InventoryClickEvent> {
    private Webdeals plugin;
    private double amount;

    public RedeemHandler(Webdeals plugin, double amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    @Override
    public void accept(InventoryClickEvent e) {
        assert e.getWhoClicked() instanceof Player;

        Player player = (Player) e.getWhoClicked();
        player.closeInventory();

        WebdealManager manager = plugin.getManager();

        double balance = manager.getBalance(player);
        if(balance < amount) {
            plugin.sendMessage(player, "coupon.not_enough",
                    manager.format(amount), manager.format(balance), manager.format(amount - balance));
            return;
        }

        plugin.sendMessage(player, "gui.root.redeem.please_wait");
        manager.createCoupon(player, amount).thenAccept(code -> Bukkit.getScheduler().runTask(plugin, () -> {
            if(code == null) {
                plugin.sendMessage(player, "coupon.creation_error");
                plugin.getLogger().severe("There was an error creating a coupon.");
                return;
            }

            manager.take(player, amount);

            String message = plugin.getMessage("coupon.success", manager.format(amount), code);

            player.spigot().sendMessage(new ComponentBuilder(message)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to put in chat so you can copy").create()))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, code))
                    .create());
        }));
    }
}
