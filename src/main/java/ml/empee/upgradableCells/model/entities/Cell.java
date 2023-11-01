package ml.empee.upgradableCells.model.entities;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import ml.empee.upgradableCells.model.Member;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@With
@Value
@Builder
public class Cell implements Entity<UUID> {

  UUID owner;

  String name;
  String description;

  @Builder.Default
  Set<Member> members = new HashSet<>();

  @Builder.Default
  Set<Member> bannedMembers = new HashSet<>();

  Integer level;

  Location origin;

  boolean updating;
  boolean publicVisible;

  public static Cell of(UUID owner, Integer level, Location origin) {
    Cell cell = Cell.builder()
        .owner(owner)
        .level(level)
        .origin(origin.clone())
        .build();

    cell = cell.withMember(Member.create(owner, Member.Rank.OWNER));

    return cell;
  }

  public Location getOrigin() {
    return origin.clone();
  }

  public Set<Member> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  public Set<Member> getBannedMembers() {
    return Collections.unmodifiableSet(bannedMembers);
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

  public Optional<Member> getBannedMember(UUID uuid) {
    return bannedMembers.stream()
        .filter(m -> m.getUuid().equals(uuid))
        .findFirst();
  }

  public Optional<Member> getMember(UUID uuid) {
    return members.stream()
        .filter(m -> m.getUuid().equals(uuid))
        .findFirst();
  }

  public Cell withoutBannedMember(UUID uuid) {
    return withBannedMembers(
        bannedMembers.stream()
            .filter(m -> !m.getUuid().equals(uuid))
            .collect(Collectors.toSet())
    );
  }

  public Cell withoutMember(UUID uuid) {
    return withMembers(
        members.stream()
            .filter(m -> !m.getUuid().equals(uuid))
            .collect(Collectors.toSet())
    );
  }

  public Cell withBannedMember(Member member) {
    var bannedMembers = new HashSet<>(this.bannedMembers);

    bannedMembers.removeIf(m -> m.getUuid().equals(member.getUuid()));
    bannedMembers.add(member);

    return withBannedMembers(bannedMembers);
  }

  public Cell withMember(Member member) {
    var members = new HashSet<>(this.members);

    members.removeIf(m -> m.getUuid().equals(member.getUuid()));
    members.add(member);

    return withMembers(members);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Cell)) {
      return false;
    }

    return owner.equals(((Cell) obj).owner);
  }

  public OfflinePlayer getPlayerOwner() {
    return Bukkit.getOfflinePlayer(owner);
  }

  @Override
  public UUID getId() {
    return owner;
  }
}
