package sh.okx.webdeals.api;

import java.util.UUID;

public class SimpleCoupon {
    private String code;
    private UUID owner;
    private double value;

    public SimpleCoupon(String code, UUID owner, double value) {
        this.code = code;
        this.owner = owner;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public UUID getOwner() {
        return owner;
    }

    public double getValue() {
        return value;
    }
}
