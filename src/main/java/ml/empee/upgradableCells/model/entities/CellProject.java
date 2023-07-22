package ml.empee.upgradableCells.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ml.empee.upgradableCells.model.content.Schematic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

/**
 * Represent a cell upgrade
 */

@AllArgsConstructor
public class CellProject {

  @Getter
  private final Integer level;
  private final Schematic schematic;

  public Vector getSpawnpoint() {
    return schematic.getOrigin();
  }

  public void paste(OwnedCell cell) {
    schematic.paste(cell);
  }

  public boolean canBuild(OwnedCell ownedCell, Location location) {
    var data = schematic.getBlock(location.toVector().subtract(ownedCell.getOrigin().toVector()));
    return data == null || data.getMaterial() == Material.AIR;
  }

}
