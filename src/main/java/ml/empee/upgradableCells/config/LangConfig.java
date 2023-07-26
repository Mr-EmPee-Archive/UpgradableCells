package ml.empee.upgradableCells.config;

import lombok.Getter;
import ml.empee.ioc.Bean;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


/**
 * Handle messages
 */

public class LangConfig extends AbstractConfig implements Bean {

  @Getter
  private static LangConfig instance;

  public LangConfig(JavaPlugin plugin) {
    super(plugin, "messages.yml", 1);
  }

  @Override
  public void onStart() {
    instance = this;
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

    return ChatColor.translateAlternateColorCodes('&', translation).formatted(placeholders);
  }

  public List<String> translateBlock(String key, Object... placeholders) {
    return List.of(translate(key, placeholders).split("\n"));
  }

}

