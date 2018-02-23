package sh.okx.webdeals.enjin.json.create;

public class Coupon {
  private String api_key;
  private String preset_id;
  private double discount_amount;
  private String discount_type = "value";
  private String expiry_type = "redeem_limit";
  private int expiry_value = 1;
  private String[] restrict_to_mc_players;

  public Coupon(String api_key, String preset_id, double discount_amount, String player) {
    this.api_key = api_key;
    this.preset_id = preset_id;
    this.discount_amount = discount_amount;
    this.restrict_to_mc_players = new String[] { player };
  }
}
