package ml.empee.upgradableCells;

import lombok.Getter;
import ml.empee.ioc.SimpleIoC;
import ml.empee.upgradableCells.config.DatabaseConfiguration;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boot class of this plugin.
 **/

public final class UpgradableCells extends JavaPlugin {

  @Getter
  private final SimpleIoC iocContainer = new SimpleIoC(this);

  private DatabaseConfiguration databaseConfiguration;

  /**
   * Called when enabling the plugin
   */
  public void onEnable() {
    Logger.setDebugEnabled(true);

    LangConfig langConfig = new LangConfig(this);
    Logger.setPrefix(langConfig.translate("prefix"));

    databaseConfiguration = new DatabaseConfiguration(this);

    iocContainer.addBean(databaseConfiguration);
    iocContainer.addBean(langConfig);
    iocContainer.initialize("relocations");
  }

  public void onDisable() {
    //TODO: execute pending tasks

    databaseConfiguration.closeConnection();
    iocContainer.removeAllBeans(true);
  }
}
