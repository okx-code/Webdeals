package sh.okx.webdeals.enjin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import sh.okx.webdeals.Webdeals;
import sh.okx.webdeals.api.CouponError;
import sh.okx.webdeals.api.SimpleCoupon;
import sh.okx.webdeals.api.WebdealManager;
import sh.okx.webdeals.enjin.json.EnjinConfig;
import sh.okx.webdeals.enjin.json.create.Coupon;
import sh.okx.webdeals.enjin.json.create.CreateCouponRequest;
import sh.okx.webdeals.enjin.json.create.CreateCouponResponse;
import sh.okx.webdeals.enjin.json.delete.DeleteCouponRequest;
import sh.okx.webdeals.enjin.json.list.CouponExisting;
import sh.okx.webdeals.enjin.json.list.GetCouponsRequest;
import sh.okx.webdeals.enjin.json.list.GetCouponsResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EnjinWebdealManager extends WebdealManager {
    public EnjinWebdealManager(Webdeals plugin) {
        super(plugin);
    }

    @Override
    protected String getSecret() {
        Plugin enjin = Bukkit.getPluginManager().getPlugin("EnjinMinecraftPlugin");
        String secret = null;
        try {
            InputStream config =
                new FileInputStream(new File(enjin.getDataFolder(), "config.json"));

            EnjinConfig json = plugin.getGson()
                .fromJson(new InputStreamReader(config), EnjinConfig.class);
            secret = json.getAuthKey();

            config.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return secret;
    }


    @Override
    public CompletableFuture<String> createCoupon(Player player, double amount) {
        Coupon coupon = new Coupon(secret,
            plugin.getConfig().getString("enjin.preset_id"),
            amount,
            player.getUniqueId().toString());
        String json = plugin.getGson().toJson(new CreateCouponRequest(coupon));

        return CompletableFuture.supplyAsync(() -> {
            try {
                return postCreateCoupon(json).getCode();
            } catch (IOException | CouponError ex) {
                ex.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<SimpleCoupon>> getCoupons(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<SimpleCoupon> coupons = new ArrayList<>();
                for(CouponExisting existing : getCoupons().getResult()) {
                    if(!existing.isAppliesTo(player.getUniqueId().toString())) {
                        continue;
                    }

                    if(!existing.isValid()) {
                        if(existing.isExpired()) {
                            deleteCoupon(existing.getCouponId());
                        }
                        continue;
                    }

                    coupons.add(new SimpleCoupon(
                        existing.getCode(),
                        player.getUniqueId(),
                        existing.getDiscountAmount()));
                }

                return coupons;
            } catch (IOException | CouponError ex) {
                ex.printStackTrace();
                return null;
            }
        });
    }

    private CreateCouponResponse postCreateCoupon(String json) throws IOException, CouponError {
        URL url = new URL(plugin.getConfig().getString("enjin.domain") + "/api/v1/api.php");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        http.setFixedLengthStreamingMode(json.length());
        http.setRequestProperty("Content-Type", "application/json");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(json.getBytes());
        }
        JsonObject o = new JsonParser().parse(toString(http.getInputStream())).getAsJsonObject();
        if (o.has("error")) {
            throw new CouponError(o.getAsJsonObject("error").get("message").getAsString());
        }

        return plugin.getGson().fromJson(o, CreateCouponResponse.class);
    }

    private void deleteCoupon(int id) throws IOException {
        URL url = new URL(plugin.getConfig().getString("enjin.domain") + "/api/v1/api.php");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        String json = plugin.getGson().toJson(new DeleteCouponRequest(
            secret,
            plugin.getConfig().getInt("enjin.preset_id"),
            id));

        http.setFixedLengthStreamingMode(json.length());
        http.setRequestProperty("Content-Type", "application/json");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(json.getBytes());
        }
        JsonObject o = new JsonParser().parse(toString(http.getInputStream())).getAsJsonObject();
    }

    private GetCouponsResponse getCoupons() throws CouponError, IOException {
        URL url = new URL(plugin.getConfig().getString("enjin.domain") + "/api/v1/api.php");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        String json = plugin.getGson().toJson(new GetCouponsRequest(
            secret,
            plugin.getConfig().getInt("enjin.preset_id")));

        http.setFixedLengthStreamingMode(json.length());
        http.setRequestProperty("Content-Type", "application/json");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(json.getBytes());
        }
        JsonObject o = new JsonParser().parse(toString(http.getInputStream())).getAsJsonObject();
        if (o.has("error")) {
            throw new CouponError(o.getAsJsonObject("error").get("message").getAsString());
        }

        return plugin.getGson().fromJson(o, GetCouponsResponse.class);
    }
}
