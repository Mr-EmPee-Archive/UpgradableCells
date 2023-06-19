package ml.empee.upgradableCells.repositories;

import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.DatabaseConfiguration;
import ml.empee.upgradableCells.model.entities.OwnedCell;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Persist a cell data
 */

public class CellRepository extends AbstractRepository<OwnedCell, Integer> implements Bean {
  public CellRepository(DatabaseConfiguration config) throws SQLException {
    super(config.getExecutor(), config.getConnectionSource(), OwnedCell.class);
  }

  @Override
  public CompletableFuture<Void> save(OwnedCell data) {
    return super.save(data);
  }

  public CompletableFuture<List<OwnedCell>> findByOwner(UUID owner) {
    return buildFutureSupplier(() -> {
      try {
        return dao.queryForEq("owner", owner);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
  }

}
