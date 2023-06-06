package ml.empee.upgradableCells.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

/**
 * A cell
 */

@AllArgsConstructor
public class Cell {

  private UUID owner;
  private CellLevel cellProject;
  private Location origin;

  public Location getSpawnpoint() {
    var spawnpoint = cellProject.getSpawnpoint();
    return new Location(origin.getWorld(), spawnpoint.getX(), spawnpoint.getY(), spawnpoint.getZ());
  }

  public Integer getLevel() {
    return cellProject.getLevel();
  }

  public void build() {
    cellProject.paste(origin);
  }

  public void delete() {
    //TODO
  }

}
