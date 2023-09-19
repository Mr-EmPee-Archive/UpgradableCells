package ml.empee.upgradableCells.utils.helpers;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

/**
 * Generator for a void world
 */

public class VoidGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createChunkData(world);
    }

}
