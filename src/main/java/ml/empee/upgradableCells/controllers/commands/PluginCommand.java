package ml.empee.upgradableCells.controllers.commands;

import ml.empee.upgradableCells.controllers.Controller;
import org.bukkit.command.CommandSender;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Plugin related commands
 */

@Singleton
@RequiredArgsConstructor
public class PluginCommand implements Controller {

  private final LangConfig langConfig;
  private final CellService cellService;

  @CommandMethod("cell reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) {
    langConfig.reload();
    cellService.reload();

    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
