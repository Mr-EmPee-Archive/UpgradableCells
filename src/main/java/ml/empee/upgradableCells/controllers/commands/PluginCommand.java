package ml.empee.upgradableCells.controllers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.command.CommandSender;

/**
 * Plugin related commands
 */

@Singleton
@RequiredArgsConstructor
public class PluginCommand extends BaseCommand {

  private final LangConfig langConfig;
  private final CellService cellService;

  @Subcommand("cell reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) {
    langConfig.reload();
    cellService.reload();

    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
