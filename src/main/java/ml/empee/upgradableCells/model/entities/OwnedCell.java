package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A cell
 */

@Getter
@Setter
public class OwnedCell {

  private int id;
  private UUID owner;
  private String name = "";
  private String description = "";
  private List<Member> members = Collections.synchronizedList(new ArrayList<>());
  private List<Member> bannedMembers = Collections.synchronizedList(new ArrayList<>());
  private AtomicInteger level = new AtomicInteger();
  private AtomicInteger visits = new AtomicInteger();
  private Location origin = new Location(null, 0, 0, 0);
  private AtomicBoolean pasting = new AtomicBoolean();
  private AtomicBoolean publicVisible = new AtomicBoolean(true);

  public OfflinePlayer getOwnerPlayer() {
    return Bukkit.getOfflinePlayer(owner);
  }

  public void setName(String name) {
    synchronized (name) {
      this.name = name;
    }
  }

  public String getName() {
    synchronized (name) {
      return name;
    }
  }

  public void setDescription(String description) {
    synchronized (description) {
      this.description = description;
    }
  }

  public String getDescription() {
    synchronized (description) {
      return description;
    }
  }

  public Location getOrigin() {
    synchronized (origin) {
      return origin.clone();
    }
  }

  public void setOrigin(Location origin) {
    synchronized (origin) {
      this.origin = origin.clone();
    }
  }

  public static OwnedCell of(UUID owner, Integer level, Location origin) {
    OwnedCell cell = new OwnedCell();
    cell.addMember(Member.create(owner, Member.Rank.OWNER));

    cell.setOwner(owner);
    cell.setLevel(level);
    cell.setOrigin(origin);
    return cell;
  }

  public void setLevel(int level) {
    this.level.set(level);
  }

  public int getLevel() {
    return level.get();
  }

  public void setVisits(int visits) {
    this.visits.set(visits);
  }

  public void addVisit() {
    visits.incrementAndGet();
  }

  public int getVisits() {
    return visits.get();
  }

  public boolean isPublicVisible() {
    return publicVisible.get();
  }

  public void setPublicVisible(boolean publicVisible) {
    this.publicVisible.set(publicVisible);
  }

  public boolean isPasting() {
    return pasting.get();
  }

  public void setPasting(boolean pasting) {
    this.pasting.set(pasting);
  }

  public List<Player> getOnlineMembers() {
    return members.stream()
        .map(m -> Bukkit.getPlayer(m.getUuid()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public List<OfflinePlayer> getAllMembers() {
    return members.stream()
        .map(m -> Bukkit.getOfflinePlayer(m.getUuid()))
        .collect(Collectors.toList());
  }

  public Member getMember(UUID uuid) {
    return members.stream()
        .filter(m -> m.getUuid().equals(uuid))
        .findFirst().orElse(null);
  }

  public void addMember(Member member) {
    members.add(member);
  }

  public Member removeMember(UUID uuid) {
    for (Member member : members) {
      if (member.getUuid().equals(uuid)) {
        members.remove(member);
        return member;
      }
    }

    return null;
  }

  public Member getBannedMember(UUID uuid) {
    return bannedMembers.stream()
        .filter(m -> m.getUuid().equals(uuid))
        .findFirst().orElse(null);
  }

  public void banMember(Member member) {
    bannedMembers.add(member);
  }

  public Member pardonMember(UUID uuid) {
    for (Member member : bannedMembers) {
      if (member.getUuid().equals(uuid)) {
        bannedMembers.remove(member);
        return member;
      }
    }

    return null;
  }

  public boolean isBannedMember(UUID uuid) {
    return bannedMembers.stream().anyMatch(m -> m.getUuid().equals(uuid));
  }

  public boolean hasMember(UUID uuid) {
    return getMember(uuid) != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof OwnedCell)) {
      return false;
    }

    return ((OwnedCell) obj).id == id;
  }
}
