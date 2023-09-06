package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Data of a cell member
 */

@Getter
@Setter
public class Member {

  private UUID uuid;
  private Long memberSince;
  private Long bannedSince;
  private Rank rank;

  public static Member create(UUID uuid, Rank rank) {
    Member member = new Member();
    member.setMemberSince(System.currentTimeMillis());
    member.setRank(rank);
    member.setUuid(uuid);

    return member;
  }

  public static Member banned(UUID uuid) {
    Member member = new Member();
    member.setUuid(uuid);
    member.setBannedSince(System.currentTimeMillis());
    return member;
  }

  public LocalDateTime getMemberSince() {
    return Instant.ofEpochMilli(memberSince).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  @Nullable
  public LocalDateTime getBannedSince() {
    if (bannedSince == null) {
      return null;
    }

    return Instant.ofEpochMilli(bannedSince).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public boolean isBanned() {
    return bannedSince != null;
  }

  /**
   * Cell ranks
   */
  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor
  public enum Rank {
    MEMBER(false, false, false, false, false, false),
    GUARD(true, true, false, false, false, false),
    MANAGER(true, true, true, false, true, false),
    OWNER(true, true, true, true, true, true);

    private final boolean canBuild;
    private final boolean canAccessChests;
    private final boolean canInvite;
    private final boolean canUpgrade;
    private final boolean canManageMembers;
    private final boolean canChangeVisibility;

    public boolean canManage(@Nullable Rank rank) {
      if (rank == null) {
        return canManageMembers;
      }

      return canManageMembers && rank.ordinal() < ordinal();
    }
  }

}
