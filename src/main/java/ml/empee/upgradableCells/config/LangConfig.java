package ml.empee.upgradableCells.config;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handle messages
 */

public class LangConfig extends AbstractConfig {

  public LangConfig(JavaPlugin plugin) {
    super(plugin, "messages.yml", 1);
  }

  @Override
  protected void update(int from) {}

  public String translate(String key) {
    var translation = config.getString(key);
    if (translation == null) {
      throw new IllegalArgumentException("Missing translation key " + key);
    }

    return ChatColor.translateAlternateColorCodes('&', translation);
  }

}
