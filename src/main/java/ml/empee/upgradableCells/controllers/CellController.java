package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.CommandMethod;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.ManageCellMenu;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Controller use to manage cell operations
 */

@RequiredArgsConstructor
public class CellController implements Bean {

  private final CellService cellService;
  private final Economy economy;
  private final LangConfig langConfig;

  @CommandMethod("cell")
  public void openCell(Player player) {
    OwnedCell cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);
    if (cell == null) {
      ClaimCellMenu.open(player);
    } else {
      ManageCellMenu.open(player, cell);
    }
  }

  /**
   * Teleport a player to his cell
   */
  @CommandMethod("home")
  public void teleportToCell(Player player) {
    var cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(player, langConfig.translate("cell.not-bought"));
      return;
    }

    teleportToCell(player, cell);
  }

  /**
   * Buy a cell
   */
  public void createCell(Player player) {
    createCell(player, player.getUniqueId());
  }

  /**
   * Level-up his cell
   */
  public void upgradeCell(Player player) {
    var cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(player, langConfig.translate("cell.not-bought"));
      return;
    }

    upgradeCell(player, cell);
  }

  /**
   * Create a cell for the targeted uuid
   */
  public void createCell(Player player, UUID target) {
    if (cellService.findCellByOwner(target).isPresent()) {
      Logger.log(player, langConfig.translate("cell.already-bought"));
      return;
    }

    var project = cellService.getCellProject(0);
    if (!economy.has(player, project.getCost())) {
      Logger.log(player, langConfig.translate("economy.missing-money", project.getCost()));
      return;
    }

    economy.withdrawPlayer(player, project.getCost());
    cellService.createCell(player.getUniqueId());
    Logger.log(player, langConfig.translate("cell.bought-cell"));
  }

  /**
   * Upgrade a cell to the next-level
   */
  public void upgradeCell(Player player, OwnedCell cell) {
    if (cellService.getLastProject().getLevel() == cell.getLevel()) {
      Logger.log(player, langConfig.translate("cell.max-level"));
      return;
    }

    if (cell.isPasting()) {
      Logger.log(player, langConfig.translate("cell.still-building"));
      return;
    }

    var project = cellService.getCellProject(cell.getLevel() + 1);
    if (!economy.has(player, project.getCost())) {
      Logger.log(player, langConfig.translate("economy.missing-money", project.getCost()));
      return;
    }

    economy.withdrawPlayer(player, project.getCost());
    cellService.upgradeCell(cell, project.getLevel());
    Logger.log(player, langConfig.translate("cell.bought-upgrade"));
  }

  /**
   * Teleport a player to a cell
   */
  public void teleportToCell(Player player, OwnedCell cell) {
    if (cell.getLevel() == 0 && cell.isPasting()) {
      Logger.log(player, langConfig.translate("cell.still-building"));
      return;
    }

    player.teleport(cellService.getSpawnpoint(cell));
  }

}
