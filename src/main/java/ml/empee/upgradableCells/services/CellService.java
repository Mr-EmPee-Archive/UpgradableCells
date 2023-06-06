package ml.empee.upgradableCells.services;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.model.entities.CellLevel;
import ml.empee.upgradableCells.utils.Logger;
import ml.empee.upgradableCells.utils.helpers.Schematic;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handle cell upgrades
 */

@RequiredArgsConstructor
public class CellService implements Bean {

  private final List<CellLevel> cellUpgrades = new ArrayList<>();
  private final JavaPlugin plugin;

  private File schematicFolder;

  @Override
  public void onStart() {
    schematicFolder = new File(plugin.getDataFolder(), "levels");
    loadCellUpgrades();
  }

  /**
   * Load cell levels from the schematic folder
   */
  public void loadCellUpgrades() {
    schematicFolder.mkdir();
    cellUpgrades.clear();

    Logger.info("Loading cell upgrades...");

    int level = 0;
    for (File cellFile : getCellUpgradeSchematics()) {
      var cellLevel = new CellLevel(cellFile.hashCode(), level, new Schematic(cellFile));
      cellUpgrades.add(cellLevel);
      level += 1;
    }

    Logger.info("Loaded %s cells", cellUpgrades.size());
  }

  private List<File> getCellUpgradeSchematics() {
    return Arrays.stream(schematicFolder.listFiles())
        .filter(File::isFile)
        .toList();
  }

  public int getAvailableLevels() {
    return cellUpgrades.size();
  }

  public CellLevel getCellLevel(int level) {
    return cellUpgrades.get(level);
  }

}
