package sh.okx.webdeals.buycraft.json;

public class Coupon {
    private String code;
    private String effective_on = "cart";
    private String discount_type = "value";
    private double discount_amount;
    private int discount_percentage = 0;
    private String expire_type = "limit";
    private int expire_limit = 1;
    private String basket_type = "both";
    private int minimum = 0;
    private int redeem_limit = 1;
    private String note = "Automatic coupon";

    public Coupon(String code, double discount_amount, String note) {
        this.code = code;
        this.discount_amount = discount_amount;
        this.note = note;
    }

    public String getCode() {
        return code;
    }
}
