package sh.okx.webdeals.enjin.json.list;

public class GetCouponsResponse {
  private CouponExisting[] result;
  private int id;
  private String jsonrpc;

  public CouponExisting[] getResult() {
    return result;
  }
}
