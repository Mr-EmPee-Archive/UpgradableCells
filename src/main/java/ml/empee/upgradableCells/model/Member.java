package ml.empee.upgradableCells.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Data of a cell member
 */

@With
@Value
@Builder
public class Member {

  UUID uuid;
  Long memberSince;
  Long bannedSince;
  Rank rank;

  public static Member create(UUID uuid, Rank rank) {
    return Member.builder()
        .memberSince(System.currentTimeMillis())
        .rank(rank)
        .uuid(uuid)
        .build();
  }

  public static Member banned(UUID uuid) {
    return Member.builder()
        .uuid(uuid)
        .bannedSince(System.currentTimeMillis())
        .build();
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
  public enum Rank {
    MEMBER(),
    GUARD(Permissions.BUILD, Permissions.ACCESS_CHESTS),
    MANAGER(Permissions.BUILD, Permissions.ACCESS_CHESTS, Permissions.MANAGE_MEMBERS,
        Permissions.INVITE,Permissions.CHANGE_VISIBILITY),
    OWNER(Permissions.BUILD, Permissions.ACCESS_CHESTS, Permissions.UPGRADE,
        Permissions.INVITE,Permissions.CHANGE_VISIBILITY, Permissions.MANAGE_MEMBERS);

    private final Permissions[] permissions;

    Rank(Permissions... permissions) {
      this.permissions = permissions;
    }

    public boolean hasPermission(Permissions permission) {
      for (var p : permissions) {
        if (p == permission) {
          return true;
        }
      }

      return false;
    }

    public boolean canManage(@Nullable Rank rank) {
      if (rank == null) {
        return hasPermission(Permissions.MANAGE_MEMBERS);
      }

      return hasPermission(Permissions.MANAGE_MEMBERS) && rank.ordinal() < ordinal();
    }
  }

  public enum Permissions {
    BUILD,
    ACCESS_CHESTS,
    INVITE,
    UPGRADE,
    MANAGE_MEMBERS,
    CHANGE_VISIBILITY
  }

}
