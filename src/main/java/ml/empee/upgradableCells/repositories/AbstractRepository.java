package ml.empee.upgradableCells.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * CRUD Repository
 *
 * @param <ID> Id type of the repo
 * @param <T>  Type of the repo
 */

public abstract class AbstractRepository<T, ID> {

  protected final Executor executor;
  protected final Dao<T, ID> dao;

  protected AbstractRepository(Executor executor, ConnectionSource connection, Class<T> target) throws SQLException {
    this.executor = executor;

    TableUtils.createTableIfNotExists(connection, target);
    this.dao = DaoManager.createDao(connection, target);
  }

  protected CompletableFuture<Void> save(T data) {
    return CompletableFuture.runAsync(() -> {
      try {
        dao.createOrUpdate(data);
      } catch (SQLException e) {
        throw new RuntimeException("Error while saving lock data, " + data, e);
      }
    });
  }

  protected CompletableFuture<Void> delete(ID id) {
    return CompletableFuture.runAsync(() -> {
      try {
        dao.deleteById(id);
      } catch (SQLException e) {
        throw new RuntimeException("Error while deleting lock data with" + id, e);
      }
    });
  }

  protected CompletableFuture<Optional<T>> findByID(ID id) {
    return buildFutureSupplier(() -> {
      try {
        return Optional.ofNullable(dao.queryForId(id));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
  }

  protected CompletableFuture<List<T>> findAll() {
    return buildFutureSupplier(() -> {
      try {
        return dao.queryForAll();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
  }


  protected <K> CompletableFuture<K> buildFutureSupplier(Supplier<K> supplier) {
    return CompletableFuture.supplyAsync(supplier, executor).whenComplete((result, exception) -> {
      if (exception != null) {
        Bukkit.getLogger().log(
            Level.SEVERE, "Exception on thread: LudoLock Async Persistence Thread", exception
        );
      }
    });
  }

}
