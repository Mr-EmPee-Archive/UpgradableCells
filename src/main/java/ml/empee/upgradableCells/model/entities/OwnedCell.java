package ml.empee.upgradableCells.model.entities;

import com.j256.ormlite.field.DatabaseField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ml.empee.upgradableCells.utils.ObjectConverter;
import org.bukkit.Location;

import java.util.UUID;

/**
 * A cell
 */

@NoArgsConstructor
public class OwnedCell {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;

  @Getter @Setter
  @DatabaseField
  private UUID owner;

  @Getter @Setter
  @DatabaseField
  private Integer level;

  @DatabaseField
  private String origin;
  private Location originCache;

  public static OwnedCell of(UUID owner, Integer level, Location origin) {
    OwnedCell cell = new OwnedCell();
    cell.setOwner(owner);
    cell.setLevel(level);
    cell.setOrigin(origin);
    return cell;
  }

  public void setOrigin(Location origin) {
    this.origin = ObjectConverter.parseLocation(origin);
    this.originCache = origin;
  }

  public Location getOrigin() {
    if (originCache == null) {
      originCache = ObjectConverter.parseLocation(origin);
    }

    return originCache.clone();
  }

}
