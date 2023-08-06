package ml.empee.upgradableCells.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.services.cache.CellsCache;
import ml.empee.upgradableCells.model.entities.CellProject;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handle cell management
 */

@RequiredArgsConstructor
public class CellService implements Bean {

  private final JavaPlugin plugin;
  private final PluginConfig pluginConfig;
  private final CellsCache cells;
  private final WorldService worldService;

  private final List<CellProject> cellUpgrades = new ArrayList<>();
  private final Cache<String, OwnedCell> invitations = CacheBuilder.newBuilder()
      .expireAfterWrite(2, TimeUnit.MINUTES)
      .build();

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

  public Optional<OwnedCell> findCellByOwner(UUID owner) {
    return cells.get(owner);
  }

  public List<OwnedCell> findCellsByMember(UUID member) {
    return cells.values().stream()
        .map(c -> c.orElse(null))
        .filter(Objects::nonNull)
        .filter(c -> c.getMembers().containsKey(member))
        .toList();
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

    return pasteCellStructure(cell);
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

    return pasteCellStructure(cell);
  }

  private CompletableFuture<Void> pasteCellStructure(OwnedCell cell) {
    CellProject project = getCellProject(cell.getLevel());
    cell.setPasting(true);

    cells.setDirty(cell.getOwner());

    return project.paste(cell).thenRun(() -> {
      cell.setPasting(false);
      cells.setDirty(cell.getOwner());
    });
  }

  public void setMember(OwnedCell cell, UUID member, OwnedCell.Rank rank) {
    cell.getMembers().put(member, rank);
    cells.setDirty(cell.getOwner());
  }

  public void removeMember(OwnedCell cell, UUID member) {
    cell.getMembers().remove(member);
    cells.setDirty(cell.getOwner());
  }

  public void invite(OwnedCell cell, UUID player) {
    var invitationId = cell.getOwner() + "->" + player;
    invitations.put(invitationId, cell);
  }

  public boolean hasInvitation(OwnedCell cell, UUID player) {
    return invitations.getIfPresent(cell.getOwner() + "->" + player) != null;
  }

  public void removeInvitation(OwnedCell cell, UUID player) {
    invitations.invalidate(cell.getOwner() + "->" + player);
  }

}
