package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A cell
 */

public class OwnedCell {

  @Getter
  @Setter
  private int id;

  @Getter
  @Setter
  private UUID owner;

  @Setter
  private Map<UUID, Rank> members = new HashMap<>();

  @Getter
  @Setter
  private Integer level;

  @Setter
  private Location origin;

  @Getter
  @Setter
  private boolean pasting;

  public OfflinePlayer getOwnerPlayer() {
    return Bukkit.getOfflinePlayer(owner);
  }

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

  public List<Player> getOnlineMembers() {
    return members.keySet().stream()
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Cell ranks
   */
  @RequiredArgsConstructor
  public enum Rank {
    MEMBER(false, false, false, false, false),
    GUARD(true, true, false, false, false),
    MANAGER(true, true, true, false, true),
    OWNER(true, true, true, true, true);

    @Getter
    @Accessors(fluent = true)
    private final boolean canBuild;

    @Getter
    @Accessors(fluent = true)
    private final boolean canAccessChests;

    @Getter
    @Accessors(fluent = true)
    private final boolean canInvite;

    @Getter
    @Accessors(fluent = true)
    private final boolean canUpgrade;

    @Getter
    @Accessors(fluent = true)
    private final boolean canPromote;

    public boolean canCommand(OwnedCell.Rank rank) {
      return rank.ordinal() < ordinal();
    }
  }

}
