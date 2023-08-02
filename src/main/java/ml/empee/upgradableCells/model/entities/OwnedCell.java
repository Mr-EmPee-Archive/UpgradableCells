package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A cell
 */

public class OwnedCell {

  @Getter @Setter
  private int id;

  @Getter @Setter
  private UUID owner;

  @Setter
  private Map<UUID, Rank> members = new HashMap<>();

  @Getter @Setter
  private Integer level;

  @Setter
  private Location origin;

  @Getter @Setter
  private boolean pasting;

  public Location getOrigin() {
    return origin.clone();
  }

  public static OwnedCell of(UUID owner, Integer level, Location origin) {
    OwnedCell cell = new OwnedCell();
    cell.setOwner(owner);
    cell.setLevel(level);
    cell.setOrigin(origin);
    return cell;
  }

  public Map<UUID, Rank> getMembers() {
    if (!members.containsValue(Rank.OWNER)) {
      members.put(owner, Rank.OWNER);
    }

    return members;
  }

  /**
   * Cell ranks
   */
  @RequiredArgsConstructor
  public enum Rank {
    MEMBER(false, false),
    GUARD(true, true),
    MANAGER(true, true),
    OWNER(true, true);

    @Getter @Accessors(fluent = true)
    private final boolean canBuild;

    @Getter @Accessors(fluent = true)
    private final boolean canAccessChests;
  }

}
