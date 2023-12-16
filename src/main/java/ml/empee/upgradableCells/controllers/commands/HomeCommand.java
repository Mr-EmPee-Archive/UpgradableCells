package ml.empee.upgradableCells.controllers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.SelectCellMenu;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.entity.Player;

/**
 * Controller use to manage cell operations
 */

@Singleton
@CommandAlias("home")
@RequiredArgsConstructor
public class HomeCommand extends BaseCommand {

  private final CellService cellService;
  private final CellController cellController;
  private final LangConfig langConfig;

  /**
   * Teleport a player to his cell
   */
  @Default
  public void teleportToCell(Player sender) {
    var cell = cellService.findCellByOwner(sender.getUniqueId());

    if (cell.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
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
}
