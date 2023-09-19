package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import ml.empee.upgradableCells.api.CellAPI;
import ml.empee.upgradableCells.config.CommandsConfig;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.ManageCellMenu;
import ml.empee.upgradableCells.controllers.views.SelectCellMenu;
import ml.empee.upgradableCells.controllers.views.TopCellsMenu;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import net.milkbowl.vault.economy.Economy;

import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Controller use to manage cell operations
 */

@Singleton
public class CellController {

  private final CellService cellService;
  private final CellAPI cellAPI;
  private final LangConfig langConfig;

  public CellController(
      CellAPI cellAPI, CommandsConfig commandsConfig,
      CellService cellService, LangConfig langConfig
  ) {
    this.cellAPI = cellAPI;
    this.cellService = cellService;
    this.langConfig = langConfig;

    commandsConfig.register(this);
  }

  @CommandMethod("claim")
  public void claimCell(Player sender) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      ClaimCellMenu.open(sender);
    } else {
      Logger.log(sender, langConfig.translate("cell.already-bought"));
    }
  }

  /**
   * Open the cell management menu
   */
  @CommandMethod("cell")
  public void openCell(Player sender) {
    var cells = cellService.findCellsByMember(sender.getUniqueId());

    if (cells.isEmpty()) {
      ClaimCellMenu.open(sender);
      return;
    }

    if (cells.size() == 1) {
      ManageCellMenu.open(sender, cells.get(0));
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> ManageCellMenu.open(sender, c)
      );
    }
  }

  /**
   * Cell-Top
   */
  @CommandMethod("cell-top")
  public void openCellTopMenu(Player sender) {
    TopCellsMenu.open(sender);
  }

  /**
   * Teleport a player to his cell
   */
  @CommandMethod("home")
  public void teleportToCell(Player sender) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    cellAPI.teleportToCell(sender, cell);
  }

  @CommandMethod("cell join <target>")
  public void joinCell(Player sender, @Argument OfflinePlayer target) {
    OwnedCell cell = cellService.findCellByOwner(target.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-existing"));
      return;
    }

    cellAPI.joinCell(sender, cell);
  }

  /**
   * Invite a player to a specific cell
   */
  @CommandMethod("cell invite <target>")
  public void inviteToCell(Player sender, @Argument Player target) {
    var cells = cellService.findCellsByMember(sender.getUniqueId()).stream()
        .filter(c -> c.getMember(sender.getUniqueId()).getRank().canInvite())
        .collect(Collectors.toList());


    if (cells.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cells.size() == 1) {
      cellAPI.invitePlayer(cells.get(0), sender, target);
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> cellAPI.invitePlayer(c, sender, target)
      );
    }
  }

  /**
   * Leave a cell
   */
  @CommandMethod("cell leave")
  public void leaveCell(Player sender) {
    var cells = cellService.findCellsByMember(sender.getUniqueId());

    if (cells.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cells.size() == 1) {
      cellAPI.leaveCell(sender, cells.get(0));
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> cellAPI.leaveCell(sender, c)
      );
    }
  }

  @CommandMethod("cell name <name>")
  public void setCellName(Player sender, @Argument @Greedy String name) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    cellAPI.setCellName(sender, cell, name);
  }

  @CommandMethod("cell description <description>")
  public void setCellDescription(Player sender, @Argument @Greedy String description) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    cellAPI.setCellDescription(sender, cell, description);
  }

  @CommandMethod("cell visit <target>")
  public void visitCell(Player sender, @Argument OfflinePlayer target) {
    OwnedCell cell = cellService.findCellByOwner(target.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-existing"));
      return;
    }

    cellAPI.teleportToCell(sender, cell);
  }
}
