package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * Data of a cell member
 */

@Getter
@Setter
public class Member {

  private UUID uuid;
  private long memberSince;
  private Rank rank;

  public static Member create(UUID uuid, Rank rank) {
    Member member = new Member();
    member.setMemberSince(System.currentTimeMillis());
    member.setRank(rank);
    member.setUuid(uuid);

    return member;
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

    public boolean canCommand(Rank rank) {
      return rank.ordinal() < ordinal();
    }
  }

}
