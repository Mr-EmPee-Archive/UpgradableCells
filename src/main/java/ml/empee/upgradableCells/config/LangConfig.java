package ml.empee.upgradableCells.config;

import lombok.Getter;
import ml.empee.ioc.Bean;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


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
  public String translate(String key, Map<String, Object> replacements) {
    var translation = config.getString(key);
    if (translation == null) {
      throw new IllegalArgumentException("Missing translation key " + key);
    }

    if (replacements != null) {
      for (Map.Entry<String, Object> replacement : replacements.entrySet()) {
        translation = translation.replace(replacement.getKey(), replacement.getValue().toString());
      }
    }

    return ChatColor.translateAlternateColorCodes('&', translation);
  }

  public String translate(String key) {
    return translate(key, null);
  }

  public List<String> translateBlock(String key, Map<String, Object> replacements) {
    return Arrays.asList(translate(key, replacements).split("\n"));
  }

  public List<String> translateBlock(String key) {
    return translateBlock(key, null);
  }

}

