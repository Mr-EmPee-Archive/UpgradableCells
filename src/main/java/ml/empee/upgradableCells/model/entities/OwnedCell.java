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

//TODO Name & Description

@Getter
@Setter
public class OwnedCell {

  private int id;
  private UUID owner;
  private Map<UUID, Rank> members = new HashMap<>();
  private Integer level;
  private Location origin;
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

  public List<OfflinePlayer> getMemberPlayers() {
    return members.keySet().stream()
        .map(Bukkit::getOfflinePlayer)
        .toList();
  }

  /**
   * Cell ranks
   */
  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor
  public enum Rank {
    MEMBER(false, false, false, false, false),
    GUARD(true, true, false, false, false),
    MANAGER(true, true, true, false, true),
    OWNER(true, true, true, true, true);

    private final boolean canBuild;
    private final boolean canAccessChests;
    private final boolean canInvite;
    private final boolean canUpgrade;
    private final boolean canPromote;

    public boolean canCommand(OwnedCell.Rank rank) {
      return rank.ordinal() < ordinal();
    }
  }

}
