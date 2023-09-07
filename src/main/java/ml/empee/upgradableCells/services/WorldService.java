package ml.empee.upgradableCells.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.model.entities.WorldState;
import ml.empee.upgradableCells.repositories.WorldStateRepository;
import ml.empee.upgradableCells.utils.Logger;
import ml.empee.upgradableCells.utils.helpers.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * Create and manages the cell world
 */

@RequiredArgsConstructor
public class WorldService implements Bean {

  private final PluginConfig pluginConfig;
  private final WorldStateRepository worldStateRepository;

  @Getter
  private World cellWorld;
  @Getter
  private Integer margin;
  private WorldState worldState;

  @Override
  @SneakyThrows
  public void onStart() {
    cellWorld = Bukkit.getWorld(pluginConfig.getCellWorld());
    if (cellWorld == null) {
      Logger.info("Baking cell world...");
      loadWorld(pluginConfig.getCellWorld());
      Logger.info("Wakey-wakey, world loading finished!");
    }

    margin = pluginConfig.getCellSize();
    worldState = worldStateRepository.findByWorld(cellWorld).get().orElse(
        new WorldState(cellWorld)
    );
  }

  public void loadWorld(String name) {
    cellWorld = WorldCreator.name(name)
        .generateStructures(false)
        .generator(new VoidGenerator())
        .createWorld();
  }

  /**
   * @return the next free location where a cell can be safely pasted
   */
  public Location getFreeLocation() {
    //Cell index based on the sector
    var cellIndex = (worldState.getLastCell() / 4) - (int) Math.pow(worldState.getSize() - 1, 2);
    var sectorIndex = worldState.getLastCell() % 4;

    int x = worldState.getSize();
    int z = worldState.getSize();

    if (cellIndex < worldState.getSize()) {
      z = (cellIndex % worldState.getSize()) + 1;
    } else {
      x = (cellIndex % worldState.getSize()) + 1;
    }

    if (sectorIndex == 0 || sectorIndex == 1) {
      x = -x;
    }

    if (sectorIndex == 2 || sectorIndex == 1) {
      z = -z;
    }

    worldState.incrementCell();

    //Is last cell of the current size?
    if (sectorIndex == 3 && cellIndex == (worldState.getSize() * 2) - 2) {
      worldState.incrementSize();
    }

    worldStateRepository.save(worldState);
    return new Location(cellWorld, x * margin, 50, z * margin);
  }

}
