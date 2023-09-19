package ml.empee.upgradableCells.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Access NMS easily
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NmsUtils {

  private static Method getNmsWorldMethod;
  private static Constructor<?> blockPositionConstructor;
  private static Method getBlockStateMethod;
  private static Method setBlockMethod;

  @SneakyThrows
  public static Object buildBlockPos(int x, int y, int z) {
    if (blockPositionConstructor == null) {
      blockPositionConstructor = findBlockPosClass().getConstructor(int.class, int.class, int.class);
    }

    return blockPositionConstructor.newInstance(x, y, z);
  }

  @SneakyThrows
  private static Class<?> findBlockPosClass() {
    if (ServerVersion.isGreaterThan(1, 17)) {
      return Class.forName("net.minecraft.core.BlockPosition");
    } else {
      return Class.forName(
          "net.minecraft.server." + ServerVersion.getNmsVersion() + ".BlockPosition"
      );
    }
  }

  @SneakyThrows
  private static Class<?> findWorldNmsClass() {
    if (ServerVersion.isGreaterThan(1, 17)) {
      return Class.forName("net.minecraft.world.level.World");
    } else {
      return Class.forName(
          "net.minecraft.server." + ServerVersion.getNmsVersion() + ".World"
      );
    }
  }

  @SneakyThrows
  public static Object getBlockState(BlockData blockData) {
    if (getBlockStateMethod == null) {
      getBlockStateMethod = blockData.getClass().getMethod("getState");
    }

    return getBlockStateMethod.invoke(blockData);
  }

  @SneakyThrows
  public static Object getNmsWorld(World world) {
    if (getNmsWorldMethod == null) {
      getNmsWorldMethod = world.getClass().getMethod("getHandle");
    }

    return getNmsWorldMethod.invoke(world);
  }

  /**
   * Method used to change a world block, faster then bukkit one
   */
  @SneakyThrows
  public static void setBlockFast(Location location, BlockData blockData) {
    if (location.getBlock().getType() == blockData.getMaterial()) {
      return;
    }

    Object blockPos = buildBlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    Object blockState = getBlockState(blockData);
    int flags = 2 | 16 | 1024;

    if (setBlockMethod == null) {
      setBlockMethod = Arrays.stream(findWorldNmsClass().getMethods())
          .filter(m -> m.getParameterCount() == 3)
          .filter(m -> m.getReturnType() == boolean.class)
          .filter(m -> m.getParameterTypes()[0] == blockPos.getClass())
          .filter(m -> m.getParameterTypes()[1].isAssignableFrom(blockState.getClass()))
          .filter(m -> m.getParameterTypes()[2] == int.class)
          .findFirst().orElseThrow();
    }

    setBlockMethod.invoke(getNmsWorld(location.getWorld()), blockPos, blockState, flags);
  }

}
