package sh.okx.webdeals.api;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import sh.okx.webdeals.Webdeals;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public abstract class WebdealManager {
    protected Webdeals plugin;
    private FileConfiguration config;
    private DecimalFormat df;

    public FileConfiguration getConfig() {
        return config;
    }

    public WebdealManager(Webdeals plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig("balances");

        reload();
    }

    public void reload() {
        df = new DecimalFormat(plugin.getMessage("money_format"));
    }

    public String format(double v) {
        return df.format(v);
    }

    private String getPath(Player player) {
        return "balances." + player.getUniqueId();
    }

    public void add(Player player, double amount) {
        config.set(getPath(player), getBalance(player) + amount);
        plugin.saveConfig(config, "balances");
    }

    public void take(Player player, double amount) {
        config.set(getPath(player), getBalance(player) - amount);
        plugin.saveConfig(config, "balances");
    }

    public void set(Player player, double amount) {
        config.set(getPath(player), Math.max(0, getBalance(player) - amount));
        plugin.saveConfig(config, "balances");
    }

    public double getBalance(Player player) {
        return config.getInt(getPath(player), 0);
    }

    private final char[] characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    protected String randomCouponName() {
        StringBuilder couponName = new StringBuilder();

        for(int i = 0; i < 8; i++) {
            couponName.append(characters[ThreadLocalRandom.current().nextInt(characters.length)]);
        }

        return couponName.toString();
    }

    public abstract CompletableFuture<String> createCoupon(Player player, double amount);
    public abstract CompletableFuture<List<SimpleCoupon>> getCoupons(Player player);
}
