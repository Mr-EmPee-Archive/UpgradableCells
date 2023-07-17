package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.UUID;

/**
 * A cell
 */

public class OwnedCell {

  @Getter @Setter
  private UUID owner;

  @Getter @Setter
  private Integer level;

  @Getter @Setter
  private Location origin;

  public static OwnedCell of(UUID owner, Integer level, Location origin) {
    OwnedCell cell = new OwnedCell();
    cell.setOwner(owner);
    cell.setLevel(level);
    cell.setOrigin(origin);
    return cell;
  }

}
