package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.CommandsConfig;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.services.CellService;
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

  @CommandMethod("cell test")
  @CommandPermission(Permissions.ADMIN)
  public void test(Player player) {
    new Cell(player.getUniqueId(), cellService.getCellLevel(0), player.getLocation()).build();
  }

  @CommandMethod("cell reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) {
    langConfig.reload();
    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
