package sh.okx.webdeals.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import sh.okx.webdeals.Webdeals;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class WebdealManager {
    protected String secret;
    protected Webdeals plugin;
    private FileConfiguration config;
    private DecimalFormat df;

    public FileConfiguration getConfig() {
        return config;
    }

    public WebdealManager(Webdeals plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig("balances");
        this.secret = getSecret();

        reload();
    }

    protected abstract String getSecret();

    public void reload() {
        df = new DecimalFormat(plugin.getMessage("money_format"));
        secret = getSecret();
    }

    public String format(double v) {
        return df.format(v);
    }

    private String getPath(UUID uuid) {
        return "balances." + uuid;
    }

    public void add(Player player, double amount) {
        config.set(getPath(player.getUniqueId()), getBalance(player) + amount);
        plugin.saveConfig(config, "balances");
    }

    public void take(Player player, double amount) {
        config.set(getPath(player.getUniqueId()), getBalance(player) - amount);
        plugin.saveConfig(config, "balances");
    }

    public void set(Player player, double amount) {
        config.set(getPath(player.getUniqueId()), Math.max(0, amount));
        plugin.saveConfig(config, "balances");
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public double getBalance(UUID uuid) {
        return config.getDouble(getPath(uuid), 0);
    }

    public abstract CompletableFuture<String> createCoupon(Player player, double amount);
    public abstract CompletableFuture<List<SimpleCoupon>> getCoupons(Player player);

    protected String toString(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            buf.write((byte) result);
            result = bis.read();
        }

        return buf.toString("UTF-8");
    }
}
