package ml.empee.upgradableCells.model.entities;

import lombok.Builder;
import lombok.Getter;
import ml.empee.upgradableCells.model.content.Schematic;
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

  private final String schematicId;
  private Schematic schematic;

  public Vector getSpawnpoint() {
    return schematic.getOrigin();
  }

  public void loadSchematic(File schematicFolder) {
    schematic = new Schematic(new File(schematicFolder, schematicId));
  }

  public CompletableFuture<Void> paste(OwnedCell cell) {
    return schematic.paste(cell);
  }

  public boolean hasSchematic() {
    return schematicId != null;
  }

  public boolean canBuild(OwnedCell ownedCell, Location location) {
    var data = schematic.getBlock(location.toVector().subtract(ownedCell.getOrigin().toVector()));
    return data == null || data.getMaterial() == Material.AIR;
  }

  public static CellProject fromConfig(int level, ConfigurationSection config) {
    return CellProject.builder()
        .level(level)
        .schematicId(config.getString("schematic"))
        .cost(config.getDouble("cost", 0))
        .members(config.getInt("members", 1))
        .build();
  }

}
