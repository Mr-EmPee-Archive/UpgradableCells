package ml.empee.upgradableCells.handlers;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handle actions inside a cell
 */

@RequiredArgsConstructor
public class CellProtectionHandler implements Bean, RegisteredListener {

  private final CellService cellService;
  private final LangConfig langConfig;
  private final PluginConfig pluginConfig;

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (canBuild(event.getPlayer(), event.getBlock().getLocation())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-build"));
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    if (canBuild(event.getPlayer(), event.getBlock().getLocation())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-build"));
  }

  private boolean canBuild(Player player, Location target) {
    if (player.hasPermission(Permissions.ADMIN)) {
      return true;
    }

    var cell = cellService.findCellByLocation(target).orElse(null);
    if (cell == null) {
      return true;
    }

    Member member = cell.getMember(player.getUniqueId());
    if (member == null || !member.getRank().canBuild()) {
      return false;
    }

    var project = cellService.getCellProject(cell.getLevel());
    return project.canBuild(cell, target);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();
    if (player.hasPermission(Permissions.ADMIN)) {
      return;
    }

    var clickedBlock = event.getClickedBlock();
    var cell = cellService.findCellByLocation(clickedBlock.getLocation()).orElse(null);
    if (cell == null) {
      return;
    }

    Member member = cell.getMember(player.getUniqueId());
    if (member != null && (clickedBlock.getType() != Material.CHEST || member.getRank().canAccessChests())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-interact"));
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
