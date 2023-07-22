package ml.empee.upgradableCells.config.client;

import lombok.Getter;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Database client
 */

public class DbClient implements Bean {

  private static final ThreadFactory threadFactory = new ThreadFactory() {
    @Override
    public Thread newThread(@NotNull Runnable r) {
      return new Thread(() -> {
        try {
          r.run();
        } catch (Exception e) {
          Logger.error("Error while performing db operation");
          e.printStackTrace();
        }
      }, "database");
    }
  };

  @Getter
  private final ExecutorService threadPool = Executors.newFixedThreadPool(1, threadFactory);
  private final String dbUrl;

  private Connection jdbcConnection;

  public DbClient(JavaPlugin plugin) {
    File dbFile = new File(plugin.getDataFolder(), "cells.sqlite");
    this.dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

    dbFile.getParentFile().mkdirs();
  }

  @Override
  public void onStart() {
    //Ping db
  }

  @SneakyThrows
  public Connection getJdbcConnection() {
    if (jdbcConnection == null || jdbcConnection.isClosed()) {
      jdbcConnection = DriverManager.getConnection(dbUrl);
      jdbcConnection.setAutoCommit(true);
    }

    return jdbcConnection;
  }

  @SneakyThrows
  public void closeConnections() {
    Logger.info("Shutting down db connections (Forced stop in 60seconds)");

    threadPool.shutdown();
    threadPool.awaitTermination(60, TimeUnit.SECONDS);

    jdbcConnection.close();
  }

}
