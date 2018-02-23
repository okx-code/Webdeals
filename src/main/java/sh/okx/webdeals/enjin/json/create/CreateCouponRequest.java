package sh.okx.webdeals.enjin.json.create;

public class CreateCouponRequest {
  private String jsonrpc = "2.0";
  private int id = 34954;
  private Coupon params;
  private String method = "Shop.createCoupon";

  public CreateCouponRequest(Coupon coupon) {
    this.params = coupon;
  }
}
