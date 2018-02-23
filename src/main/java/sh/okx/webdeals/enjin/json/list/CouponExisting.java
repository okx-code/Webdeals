package sh.okx.webdeals.enjin.json.list;

public class CouponExisting {
  /*
              "coupon_id": "134198",
            "preset_id": "41958034",
            "discount_amount": "40.00",
            "discount_type": "percent",
            "start_date": "1512432000",
            "end_date": "0",
            "max_uses": "0",
            "effective_type": "cart",
            "expire_type": "Never Expires",
            "expire_limit": "0",
            "min_cart": "0.00",
            "apply_to_total": "0",
            "apply_to_options": "0",
            "apply_to_points": "0",
            "nr_codes": "1",
            "mc_allow_only_players": "CritZone,Lmafo,Jewsound,Cxllum_15,Jordy1999,Trisprite",
            "discount_quantity_limit": "0",
            "redeem_limit_accumulative": "0",
            "coupon_code": "MSSTAFF#90453",
            "times_used": "22",
            "effective_on": [],
            "description": "40% off entire cart",
            "expired": false
   */
  private int coupon_id;
  private String preset_id;
  private double discount_amount;
  private String start_date;
  private String end_date;
  private String max_uses;
  private String effective_type;
  private String expire_type;
  private String expire_limit;
  private String min_cart;
  private String apply_to_total;
  private String apply_to_options;
  private String apply_to_points;
  private String nr_codes;
  private String mc_allow_only_players;
  private String discount_quantity_limit;
  private String redeem_limit_accumulative;
  private String coupon_code;
  private int times_used;
  private String[] effective_on;
  private String description;
  private boolean expired;

  public double getDiscountAmount() {
    return discount_amount;
  }

  public String getCode() {
    return coupon_code;
  }

  public boolean isAppliesTo(String uuid) {
    return mc_allow_only_players.equals(uuid);
  }

  public boolean isValid() {
    return !expired && times_used == 0;
  }

  public boolean isExpired() {
    return expired;
  }

  public int getCouponId() {
    return coupon_id;
  }
}
