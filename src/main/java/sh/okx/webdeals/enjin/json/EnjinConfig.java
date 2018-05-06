package sh.okx.webdeals.enjin.json;

import com.google.gson.annotations.SerializedName;

public class EnjinConfig {
  @SerializedName("auth-key")
  private String authKey;

  public String getAuthKey() {
    return authKey;
  }
}
