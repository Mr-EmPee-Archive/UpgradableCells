package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.CommandsConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.utils.Logger;
import ml.empee.upgradableCells.utils.Translator;
import org.bukkit.command.CommandSender;

/**
 * Plugin related commands
 */

@RequiredArgsConstructor
public class PluginController implements Bean {

  private final CommandsConfig commandsConfig;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  @CommandMethod("mb reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) {
    Translator.reload();

    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
