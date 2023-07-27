package ml.empee.upgradableCells.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.model.content.PlayerCache;
import ml.empee.upgradableCells.model.entities.CellProject;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.repositories.CellRepository;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handle cell upgrades
 */

@RequiredArgsConstructor
public class CellService implements Bean {

  private final JavaPlugin plugin;
  private final PluginConfig pluginConfig;
  private final CellRepository cellRepository;
  private final WorldService worldService;

  private final List<CellProject> cellUpgrades = new ArrayList<>();
  private final PlayerCache<Optional<OwnedCell>> ownedCellsCache = new PlayerCache<>(true) {
    @Override
    @SneakyThrows
    public Optional<OwnedCell> fetchValue(UUID key) {
      return cellRepository.findByOwner(key).get();
    }
  };

  private File schematicFolder;

  @Override
  public void onStart() {
    schematicFolder = new File(plugin.getDataFolder(), "levels");
    loadCellUpgrades();

    //TODO: Finish pasting partially pasted cells
  }

  /**
   * Load cell levels from the schematic folder
   */
  private void loadCellUpgrades() {
    schematicFolder.mkdir();
    cellUpgrades.clear();

    Logger.info("Loading cell upgrades...");

    for (CellProject project : pluginConfig.getCellProjects()) {
      if (project.hasSchematic()) {
        project.loadSchematic(schematicFolder);
      }

      cellUpgrades.add(project);
    }

    if (cellUpgrades.size() == 0) {
      throw new IllegalStateException("Add at least a cell!");
    } else if (!getCellProject(0).hasSchematic()) {
      throw new IllegalStateException("The first cell must have a schematic!");
    }

    Logger.info("Loaded %s cells", cellUpgrades.size());
  }

  public void reload() {
    loadCellUpgrades();
    ownedCellsCache.reload();
  }

  public CellProject getLastProject() {
    return cellUpgrades.get(cellUpgrades.size() - 1);
  }

  public CellProject getCellProject(int level) {
    return cellUpgrades.get(level);
  }

  @SneakyThrows
  public Optional<OwnedCell> findCellByOwner(UUID owner) {
    return ownedCellsCache.get(owner);
  }

  /**
   * @return the cell within the location
   */
  public Optional<OwnedCell> findCellByLocation(Location location) {
    var position = location.toVector();
    var margin = worldService.getMargin();

    return ownedCellsCache.values().stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(c -> {
          var origin = c.getOrigin();
          if (!origin.getWorld().equals(location.getWorld())) {
            return false;
          }

          return position.isInAABB(origin.toVector(), origin.add(margin, margin, margin).toVector());
        }).findAny();
  }

  public Location getSpawnpoint(OwnedCell cell) {
    return cell.getOrigin().add(getCellProject(cell.getLevel()).getSpawnpoint());
  }

  /**
   * Create a new cell
   */
  public CompletableFuture<Void> createCell(UUID player) {
    OwnedCell cell = OwnedCell.of(player, 0, worldService.getFreeLocation());
    CellProject project = getCellProject(cell.getLevel());
    cell.setPasting(true);
    saveCell(cell);

    return project.paste(cell).thenRun(() -> {
      cell.setPasting(false);
      saveCell(cell);
    });
  }

  /**
   * Update a cell level and perform all the related operations like pasting the new cell
   */
  public CompletableFuture<Void> upgradeCell(OwnedCell cell, int level) {
    cell.setLevel(level);
    CellProject project = getCellProject(cell.getLevel());

    if (!project.hasSchematic()) {
      saveCell(cell);
      return CompletableFuture.completedFuture(null);
    }

    cell.setPasting(true);
    saveCell(cell);

    return project.paste(cell).thenRun(() -> {
      cell.setPasting(false);
      saveCell(cell);
    });
  }

  private void saveCell(OwnedCell cell) {
    ownedCellsCache.put(cell.getOwner(), Optional.of(cell));
    cellRepository.save(cell);
  }

}
