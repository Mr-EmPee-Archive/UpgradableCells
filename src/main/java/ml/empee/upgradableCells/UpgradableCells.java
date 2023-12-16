package ml.empee.upgradableCells;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import lombok.val;
import ml.empee.simplemenu.SimpleMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.Lightwire;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boot class of this plugin.
 **/

public final class UpgradableCells extends JavaPlugin {

  private final Lightwire iocContainer = new Lightwire();
  private final SimpleMenu simpleMenu = new SimpleMenu();
  private BukkitCommandManager commandManager;


  /**
   * Called when enabling the plugin
   */
  public void onEnable() {
    simpleMenu.init(this);
    commandManager = new BukkitCommandManager(this);

    iocContainer.addBean(this);
    iocContainer.addBean(getEconomyProvider());

    iocContainer.loadBeans(getClass().getPackage());

    val langConfig = iocContainer.getBean(LangConfig.class);
    Logger.setPrefix(langConfig.translate("prefix"));

    iocContainer.getAllBeans(Listener.class).forEach(
      l -> getServer().getPluginManager().registerEvents(l, this)
    );

    iocContainer.getAllBeans(BaseCommand.class).forEach(
      c -> commandManager.registerCommand(c)
    );
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
