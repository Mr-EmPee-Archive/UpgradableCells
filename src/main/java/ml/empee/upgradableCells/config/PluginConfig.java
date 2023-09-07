package ml.empee.upgradableCells.config;

import ml.empee.upgradableCells.model.entities.CellProject;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin config file
 */

@Singleton
public class PluginConfig extends AbstractConfig {

  private Location spawnLocation;

  public PluginConfig(JavaPlugin plugin) {
    super(plugin, "config.yml", 1);
  }

  @Override
  protected void update(int from) {

  }

  @Override
  public void reload() {
    spawnLocation = null;
    super.reload();
  }

  /**
   * @return the spawn location of the server
   */
  public Location getSpawnLocation() {
    if (spawnLocation == null) {
      spawnLocation = new Location(
          Bukkit.getWorld(config.getString("spawn-point.world", "world")),
          config.getDouble("spawn-point.x", 0),
          config.getDouble("spawn-point.y", 0),
          config.getDouble("spawn-point.z", 0),
          (float) config.getDouble("spawn-point.yaw", 0),
          (float) config.getDouble("spawn-point.pitch", 0)
      );
    }
    return spawnLocation;
  }

  public String getCellWorld() {
    return config.getString("world.name", "cell-world");
  }

  public List<CellProject> getCellProjects() {
    int level = 0;

    CellProject prevProject = null;
    List<CellProject> projects = new ArrayList<>();
    for (ConfigurationSection project : getSections("cells.upgrades")) {
      prevProject = CellProject.fromConfig(prevProject, level++, project);
      projects.add(prevProject);
    }

    return projects;
  }

  public int getCellSize() {
    return config.getInt("world.cell-size", 500);
  }

}
