package ml.empee.upgradableCells.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.stream.Collectors;

/**
 * Store the plugin permissions
 **/

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissions {

  private static final String PREFIX = "upgradablecells.";
  public static final String ADMIN = PREFIX + "admin";

  private static final String CLAIMABLE_CELLS = PREFIX + "own.";

  public static boolean canClaimCells(Permissible entity, int numberOfCells) {
    if (entity.hasPermission(ADMIN)) {
      return true;
    }

    var permissions = entity.getEffectivePermissions().stream()
        .filter(PermissionAttachmentInfo::getValue)
        .filter(p -> p.getPermission().toLowerCase().startsWith(CLAIMABLE_CELLS))
        .collect(Collectors.toList());

    for (var p : permissions) {
      var groups = p.getPermission().split("\\.");

      try {
        int maxCells = Integer.parseInt(groups[groups.length - 1]);
        if (maxCells >= numberOfCells) {
          return true;
        }
      } catch (IllegalArgumentException ignored) {}
    }

    return false;
  }

}
