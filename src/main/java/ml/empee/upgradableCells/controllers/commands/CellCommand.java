package ml.empee.upgradableCells.controllers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.ManageCellMenu;
import ml.empee.upgradableCells.controllers.views.SelectCellMenu;
import ml.empee.upgradableCells.model.Member;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * Controller use to manage cell operations
 */

@Singleton
@CommandAlias("cell")
@RequiredArgsConstructor
public class CellCommand extends BaseCommand {

  private final CellService cellService;
  private final CellController cellController;
  private final LangConfig langConfig;

  /**
   * Open the cell management menu
   */
  @Default
  public void openCell(Player sender) {
    var cells = cellService.findCellsByMember(sender.getUniqueId());

    if (cells.isEmpty()) {
      ClaimCellMenu.open(sender);
      return;
    }

    if (cells.size() == 1) {
      ManageCellMenu.open(sender, cells.get(0).getId());
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> ManageCellMenu.open(sender, c));
    }
  }

  @Subcommand("join")
  public void joinCell(Player sender) {
    var invites = cellService.getInvitations(sender.getUniqueId());
    if (invites.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.invitation.empty"));
      return;
    }

    if (invites.size() == 1) {
      cellController.joinCell(invites.get(0).getId(), sender);
    } else {
      SelectCellMenu.selectCell(sender, invites).thenAccept(
          c -> cellController.joinCell(c, sender)
      );
    }
  }

  /**
   * Invite a player to a specific cell
   */
  @Subcommand("invite")
  public void inviteToCell(Player sender, Player target) {
    var cells = cellService.findCellsByMember(sender.getUniqueId()).stream()
        .filter(c -> c.getMember(sender.getUniqueId()).orElseThrow().getRank().hasPermission(Member.Permissions.INVITE))
        .collect(Collectors.toList());

    if (cells.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cells.size() == 1) {
      cellController.invitePlayer(cells.get(0).getId(), sender, target);
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> cellController.invitePlayer(c, sender, target));
    }
  }

  /**
   * Leave a cell
   */
  @Subcommand("leave")
  public void leaveCell(Player sender) {
    var cells = cellService.findCellsByMember(sender.getUniqueId()).stream()
      .filter(c -> !sender.getUniqueId().equals(c.getOwner().orElse(null)))
      .collect(Collectors.toList());

    if (cells.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cells.size() == 1) {
      cellController.leaveCell(cells.get(0).getId(), sender);
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> cellController.leaveCell(c, sender)
      );
    }
  }

  @Subcommand("description")
  public void setCellDescription(Player sender, String description) {
    var cell = cellService.findCellByOwner(sender.getUniqueId());

    if (cell.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cell.size() == 1) {
      cellController.setCellDescription(cell.get(0).getId(), sender, description);
    } else {
      SelectCellMenu.selectCell(sender, cell).thenAccept(
          c -> cellController.setCellDescription(c, sender, description)
      );
    }
  }

  @Subcommand("visit")
  public void visitCell(Player sender, OfflinePlayer target) {
    var cell = cellService.findCellByOwner(target.getUniqueId());

    if (cell.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-existing"));
      return;
    }

    if (cell.size() == 1) {
      cellController.teleportToCell(cell.get(0).getId(), sender);
    } else {
      SelectCellMenu.selectCell(sender, cell).thenAccept(
          c -> cellController.teleportToCell(c, sender)
      );
    }
  }

  @Subcommand("name")
  public void setCellName(Player sender, String name) {
    var cell = cellService.findCellByOwner(sender.getUniqueId());

    if (cell.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cell.size() == 1) {
      cellController.setCellName(cell.get(0).getId(), sender, name);
    } else {
      SelectCellMenu.selectCell(sender, cell).thenAccept(
          c -> cellController.setCellName(c, sender, name)
      );
    }
  }

  @Subcommand("delete")
  @CommandPermission(Permissions.ADMIN)
  public void deleteCell(Player sender, OfflinePlayer target) {
    var cell = cellService.findCellByOwner(target.getUniqueId());

    if (cell.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-existing"));
      return;
    }

    if (cell.size() == 1) {
      cellController.makeCellUnacessable(cell.get(0).getId(), sender);
    } else {
      SelectCellMenu.selectCell(sender, cell).thenAccept(
          c -> cellController.makeCellUnacessable(c, sender)
      );
    }
  }
}
