package sh.okx.webdeals.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerUtil {
  @SuppressWarnings("deprecation")
  public static OfflinePlayer getPlayer(String match) {
    Player online = Bukkit.getPlayer(match);
    if(online != null) {
      return online;
    }

    return Bukkit.getOfflinePlayer(match);
  }
}
