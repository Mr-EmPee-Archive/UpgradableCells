package ml.empee.upgradableCells.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData;

/**
 * Access NMS easily
 */

//TODO: Updated for retrocompat

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NmsUtils {

  public static void setBlockFast(Location location, BlockData blockData) {
    if (location.getBlock().getBlockData().equals(blockData)) {
      return;
    }

    ((CraftWorld) location.getWorld()).getHandle()
        .setBlock(
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
            ((CraftBlockData) blockData).getState(), 2 | 16 | 1024 //2 | 16 | 1024
        );
  }

}
