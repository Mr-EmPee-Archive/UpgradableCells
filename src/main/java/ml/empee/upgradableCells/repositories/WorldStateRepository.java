package ml.empee.upgradableCells.repositories;

import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.WorldState;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Save the cell world state
 */

public class WorldStateRepository implements Bean {

  private final DbClient client;

  public WorldStateRepository(DbClient client) {
    this.client = client;
  }

  @Override
  @SneakyThrows
  public void onStart() {
    try (var stm = client.getJdbcConnection().createStatement()) {
      stm.executeUpdate("""
          CREATE TABLE IF NOT EXISTS worlds_state (
              world STRING PRIMARY KEY,
              last_cell INTEGER,
              margin INTEGER,
              size INTEGER
          );
          """);
    }
  }

  @SneakyThrows
  private void syncSave(WorldState data) {
    var query = "INSERT OR REPLACE INTO worlds_state (world, last_cell, margin, size) VALUES (?, ?, ?, ?);";
    try (var stm = client.getJdbcConnection().prepareStatement(query)) {
      stm.setString(1, data.getWorld().getName());
      stm.setInt(2, data.getLastCell());
      stm.setInt(3, data.getMargin());
      stm.setInt(4, data.getSize());
      stm.executeUpdate();
    }
  }

  public CompletableFuture<Void> save(WorldState data) {
    return CompletableFuture.runAsync(() -> syncSave(data), client.getThreadPool());
  }

  @SneakyThrows
  private Optional<WorldState> syncFindByWorld(World world) {
    var query = "SELECT * FROM worlds_state WHERE world = ?";
    try (var stm = client.getJdbcConnection().prepareStatement(query)) {
      stm.setString(1, world.getName());

      var result = stm.executeQuery();
      if (!result.next()) {
        return Optional.empty();
      }

      return Optional.of(parseResult(result));
    }
  }

  public CompletableFuture<Optional<WorldState>> findByWorld(World world) {
    return CompletableFuture.supplyAsync(() -> syncFindByWorld(world), client.getThreadPool());
  }

  @SneakyThrows
  private WorldState parseResult(ResultSet rs) {
    WorldState cell = new WorldState(Bukkit.getWorld(rs.getString("world")));
    cell.setMargin(rs.getInt("margin"));
    cell.setSize(rs.getInt("size"));
    cell.setLastCell(rs.getInt("last_cell"));
    return cell;
  }

}
