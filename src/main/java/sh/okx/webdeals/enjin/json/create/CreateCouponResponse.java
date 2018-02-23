package sh.okx.webdeals.enjin.json.create;

public class CreateCouponResponse {
  private String id;
  private String jsonrpc;
  private Result result;

  public String getCode() {
    return result.getCouponCode();
  }
}

class Result {
  private String coupon_id;
  private String[] coupon_codes;

  public String getCouponCode() {
    assert coupon_codes.length == 1;
    return coupon_codes[0];
  }
}