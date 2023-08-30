package ml.empee.upgradableCells.repositories;

import io.leangen.geantyref.TypeToken;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.utils.ObjectConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
              owner TEXT PRIMARY KEY,
              level INTEGER NOT NULL,
              origin TEXT NOT NULL,
              pasting INTEGER DEFAULT 0 NOT NULL,
              members TEXT DEFAULT "" NOT NULL
          );
          """);
    }
  }

  /**
   * Update or create a cell
   */
  public CompletableFuture<Void> save(OwnedCell data) {
    return CompletableFuture.runAsync(() -> {
      var query = """
          INSERT OR REPLACE INTO cells (
            owner, level, origin, pasting, members
          ) VALUES (?, ?, ?, ?, ?);
          """;

      try (var stm = client.getJdbcConnection().prepareStatement(query)) {
        stm.setString(1, data.getOwner().toString());
        stm.setInt(2, data.getLevel());
        stm.setString(3, ObjectConverter.parseLocation(data.getOrigin()));
        stm.setInt(4, data.isPasting() ? 1 : 0);
        stm.setString(5, ObjectConverter.parse(data.getMembers()));
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

  public CompletableFuture<List<OwnedCell>> findByMember(UUID member) {
    return CompletableFuture.supplyAsync(() -> {
      var query = "SELECT * FROM cells WHERE members LIKE ?";
      try (var stm = client.getJdbcConnection().prepareStatement(query)) {
        stm.setString(1, "\"" + member + "\":");
        return parseList(stm.executeQuery());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, client.getThreadPool());
  }

  public CompletableFuture<List<OwnedCell>> findAll() {
    return CompletableFuture.supplyAsync(() -> {
      String query = "SELECT * FROM cells";
      try (var stm = client.getJdbcConnection().createStatement()) {
        return parseList(stm.executeQuery(query));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, client.getThreadPool());
  }

  @SneakyThrows
  private List<OwnedCell> parseList(ResultSet rs) {
    List<OwnedCell> cells = new ArrayList<>();
    while (rs.next()) {
      cells.add(parseResult(rs));
    }

    return cells;
  }

  @SneakyThrows
  private OwnedCell parseResult(ResultSet rs) {
    OwnedCell cell = new OwnedCell();
    cell.setPasting(rs.getInt("pasting") == 1);
    cell.setOwner(UUID.fromString(rs.getString("owner")));
    cell.setLevel(rs.getInt("level"));
    cell.setOrigin(ObjectConverter.parseLocation(rs.getString("origin")));
    cell.setMembers(ObjectConverter.parse(
        rs.getString("members"), new TypeToken<>() {}
    ));

    return cell;
  }

}
