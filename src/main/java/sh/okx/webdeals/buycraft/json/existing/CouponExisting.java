package sh.okx.webdeals.buycraft.json.existing;

public class CouponExisting {
    private int id;
    private String code;
    private Effective effective;
    private Discount discount;
    private Expire expire;
    private String basket_type;
    private String start_date;
    private int user_limit;
    private int minimum;
    private String note;

    private CouponExisting() {

    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Discount getDiscount() {
        return discount;
    }

    public Expire getExpire() {
        return expire;
    }

    public String getNote() {
        return note;
    }
}
