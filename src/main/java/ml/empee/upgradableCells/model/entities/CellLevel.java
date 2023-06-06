package ml.empee.upgradableCells.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ml.empee.upgradableCells.utils.helpers.Schematic;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Represent a cell upgrade
 */

@AllArgsConstructor
public class CellLevel {

  @Getter
  private final int id;

  @Getter
  private final Integer level;
  private final Schematic schematic;

  public Vector getSpawnpoint() {
    return schematic.getOrigin();
  }

  public void paste(Location location) {
    schematic.paste(location);
  }

}
