package sh.okx.webdeals.buycraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import sh.okx.webdeals.Webdeals;
import sh.okx.webdeals.api.CouponCreationError;
import sh.okx.webdeals.api.SimpleCoupon;
import sh.okx.webdeals.api.WebdealManager;
import sh.okx.webdeals.buycraft.json.Coupon;
import sh.okx.webdeals.buycraft.json.existing.CouponExisting;
import sh.okx.webdeals.buycraft.json.existing.CouponList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BuycraftWebdealManager extends WebdealManager {
    public BuycraftWebdealManager(Webdeals plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<String> createCoupon(Player player, double amount) {
        Coupon coupon = new Coupon(randomCouponName(), amount, "Webdeal|" + player.getUniqueId().toString());
        String json = plugin.getGson().toJson(coupon);

        return CompletableFuture.supplyAsync(() -> {
            try {
                postCreateCoupon(json);
            } catch (IOException | CouponCreationError ex) {
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
            } catch (IOException | CouponCreationError ex) {
                ex.printStackTrace();
                return null;
            }
            return coupons;
        });
    }

    private void postCreateCoupon(String json) throws CouponCreationError, IOException {
        URL url = new URL("https://plugin.buycraft.net/coupons");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        http.setFixedLengthStreamingMode(json.length());
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-Buycraft-Secret", plugin.getConfig().getString("secret"));
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(json.getBytes());
        }
        JsonObject o = new JsonParser().parse(toString(http.getInputStream())).getAsJsonObject();
        if (o.has("error_code")) {
            throw new CouponCreationError(o.get("error_message").getAsString());
        }
    }

    private void deleteCoupon(int id) throws IOException {
        URL url = new URL("https://plugin.buycraft.net/coupons/" + id);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("DELETE");

        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-Buycraft-Secret", plugin.getConfig().getString("secret"));
        http.connect();

        String output = toString(http.getInputStream());

        if(!output.isEmpty()) {
            throw new IOException("Could not delete coupon: " + output);
        }
    }

    private CouponList getListCoupons() throws CouponCreationError, IOException {
        URL url = new URL("https://plugin.buycraft.net/coupons");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("GET");

        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-Buycraft-Secret", plugin.getConfig().getString("secret"));
        http.connect();

        String input = toString(http.getInputStream());

        JsonObject o = new JsonParser().parse(input).getAsJsonObject();
        if (o.has("error_code")) {
            throw new CouponCreationError(o.get("error_message").getAsString());
        }

        return plugin.getGson().fromJson(input, CouponList.class);
    }

    private String toString(InputStream inputStream) throws IOException {
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