package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.CommandsConfig;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.constants.Permissions;
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
  private final WorldService worldService;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  //TODO: Replace with GUI
  @CommandMethod("cell levelup")
  @CommandPermission(Permissions.ADMIN)
  public void levelup(Player player) {
    OwnedCell cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);
    if (cell == null) {
      cell = OwnedCell.of(player.getUniqueId(), 0, worldService.getFreeLocation());
    } else {
      if (cell.getLevel() + 1 == cellService.getAvailableLevels()) {
        Logger.log(player, "&cMax level reached!");
        return;
      }

      cell.setLevel(cell.getLevel() + 1);
    }

    cellService.updateCellLevel(cell, cell.getLevel());
  }

  @CommandMethod("home")
  public void cellHome(Player player) {
    OwnedCell cell = cellService.findCellByOwner(player.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(player, langConfig.translate("cell.not-bought"));
      return;
    }

    player.teleport(cellService.getSpawnpoint(cell));
  }

  @CommandMethod("cell reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) {
    langConfig.reload();
    cellService.reload();

    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
