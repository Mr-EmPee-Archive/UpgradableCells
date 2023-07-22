package ml.empee.upgradableCells.repositories;

import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.utils.ObjectConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
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
              owner STRING PRIMARY KEY,
              level INTEGER,
              origin STRING
          );
          """);
    }
  }

  /**
   * Update or create a cell
   */
  public CompletableFuture<Void> save(OwnedCell data) {
    return CompletableFuture.runAsync(() -> {
      var query = "INSERT OR REPLACE INTO cells (owner, level, origin) VALUES (?, ?, ?);";
      try (var stm = client.getJdbcConnection().prepareStatement(query)) {
        stm.setString(1, data.getOwner().toString());
        stm.setInt(2, data.getLevel());
        stm.setString(3, ObjectConverter.parseLocation(data.getOrigin()));
        stm.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, client.getThreadPool());
  }

  /**
   * Find a cell by his owner
   */
  public CompletableFuture<Optional<OwnedCell>> findByOwner(UUID owner) {
    return CompletableFuture.supplyAsync(() -> {
      var query = "SELECT * FROM cells WHERE owner = ?";
      try (var stm = client.getJdbcConnection().prepareStatement(query)) {
        stm.setString(1, owner.toString());

        var result = stm.executeQuery();
        if (!result.next()) {
          return Optional.empty();
        }

        return Optional.of(parseResult(result));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, client.getThreadPool());
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
