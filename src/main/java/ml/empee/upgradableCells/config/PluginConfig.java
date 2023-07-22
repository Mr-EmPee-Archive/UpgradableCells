package ml.empee.upgradableCells.config;

import ml.empee.ioc.Bean;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin config file
 */

public class PluginConfig extends AbstractConfig implements Bean {

  public PluginConfig(JavaPlugin plugin) {
    super(plugin, "config.yml", 1);
  }

  @Override
  protected void update(int from) {

  }

  public String getCellWorld() {
    return config.getString("world.name", "cell-world");
  }

  public int getCellSize() {
    return config.getInt("world.cell-size", 500);
  }

}
