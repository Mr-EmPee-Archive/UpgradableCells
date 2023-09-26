package ml.empee.upgradableCells.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.services.CellService;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Handle player movement and access to a cell
 */

@Singleton
public class CellPerimeterHandler implements Listener {

  private final CellService cellService;
  private final PluginConfig pluginConfig;

  public CellPerimeterHandler(
      UpgradableCells plugin, CellService cellService, PluginConfig pluginConfig
  ) {
    this.cellService = cellService;
    this.pluginConfig = pluginConfig;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    var cell = cellService.findCellByLocation(player.getLocation()).orElse(null);
    if (cell == null) {
      return;
    }

    if (!cell.isBannedMember(player.getUniqueId())) {
      return;
    }

    player.teleport(pluginConfig.getSpawnLocation());
  }

}
