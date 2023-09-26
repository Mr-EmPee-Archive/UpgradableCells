package ml.empee.upgradableCells.handlers;

import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Handle actions inside a cell
 */

@Singleton
public class CellProtectionHandler implements Listener {

  private final CellService cellService;
  private final LangConfig langConfig;

  public CellProtectionHandler(
      UpgradableCells plugin, CellService cellService, LangConfig langConfig) {
    this.cellService = cellService;
    this.langConfig = langConfig;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (cellService.canBuild(event.getPlayer(), event.getBlock().getLocation())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-build"));
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    if (cellService.canBuild(event.getPlayer(), event.getBlock().getLocation())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-build"));
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (cellService.canInteract(event.getPlayer(), event.getClickedBlock().getLocation(), null)) {
      return;
    }

    event.setCancelled(true);
    if (event.getHand() != EquipmentSlot.OFF_HAND) {
      Logger.log(event.getPlayer(), langConfig.translate("cell.cant-interact"));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onBucketFill(PlayerBucketFillEvent event) {
    if (cellService.canBuild(event.getPlayer(), event.getBlockClicked().getLocation())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-build"));
  }

  @EventHandler(ignoreCancelled = true)
  public void onBucketEmpty(PlayerBucketEmptyEvent event) {
    if (cellService.canBuild(event.getPlayer(), event.getBlockClicked().getLocation())) {
      return;
    }

    event.setCancelled(true);
    Logger.log(event.getPlayer(), langConfig.translate("cell.cant-build"));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    event.blockList().removeIf(b -> cellService.isCellBlock(b.getLocation()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent event) {
    event.blockList().removeIf(b -> cellService.isCellBlock(b.getLocation()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onPistonExtend(BlockPistonExtendEvent event) {
    boolean movingSchemBlock = event.getBlocks().stream().anyMatch(
        b -> cellService.isCellBlock(b.getLocation()));

    if (movingSchemBlock) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPistonRetract(BlockPistonRetractEvent event) {
    boolean movingSchemBlock = event.getBlocks().stream().anyMatch(
        b -> cellService.isCellBlock(b.getLocation()));

    if (movingSchemBlock) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (cellService.isCellBlock(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockBurn(BlockBurnEvent event) {
    if (cellService.isCellBlock(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

}
