package sh.okx.webdeals.enjin.json.delete;


public class DeleteCouponRequest {
  private String jsonrpc = "2.0";
  private int id = 258137;
  private Params params;
  private String method = "Shop.deleteCoupon";

  public DeleteCouponRequest(String api_key, int preset_id, int coupon_id) {
    this.params = new Params(api_key, preset_id, coupon_id);
  }
}

class Params {
  private String api_key;
  private int preset_id;
  private int coupon_id;

  public Params(String api_key, int preset_id, int coupon_id) {
    this.api_key = api_key;
    this.preset_id = preset_id;
    this.coupon_id = coupon_id;
  }
}