package sh.okx.webdeals.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import sh.okx.webdeals.Webdeals;
import sh.okx.webdeals.api.SimpleCoupon;
import sh.okx.webdeals.gui.Gui;
import sh.okx.webdeals.util.ItemBuilder;
import sh.okx.webdeals.util.NumberUtil;

import java.util.function.Consumer;

public class ListCouponsHandler implements Consumer<InventoryClickEvent> {
    private Webdeals plugin;

    public ListCouponsHandler(Webdeals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(InventoryClickEvent e) {
        assert e.getWhoClicked() instanceof Player;

        Player player = (Player) e.getWhoClicked();
        player.closeInventory();

        plugin.sendMessage(player, "gui.root.coupons.please_wait");
        plugin.getManager().getCoupons(player).thenAccept(coupons -> Bukkit.getScheduler().runTask(plugin, () -> {

            if(coupons == null) {
                plugin.sendMessage(player, "error.get_coupons");
                return;
            }

            if(coupons.size() == 0) {
                plugin.sendMessage(player, "gui.root.coupons.none");
                return;
            }

            Gui list = new Gui(plugin, "Coupons",
                NumberUtil.roundUp(coupons.size(), 9) / 9);
            list.setUnregisterOnClose(true);

            for(int i = 0; i < coupons.size(); i++) {
                SimpleCoupon coupon = coupons.get(i);

                list.register(new ItemBuilder(Material.PAPER)
                        .setDisplayName(plugin.getMessage("gui.list.name", plugin.getManager().format(coupon.getValue())))
                        .setLore(plugin.getMessage("gui.list.description", coupon.getCode()))
                        .build(), i, new CouponClickHandler(plugin, coupon.getCode()));
            }

            list.open(player);
        }));
    }
}
