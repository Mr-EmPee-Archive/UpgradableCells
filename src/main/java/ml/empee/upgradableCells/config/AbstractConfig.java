package ml.empee.upgradableCells.config;

import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic class to cache and update a configuration file
 */

public abstract class AbstractConfig {

  protected final File file;
  protected final int version;
  protected YamlConfiguration config;

  public AbstractConfig(JavaPlugin plugin, String resource, int version) {
    file = new File(plugin.getDataFolder(), resource);
    if (!file.exists()) {
      plugin.saveResource(resource, true);
    }

    this.version = version;
    this.config = loadConfig(file);
  }

  private YamlConfiguration loadConfig(File file) {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    update(config);
    return config;
  }

  @SneakyThrows
  private void update(YamlConfiguration config) {
    int currentVersion = config.getInt("version", 1);
    if (currentVersion == version) {
      return;
    } else if (currentVersion > version) {
      throw new IllegalArgumentException("The config file has been generated from a newer version!");
    }

    update(currentVersion);

    config.save(file);
  }

  protected abstract void update(int from);

  @NotNull
  protected List<ConfigurationSection> getSections(String path) {
    ConfigurationSection section = config.getConfigurationSection(path);
    if (section == null) {
      return Collections.emptyList();
    }

    List<ConfigurationSection> sections = new ArrayList<>();
    for (String id : section.getKeys(false)) {
      sections.add(section.getConfigurationSection(id));
    }

    return sections;
  }

  public void reload() {
    config = loadConfig(file);
  }

}
