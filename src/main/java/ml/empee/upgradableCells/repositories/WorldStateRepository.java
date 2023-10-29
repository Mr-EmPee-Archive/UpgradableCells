package ml.empee.upgradableCells.repositories;

import lombok.SneakyThrows;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.WorldState;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Save the cell world state
 */

@Singleton
public class WorldStateRepository extends AbstractRepository<WorldState, String> {

  public WorldStateRepository(DbClient client) {
    super(client, "worlds_state");
  }

  @Override
  protected List<String> schema() {
    return List.of(
        "world STRING PRIMARY KEY",
        "last_cell INTEGER",
        "size INTEGER"
    );
  }

  @Override
  protected void prepareStatement(PreparedStatement stm, WorldState data) throws SQLException {
    stm.setString(1, data.getWorld().getName());
    stm.setInt(2, data.getLastCell());
    stm.setInt(3, data.getSize());
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

        return Optional.of(parse(result));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, client.getThreadPool());
  }

  @SneakyThrows
  protected WorldState parse(ResultSet rs) {
    WorldState cell = new WorldState(Bukkit.getWorld(rs.getString("world")));
    cell.setSize(rs.getInt("size"));
    cell.setLastCell(rs.getInt("last_cell"));
    return cell;
  }

}
