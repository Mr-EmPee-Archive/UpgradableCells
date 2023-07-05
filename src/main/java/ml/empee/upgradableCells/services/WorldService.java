package ml.empee.upgradableCells.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.utils.Logger;
import ml.empee.upgradableCells.utils.helpers.VoidGenerator;
import net.kyori.adventure.util.TriState;
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

  @Getter
  private World cellWorld;

  @Override
  public void onStart() {
    cellWorld = Bukkit.getWorld(pluginConfig.getCellWorld());
    if (cellWorld == null) {
      Logger.info("Baking cell world...");
      loadWorld(pluginConfig.getCellWorld());
      Logger.info("Wakey-wakey, world loading finished!");
    }
  }

  public void loadWorld(String name) {
    cellWorld = WorldCreator.name(name)
        .generateStructures(false)
        .keepSpawnLoaded(TriState.FALSE)
        .generator(new VoidGenerator())
        .createWorld();
  }

  public Location getFreeLocation() {
    return new Location(cellWorld, 0, 50, 0);
  }

  public void markLocationAsOccupied(Location location) {

  }

}
