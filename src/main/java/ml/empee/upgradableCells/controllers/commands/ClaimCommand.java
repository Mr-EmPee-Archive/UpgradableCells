package ml.empee.upgradableCells.controllers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.entity.Player;

/**
 * Controller use to manage cell operations
 */

@Singleton
@CommandAlias("claim")
@RequiredArgsConstructor
public class ClaimCommand extends BaseCommand {
  @Default
  public void claimCell(Player sender) {
    ClaimCellMenu.open(sender);
  }

}
