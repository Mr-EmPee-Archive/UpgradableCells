package ml.empee.upgradableCells;

import ml.empee.simplemenu.SimpleMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.Lightwire;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boot class of this plugin.
 **/

public final class UpgradableCells extends JavaPlugin {

  private final Lightwire iocContainer = new Lightwire();
  private final SimpleMenu simpleMenu = new SimpleMenu();

  /**
   * Called when enabling the plugin
   */
  public void onEnable() {
    Logger.setDebugEnabled(true);

    LangConfig langConfig = new LangConfig(this);
    Logger.setPrefix(langConfig.translate("prefix"));

    simpleMenu.init(this);

    iocContainer.addBean(this);
    iocContainer.addBean(getEconomyProvider());
    iocContainer.addBean(langConfig);

    iocContainer.loadBeans(getClass().getPackage());
  }

  private Economy getEconomyProvider() {
    var provider = getServer().getServicesManager().getRegistration(Economy.class);
    if (provider == null) {
      throw new IllegalStateException("Economy provider not found! Load an economy plugin!");
    }

    return provider.getProvider();
  }

  public void onDisable() {
    simpleMenu.unregister(this);
    iocContainer.getBean(DbClient.class).closeConnections();
  }
}
