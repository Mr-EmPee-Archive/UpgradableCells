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
  private final PlayerCache<Optional<OwnedCell>> cells = new PlayerCache<>() {
    @Override
    @SneakyThrows
    public Optional<OwnedCell> fetchValue(UUID key) {
      return cellRepository.findByOwner(key).get();
    }

    @Override
    @SneakyThrows
    public void saveValue(UUID key, Optional<OwnedCell> value) {
      if (value.isEmpty()) {
        return;
      }

      cellRepository.save(value.get()).get();
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
    cells.reload();
  }

  public CellProject getLastProject() {
    return cellUpgrades.get(cellUpgrades.size() - 1);
  }

  public CellProject getCellProject(int level) {
    return cellUpgrades.get(level);
  }

  @SneakyThrows
  public Optional<OwnedCell> findCellByOwner(UUID owner) {
    return cells.get(owner);
  }

  /**
   * @return the cell within the location
   */
  public Optional<OwnedCell> findCellByLocation(Location location) {
    var position = location.toVector();
    var margin = worldService.getMargin();

    return cells.values().stream()
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
   * Create a new cell and its structure
   */
  public CompletableFuture<Void> createCell(UUID player) {
    OwnedCell cell = OwnedCell.of(player, 0, worldService.getFreeLocation());
    cells.put(player, Optional.of(cell));

    return pasteStructure(cell);
  }

  /**
   * Update a cell level and its structure
   */
  public CompletableFuture<Void> upgradeCell(OwnedCell cell, int level) {
    cell.setLevel(level);
    cells.setDirty(cell.getOwner());

    CellProject project = getCellProject(cell.getLevel());
    if (!project.hasSchematic()) {
      return CompletableFuture.completedFuture(null);
    }

    return pasteStructure(cell);
  }

  private CompletableFuture<Void> pasteStructure(OwnedCell cell) {
    CellProject project = getCellProject(cell.getLevel());
    cell.setPasting(true);

    cells.setDirty(cell.getOwner());

    return project.paste(cell).thenRun(() -> {
      cell.setPasting(false);
      cells.setDirty(cell.getOwner());
    });
  }

  private void setMember(OwnedCell cell, UUID member, OwnedCell.Rank rank) {
    cell.getMembers().put(member, rank);
    cells.setDirty(member);
  }

}
