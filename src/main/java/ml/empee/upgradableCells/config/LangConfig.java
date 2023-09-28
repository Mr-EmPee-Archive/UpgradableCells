package ml.empee.upgradableCells.config;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;


/**
 * Handle messages
 */

@Singleton
public class LangConfig extends AbstractConfig {

  @Instance @Getter
  private static LangConfig instance;

  public LangConfig(JavaPlugin plugin) {
    super(plugin, "messages.yml", 1);
  }

  @Override
  protected void update(int from) {
  }

  /**
   * Translate a key to a message
   */
  public String translate(String key, Object... placeholders) {
    var translation = config.getString(key);
    if (translation == null) {
      throw new IllegalArgumentException("Missing translation key " + key);
    }

    return String.format(ChatColor.translateAlternateColorCodes('&', translation), placeholders);
  }

  public List<String> translateBlock(String key, Object... placeholders) {
    return List.of(translate(key, placeholders).split("\n"));
  }

}

