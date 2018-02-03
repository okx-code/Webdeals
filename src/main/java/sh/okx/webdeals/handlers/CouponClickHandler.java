package sh.okx.webdeals.handlers;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import sh.okx.webdeals.Webdeals;

import java.util.function.Consumer;

public class CouponClickHandler implements Consumer<InventoryClickEvent> {
    private Webdeals plugin;
    private String code;

    public CouponClickHandler(Webdeals plugin, String code) {
        this.plugin = plugin;
        this.code = code;
    }

    @Override
    public void accept(InventoryClickEvent e) {
        assert e.getWhoClicked() instanceof Player;

        Player player = (Player) e.getWhoClicked();
        player.closeInventory();

        String message = plugin.getMessage("coupon.code", code);
        player.spigot().sendMessage(new ComponentBuilder(message)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to put in chat so you can copy").create()))
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, code))
                .create());
    }
}
