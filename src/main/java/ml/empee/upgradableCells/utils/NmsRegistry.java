package ml.empee.upgradableCells.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * NMS Registry
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NmsRegistry {

  private static Class<?> blockClass;
  private static Method getStateMethod;
  private static Method getIdMethod;

  static {
    findBlockClass();
  }

  @SneakyThrows
  private static void findBlockClass() {
    if (ServerVersion.isGreaterThan(1, 17, 1)) {
      blockClass = Class.forName("net.minecraft.world.level.block.Block");
    } else {
      blockClass = Class.forName(
          "net.minecraft.server." + ServerVersion.getNmsVersion() + ".Block"
      );
    }
  }

  /**
   * @return the block id from the BLOCK_STATE_REGISTRY
   */
  @SneakyThrows
  public static int getBlockId(BlockData blockData) {
    if (getStateMethod == null) {
      getStateMethod = blockData.getClass().getMethod("getState");
    }

    Object state = getStateMethod.invoke(blockData);

    if (getIdMethod == null) {
      getIdMethod = Arrays.stream(blockClass.getMethods())
          .filter(m -> m.getReturnType() == int.class)
          .filter(m -> m.getParameterCount() == 1)
          .filter(m -> m.getParameterTypes()[0] == state.getClass())
          .findFirst().get();
    }

    return (int) getIdMethod.invoke(null, state);
  }

}