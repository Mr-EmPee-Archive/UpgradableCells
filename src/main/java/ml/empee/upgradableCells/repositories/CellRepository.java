package ml.empee.upgradableCells.repositories;

import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.utils.ObjectConverter;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Persist a cell data
 */

public class CellRepository implements Bean {

  private final DbClient client;

  public CellRepository(DbClient client) {
    this.client = client;
  }

  @Override
  @SneakyThrows
  public void onStart() {
    try (var stm = client.getJdbcConnection().createStatement()) {
      stm.executeUpdate("""
          CREATE TABLE IF NOT EXISTS cells (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              owner STRING,
              level INTEGER,
              origin STRING
          );
          """);
    }
  }

  @SneakyThrows
  private void syncSave(OwnedCell data) {
    var query = "INSERT INTO cells (owner, level, origin) VALUES (?, ?, ?);";
    try (var stm = client.getJdbcConnection().prepareStatement(query)) {
      stm.setString(0, data.getOwner().toString());
      stm.setInt(1, data.getLevel());
      stm.setString(2, ObjectConverter.parseLocation(data.getOrigin()));
      stm.executeUpdate();
    }
  }

  public CompletableFuture<Void> save(OwnedCell data) {
    return CompletableFuture.runAsync(() -> syncSave(data), client.getThreadPool());
  }

  @SneakyThrows
  private Optional<OwnedCell> syncFindByOwner(UUID owner) {
    var query = "SELECT * FROM cells WHERE owner = ?";
    try (var stm = client.getJdbcConnection().prepareStatement(query)) {
      stm.setString(0, owner.toString());

      var result = stm.executeQuery();
      if (!result.next()) {
        return Optional.empty();
      }

      return Optional.of(parseResult(result));
    }
  }

  public CompletableFuture<Optional<OwnedCell>> findByOwner(UUID owner) {
    return CompletableFuture.supplyAsync(() -> syncFindByOwner(owner), client.getThreadPool());
  }

  @SneakyThrows
  private OwnedCell parseResult(ResultSet rs) {
    OwnedCell cell = new OwnedCell();
    cell.setOwner(UUID.fromString(rs.getString("owner")));
    cell.setLevel(rs.getInt("level"));
    cell.setOrigin(ObjectConverter.parseLocation(rs.getString("origin")));
    return cell;
  }

}
