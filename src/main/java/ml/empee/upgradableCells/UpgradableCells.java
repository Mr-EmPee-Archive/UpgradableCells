package ml.empee.upgradableCells;

import lombok.Getter;
import ml.empee.ioc.SimpleIoC;
import ml.empee.simplemenu.SimpleMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.utils.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boot class of this plugin.
 **/

public final class UpgradableCells extends JavaPlugin {

  @Getter
  private final SimpleIoC iocContainer = new SimpleIoC(this);
  private final SimpleMenu simpleMenu = new SimpleMenu();

  /**
   * Called when enabling the plugin
   */
  public void onEnable() {
    Logger.setDebugEnabled(true);

    LangConfig langConfig = new LangConfig(this);
    Logger.setPrefix(langConfig.translate("prefix"));

    simpleMenu.init(this);

    iocContainer.addBean(getEconomyProvider());
    iocContainer.addBean(langConfig);
    iocContainer.initialize("relocations");
  }

  private Economy getEconomyProvider() {
    return getServer().getServicesManager().getRegistration(Economy.class).getProvider();
  }

  public void onDisable() {
    simpleMenu.unregister(this);
    iocContainer.getBean(DbClient.class).closeConnections();

    iocContainer.removeAllBeans(true);
  }
}
