package ml.empee.upgradableCells.controllers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.TopCellsMenu;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.entity.Player;

/**
 * Controller use to manage cell operations
 */

@Singleton
@CommandAlias("cell-top")
@RequiredArgsConstructor
public class CellTopCommand extends BaseCommand {
  /**
   * Cell-Top
   */
  @Default
  public void openCellTopMenu(Player sender) {
    TopCellsMenu.open(sender);
  }
}
