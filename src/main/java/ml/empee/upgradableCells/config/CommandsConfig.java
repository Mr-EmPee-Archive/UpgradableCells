package ml.empee.upgradableCells.config;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import io.leangen.geantyref.TypeToken;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

/**
 * Config class to setup cloud commands framework
 */

@Singleton
public class CommandsConfig {

  private final LangConfig langConfig;
  private final PaperCommandManager<CommandSender> commandManager;
  private final AnnotationParser<CommandSender> commandParser;

  /**
   * Setup the command framework
   */
  public CommandsConfig(JavaPlugin plugin, LangConfig langConfig) throws Exception {
    this.langConfig = langConfig;
    commandManager = new PaperCommandManager<>(
        plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity()
    );

    commandParser = new AnnotationParser<>(
        commandManager, CommandSender.class, parameters -> SimpleCommandMeta.empty()
    );

    registerExceptionHandlers();
    registerBrigadier();
  }

  private void registerBrigadier() {
    try {
      commandManager.registerBrigadier();
    } catch (BukkitCommandManager.BrigadierFailureException e) {
      Logger.warning("Command suggestion not supported! If you think this is an error make sure to use paper");
    }
  }

  private void registerExceptionHandlers() {
    commandManager.registerExceptionHandler(NoPermissionException.class, (sender, e) -> {
      Logger.log(sender, langConfig.translate("cmd.missing-permission"));
    });

    commandManager.registerExceptionHandler(InvalidSyntaxException.class, (sender, e) -> {
      Logger.log(sender, langConfig.translate("cmd.invalid-syntax"));
    });

    commandManager.registerExceptionHandler(InvalidCommandSenderException.class, (sender, e) -> {
      Logger.log(sender, langConfig.translate("cmd.invalid-sender"));
    });

    commandManager.registerExceptionHandler(ArgumentParseException.class, (sender, e) -> {
      Logger.log(sender, langConfig.translate("cmd.invalid-argument", e.getCause().getMessage()));
    });

    commandManager.registerExceptionHandler(Exception.class, (sender, e) -> {
      Logger.log(sender, langConfig.translate("cmd.unknown-error"));
    });
  }

  public <T> void register(T command) {
    commandParser.parse(command);
  }

  public <T> void register(Class<T> type, ArgumentParser<CommandSender, T> parser) {
    commandManager.parserRegistry().registerParserSupplier(
        TypeToken.get(type), opts -> parser
    );
  }

}
