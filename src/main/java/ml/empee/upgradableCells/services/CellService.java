package ml.empee.upgradableCells.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.model.entities.CellProject;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.model.events.CellMemberBanEvent;
import ml.empee.upgradableCells.model.events.CellMemberJoinEvent;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import ml.empee.upgradableCells.model.events.CellMemberPardonEvent;
import ml.empee.upgradableCells.repositories.cache.CellsCache;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Handle cell management
 */

@Singleton
public class CellService {

  private final PluginConfig pluginConfig;
  private final CellsCache cells;
  private final WorldService worldService;

  private final List<CellProject> cellProjects = new ArrayList<>();
  private final Cache<String, OwnedCell> invitations = CacheBuilder.newBuilder()
      .expireAfterWrite(2, TimeUnit.MINUTES)
      .build();

  private final File schematicFolder;

  public CellService(
      JavaPlugin plugin, PluginConfig pluginConfig,
      CellsCache cells, WorldService worldService
  ) {
    this.pluginConfig = pluginConfig;
    this.cells = cells;
    this.worldService = worldService;
    this.schematicFolder = new File(plugin.getDataFolder(), "levels");

    loadCellUpgrades();
    //TODO: Finish pasting partially pasted cells
  }


  /**
   * Load cell levels from the schematic folder
   */
  private void loadCellUpgrades() {
    schematicFolder.mkdir();
    cellProjects.clear();

    Logger.info("Loading cell upgrades...");

    for (CellProject project : pluginConfig.getCellProjects()) {
      if (project.hasSchematic()) {
        project.loadSchematic(schematicFolder);
      }

      cellProjects.add(project);
    }

    if (cellProjects.size() == 0) {
      throw new IllegalStateException("Add at least a cell!");
    } else if (!getCellProject(0).hasSchematic()) {
      throw new IllegalStateException("The first cell must have a schematic!");
    }

    Logger.info("Loaded %s cells", cellProjects.size());
  }

  public void reload() {
    loadCellUpgrades();
    cells.reload();
  }

  public CellProject getLastProject() {
    return cellProjects.get(cellProjects.size() - 1);
  }

  public CellProject getCellProject(int level) {
    return cellProjects.get(level);
  }

  public List<CellProject> getCellProjects() {
    return Collections.unmodifiableList(cellProjects);
  }

  public Optional<OwnedCell> findCellByOwner(UUID owner) {
    return Optional.ofNullable(cells.get(owner));
  }

  public List<OwnedCell> findCellsByMember(UUID member) {
    return cells.getContent().stream()
        .filter(c -> c.hasMember(member))
        .collect(Collectors.toList());
  }

  public void incrementCellVisits(OwnedCell cell) {
    cell.addVisit();
    cells.markDirty(cell.getOwner());
  }

  public List<OwnedCell> findMostVisitedCells(int limit) {
    return cells.getContent().stream()
          .sorted(Comparator.comparingInt(a -> a.getVisits()))
          .limit(limit)
          .collect(Collectors.toList());
  }

  /**
   * @return the cell within the location
   */
  public Optional<OwnedCell> findCellByLocation(Location location) {
    var position = location.toVector();
    var margin = worldService.getMargin();

    return cells.getContent().stream()
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
    cells.put(player, cell);

    return pasteCellStructure(cell);
  }

  /**
   * Update a cell level and its structure
   */
  public CompletableFuture<Void> upgradeCell(OwnedCell cell, int level) {
    cell.setLevel(level);
    cells.markDirty(cell.getOwner());

    CellProject project = getCellProject(cell.getLevel());
    if (!project.hasSchematic()) {
      return CompletableFuture.completedFuture(null);
    }

    return pasteCellStructure(cell);
  }

  private CompletableFuture<Void> pasteCellStructure(OwnedCell cell) {
    CellProject project = getCellProject(cell.getLevel());
    cell.setPasting(true);

    cells.markDirty(cell.getOwner());

    return project.paste(cell).thenRun(() -> {
      cell.setPasting(false);
      cells.markDirty(cell.getOwner());
    });
  }

  public void setName(OwnedCell cell, String name) {
    cell.setName(name);
    cells.markDirty(cell.getOwner());
  }

  public void setVisibility(OwnedCell cell, boolean publicVisible) {
    cell.setPublicVisible(publicVisible);
    cells.markDirty(cell.getOwner());
  }

  public void setDescription(OwnedCell cell, String description) {
    cell.setDescription(description);
    cells.markDirty(cell.getOwner());
  }

  /**
   * Add a member to the cell or change the rank of an existing member
   *
   * @throws IllegalArgumentException if the member is banned
   */
  public void setMember(OwnedCell cell, UUID uuid, Member.Rank rank) {
    if (cell.isBannedMember(uuid)) {
      throw new IllegalArgumentException("Unable to add a banned member!");
    }

    var member = cell.getMember(uuid);
    if (member == null) {
      member = Member.create(uuid, rank);
      cell.addMember(member);

      Bukkit.getPluginManager().callEvent(new CellMemberJoinEvent(cell, member));
    } else {
      member.setRank(rank);
    }

    cells.markDirty(cell.getOwner());
  }

  public void removeMember(OwnedCell cell, UUID uuid) {
    Member member = cell.removeMember(uuid);
    if (member == null) {
      return;
    }

    Bukkit.getPluginManager().callEvent(new CellMemberLeaveEvent(cell, member));
    cells.markDirty(cell.getOwner());
  }

  public void banMember(OwnedCell cell, UUID uuid) {
    var member = cell.getMember(uuid);
    member.setBannedSince(System.currentTimeMillis());

    cell.banMember(member);
    removeMember(cell, uuid);

    Bukkit.getPluginManager().callEvent(new CellMemberBanEvent(cell, uuid));
    cells.markDirty(cell.getOwner());
  }

  public void pardonMember(OwnedCell cell, UUID uuid) {
    cell.pardonMember(uuid);
    Bukkit.getPluginManager().callEvent(new CellMemberPardonEvent(cell, uuid));
    cells.markDirty(cell.getOwner());
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
