package ml.empee.upgradableCells.model.entities;

import lombok.Builder;
import lombok.Getter;
import ml.empee.upgradableCells.utils.helpers.Schematic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Represent a cell upgrade
 */

@Builder
public class CellProject {

  @Getter
  private final int level;

  @Getter
  private final double cost;

  @Getter
  private final int members;

  @Getter
  private CellProject parent;

  private final String schematicId;
  private Schematic schematic;

  private Schematic getSchematic() {
    if (schematic != null) {
      return schematic;
    } else {
      return parent.getSchematic();
    }
  }

  public Vector getSpawnpoint() {
    return getSchematic().getOrigin();
  }

  public void loadSchematic(File schematicFolder) {
    schematic = new Schematic(new File(schematicFolder, schematicId));
  }

  public CompletableFuture<Void> paste(OwnedCell cell) {
    Schematic schematic = parent == null ? getSchematic() : parent.getSchematic();
    return getSchematic().paste(cell.getOrigin(), l -> {
      var data = schematic.getBlock(l);
      return data == null || data.getMaterial() == Material.AIR;
    });
  }

  public boolean hasSchematic() {
    return schematicId != null;
  }

  public boolean isCellBlock(OwnedCell ownedCell, Location location) {
    var data = getSchematic().getBlock(location.toVector().subtract(ownedCell.getOrigin().toVector()));
    return data != null && data.getMaterial() != Material.AIR;
  }

  /**
   * Parse a cellProject configuration
   */
  public static CellProject fromConfig(CellProject parent, Integer level, ConfigurationSection config) {
    return CellProject.builder()
        .level(level)
        .parent(parent)
        .schematicId(config.getString("schematic"))
        .cost(config.getDouble("cost", 0))
        .members(config.getInt("members", 1))
        .build();
  }

}
