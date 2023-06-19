package ml.empee.upgradableCells.config;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class DatabaseConfiguration {

  private final JdbcConnectionSource connectionSource;

  @Getter
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final Logger logger;

  @SneakyThrows
  public DatabaseConfiguration(JavaPlugin plugin) {
    this.logger = plugin.getLogger();

    File dbFile = new File(plugin.getDataFolder(), "cells.sqlite");
    String dbURL = "jdbc:sqlite:" + dbFile.getAbsolutePath();

    dbFile.getParentFile().mkdirs();
    LoggerFactory.setLogBackendFactory(LogBackendType.NULL);
    connectionSource = new JdbcConnectionSource(dbURL);
  }

  public ConnectionSource getConnectionSource() {
    return connectionSource;
  }

  @SneakyThrows
  public void closeConnection() {
    logger.info("Shutting down db connections (Forced stop in 60seconds)");
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
    connectionSource.close();
  }

}
