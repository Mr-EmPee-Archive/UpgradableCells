package ml.empee.upgradableCells.repositories;

import lombok.SneakyThrows;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.WorldState;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Save the cell world state
 */

@Singleton
public class WorldStateRepository {

  private final DbClient client;

  public WorldStateRepository(DbClient client) {
    this.client = client;
    createTable();
  }

  @SneakyThrows
  private void createTable() {
    try (var stm = client.getJdbcConnection().createStatement()) {
      stm.executeUpdate("""
          CREATE TABLE IF NOT EXISTS worlds_state (
              world STRING PRIMARY KEY,
              last_cell INTEGER,
              size INTEGER
          );
          """);
    }
  }

  /**
   * Create or update a world-sate
   */
  public CompletableFuture<Void> save(WorldState data) {
    return CompletableFuture.runAsync(() -> {
      var query = "INSERT OR REPLACE INTO worlds_state (world, last_cell, margin, size) VALUES (?, ?, ?);";
      try (var stm = client.getJdbcConnection().prepareStatement(query)) {
        stm.setString(1, data.getWorld().getName());
        stm.setInt(2, data.getLastCell());
        stm.setInt(3, data.getSize());
        stm.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, client.getThreadPool());
  }

  /**
   * Find a world-sate
   */
  public CompletableFuture<Optional<WorldState>> findByWorld(World world) {
    return CompletableFuture.supplyAsync(() -> {
      var query = "SELECT * FROM worlds_state WHERE world = ?";
      try (var stm = client.getJdbcConnection().prepareStatement(query)) {
        stm.setString(1, world.getName());

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
  private WorldState parseResult(ResultSet rs) {
    WorldState cell = new WorldState(Bukkit.getWorld(rs.getString("world")));
    cell.setSize(rs.getInt("size"));
    cell.setLastCell(rs.getInt("last_cell"));
    return cell;
  }

}
