package sh.okx.webdeals.buycraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import sh.okx.webdeals.Webdeals;
import sh.okx.webdeals.api.CouponError;
import sh.okx.webdeals.api.SimpleCoupon;
import sh.okx.webdeals.api.WebdealManager;
import sh.okx.webdeals.buycraft.json.Coupon;
import sh.okx.webdeals.buycraft.json.existing.CouponExisting;
import sh.okx.webdeals.buycraft.json.existing.CouponList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class BuycraftWebdealManager extends WebdealManager {
    public BuycraftWebdealManager(Webdeals plugin) {
        super(plugin);
    }

    @Override
    protected String getSecret() {
        Plugin buycraft = Bukkit.getPluginManager().getPlugin("BuycraftX");
        String secret = null;
        try {
            InputStream config =
                new FileInputStream(new File(buycraft.getDataFolder(), "config.properties"));
            Properties properties = new Properties();
            properties.load(config);

            secret = properties.getProperty("server-key");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return secret;
    }

    @Override
    public CompletableFuture<String> createCoupon(Player player, double amount) {
        Coupon coupon = new Coupon(randomCouponName(), amount, "Webdeal|" + player.getUniqueId().toString());
        String json = plugin.getGson().toJson(coupon);

        return CompletableFuture.supplyAsync(() -> {
            try {
                postCreateCoupon(json);
            } catch (IOException | CouponError ex) {
                ex.printStackTrace();
                return null;
            }
            return coupon.getCode();
        });
    }

    @Override
    public CompletableFuture<List<SimpleCoupon>> getCoupons(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            List<SimpleCoupon> coupons = new ArrayList<>();
            try {
                for(CouponExisting coupon : getListCoupons().getData()) {
                    String[] parts = coupon.getNote().split("\\|");
                    if(parts.length != 2 || !parts[0].equals("Webdeal")) {
                        continue;
                    }

                    if(coupon.getExpire().getLimit() < 1) {
                        CompletableFuture.runAsync(() -> {
                            try {
                                deleteCoupon(coupon.getId());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                        continue;
                    }

                    UUID uuid = UUID.fromString(parts[1]);
                    if(!uuid.equals(player.getUniqueId())) {
                        continue;
                    }

                    coupons.add(new SimpleCoupon(coupon.getCode(),
                            uuid,
                            coupon.getDiscount().getValue()));
                }
            } catch (IOException | CouponError ex) {
                ex.printStackTrace();
                return null;
            }
            return coupons;
        });
    }

    private void postCreateCoupon(String json) throws CouponError, IOException {
        URL url = new URL("https://plugin.buycraft.net/coupons");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        http.setFixedLengthStreamingMode(json.length());
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-Buycraft-Secret", secret);
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(json.getBytes());
        }
        JsonObject o = new JsonParser().parse(toString(http.getInputStream())).getAsJsonObject();
        if (o.has("error_code")) {
            throw new CouponError(o.get("error_message").getAsString());
        }
    }

    private void deleteCoupon(int id) throws IOException {
        URL url = new URL("https://plugin.buycraft.net/coupons/" + id);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("DELETE");

        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-Buycraft-Secret", secret);
        http.connect();

        String output = toString(http.getInputStream());

        if(!output.isEmpty()) {
            throw new IOException("Could not delete coupon: " + output);
        }
    }

    private CouponList getListCoupons() throws CouponError, IOException {
        URL url = new URL("https://plugin.buycraft.net/coupons");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("GET");

        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-Buycraft-Secret", secret);
        http.connect();

        String input = toString(http.getInputStream());

        JsonObject o = new JsonParser().parse(input).getAsJsonObject();
        if (o.has("error_code")) {
            throw new CouponError(o.get("error_message").getAsString());
        }

        return plugin.getGson().fromJson(input, CouponList.class);
    }

    private final char[] characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    protected String randomCouponName() {
        StringBuilder couponName = new StringBuilder();

        for(int i = 0; i < 8; i++) {
            couponName.append(characters[ThreadLocalRandom.current().nextInt(characters.length)]);
        }

        return couponName.toString();
    }
}