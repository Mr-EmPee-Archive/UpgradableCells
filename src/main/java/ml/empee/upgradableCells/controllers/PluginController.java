package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.CommandsConfig;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.ManageCellMenu;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.services.WorldService;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Plugin related commands
 */

@RequiredArgsConstructor
public class PluginController implements Bean {

  private final LangConfig langConfig;
  private final CommandsConfig commandsConfig;
  private final CellService cellService;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  /**
   * Teleport the player to his cell
   */
  @CommandMethod("home")
  public void teleportToHome(Player player) {
    OwnedCell cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(player, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cell.getLevel() == 0 && cell.isPasting()) {
      Logger.log(player, langConfig.translate("cell.still-building"));
      return;
    }

    player.teleport(cellService.getSpawnpoint(cell));
  }

  @CommandMethod("cell")
  public void openCellMenu(Player player) {
    OwnedCell cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);
    if (cell == null) {
      ClaimCellMenu.open(player);
    } else {
      ManageCellMenu.open(player, cell);
    }
  }

  @CommandMethod("cell reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) {
    langConfig.reload();
    cellService.reload();

    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
