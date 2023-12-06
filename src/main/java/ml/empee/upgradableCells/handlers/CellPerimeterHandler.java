package ml.empee.upgradableCells.handlers;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CellPerimeterHandler implements Listener {

  private final CellService cellService;
  private final PluginConfig pluginConfig;

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    var cell = cellService.findCellByLocation(player.getLocation()).orElse(null);
    if (cell == null) {
      return;
    }

    if (cell.getBannedMember(player.getUniqueId()).isEmpty()) {
      return;
    }

    player.teleport(pluginConfig.getSpawnLocation());
  }

}
