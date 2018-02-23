package sh.okx.webdeals.enjin.json.list;

public class GetCouponsRequest {
  private String jsonrpc = "2.0";
  private int id = 59137;
  private Params params;
  private String method = "Shop.getCoupons";

  public GetCouponsRequest(String api_key, int preset_id) {
    this.params = new Params(api_key, preset_id);
  }
}

class Params {
  private String api_key;
  private int preset_id;

  public Params(String api_key, int preset_id) {
    this.api_key = api_key;
    this.preset_id = preset_id;
  }
}