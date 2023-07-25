package ml.empee.upgradableCells.config;

import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.model.entities.CellProject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

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

  public List<CellProject> getCellProjects() {
    int level = 0;

    List<CellProject> projects = new ArrayList<>();
    for (ConfigurationSection project : getSections("cells.upgrades")) {
      projects.add(CellProject.fromConfig(level++, project));
    }

    return projects;
  }

  public int getCellSize() {
    return config.getInt("world.cell-size", 500);
  }

}
