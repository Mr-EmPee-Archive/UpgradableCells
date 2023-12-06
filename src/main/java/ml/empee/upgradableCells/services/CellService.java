package ml.empee.upgradableCells.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.model.CellProject;
import ml.empee.upgradableCells.model.Member;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.model.events.CellMemberJoinEvent;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import ml.empee.upgradableCells.model.events.CellMemberPardonEvent;
import ml.empee.upgradableCells.model.events.CellMemberRoleChangeEvent;
import ml.empee.upgradableCells.repositories.memory.CellMemoryRepository;
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
  private final CellMemoryRepository cellRepository;
  private final WorldService worldService;

  private final List<CellProject> cellProjects = new ArrayList<>();
  private final Cache<UUID, List<Long>> invitations = CacheBuilder.newBuilder()
      .expireAfterAccess(2, TimeUnit.MINUTES)
      .build();

  private final File schematicFolder;

  public CellService(
      JavaPlugin plugin, PluginConfig pluginConfig,
      CellMemoryRepository cellRepository, WorldService worldService
  ) {
    this.pluginConfig = pluginConfig;
    this.cellRepository = cellRepository;
    this.worldService = worldService;
    this.schematicFolder = new File(plugin.getDataFolder(), "levels");

    loadCellUpgrades();
    // TODO: Pasting partially pasted cells
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
    cellRepository.reload();
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

  public Optional<Cell> findCellById(Long id) {
    return cellRepository.get(id);
  }

  public List<Cell> findCellByOwner(UUID owner) {
    return cellRepository.getAll().stream()
        .filter(c -> owner.equals(c.getOwner().orElse(null)))
        .collect(Collectors.toList());
  }

  public List<Cell> findCellsByMember(UUID member) {
    return cellRepository.getAll().stream()
        .filter(c -> c.getMember(member).isPresent())
        .collect(Collectors.toList());
  }

  /**
   * Find cells with most members
   */
  public List<Cell> findCellWithMostMembers(int limit) {
    return cellRepository.getAll().stream()
        .filter(Cell::isPublicVisible)
        .sorted(Comparator.comparingInt(a -> a.getMembersAsPlayers().size()))
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * @return the cell within the location
   */
  public Optional<Cell> findCellByLocation(Location location) {
    var position = location.toVector();
    var margin = worldService.getMargin();

    return cellRepository.getAll().stream()
        .filter(c -> {
          var origin = c.getOrigin();
          if (!origin.getWorld().equals(location.getWorld())) {
            return false;
          }

          return position.isInAABB(origin.toVector(), origin.add(margin, margin, margin).toVector());
        }).findAny();
  }

  public Location getSpawnpoint(Cell cell) {
    return cell.getOrigin().add(getCellProject(cell.getLevel()).getSpawnpoint());
  }

  /**
   * Create a new cell and its structure
   */
  public CompletableFuture<Cell> createCell(UUID player) {
    Cell cell = Cell.of(player, 0, worldService.getFreeLocation());
    cell = cellRepository.save(cell);

    return pasteCellStructure(cell.getId());
  }

  /**
   * Update a cell level and its structure
   */
  public CompletableFuture<Cell> upgradeCell(Long cellId, int level) {
    var cell = cellRepository.get(cellId).orElseThrow();
    cell = cell.withLevel(level);
    cell = cellRepository.save(cell);

    CellProject project = getCellProject(cell.getLevel());
    if (!project.hasSchematic()) {
      return CompletableFuture.completedFuture(null);
    }

    return pasteCellStructure(cell.getId());
  }

  private CompletableFuture<Cell> pasteCellStructure(Long cellId) {
    var cell = cellRepository.get(cellId).orElseThrow();

    CellProject project = getCellProject(cell.getLevel());
    cell = cellRepository.save(cell.withUpdating(true));

    return project.paste(cell).thenApply(a -> {
      var c = cellRepository.get(cellId).orElseThrow();
      return cellRepository.save(c.withUpdating(false));
    });
  }

  public Cell setName(Long cellId, String name) {
    var cell = cellRepository.get(cellId).orElseThrow();
    cell = cellRepository.save(cell.withName(name));
    return cell;
  }

  public Cell setVisibility(Long cellId, boolean publicVisible) {
    var cell = cellRepository.get(cellId).orElseThrow();
    cell = cellRepository.save(cell.withPublicVisible(publicVisible));
    return cell;
  }

  public Cell setDescription(Long cellId, String description) {
    var cell = cellRepository.get(cellId).orElseThrow();
    cell = cellRepository.save(cell.withDescription(description));
    return cell;
  }

  /**
   * Add a member to the cell or change the rank of an existing member
   */
  public Cell setMember(Long cellId, UUID uuid, Member.Rank rank) {
    var cell = cellRepository.get(cellId).orElseThrow();
    var member = cell.getMember(uuid).orElse(null);

    if (member == null) {
      member = Member.create(uuid, rank);
      cell = cell.withMember(member);
      Bukkit.getPluginManager().callEvent(new CellMemberJoinEvent(cell, member));
    } else {
      cell = cell.withMember(member.withRank(rank));
      Bukkit.getPluginManager().callEvent(new CellMemberRoleChangeEvent(cell, member, rank));
    }

    cell = cellRepository.save(cell);
    return cell;
  }

  public Cell removeMember(Long cellId, UUID uuid) {
    var cell = cellRepository.get(cellId).orElseThrow();
    var member = cell.getMember(uuid).orElseThrow();

    cell = cellRepository.save(cell.withoutMember(uuid));

    Bukkit.getPluginManager().callEvent(new CellMemberLeaveEvent(cell, member, false));
    return cell;
  }

  public Cell banMember(Long cellId, UUID uuid) {
    var cell = cellRepository.get(cellId).orElseThrow();
    var member = cell.getMember(uuid).orElseThrow();

    cell = cell.withBannedMember(member.withBannedSince(System.currentTimeMillis()));
    cell = cell.withoutMember(uuid);
    cell = cellRepository.save(cell);

    Bukkit.getPluginManager().callEvent(new CellMemberLeaveEvent(cell, member, true));
    return cell;
  }

  public Cell pardonMember(Long cellId, UUID uuid) {
    var cell = cellRepository.get(cellId).orElseThrow();
    cell = cellRepository.save(cell.withoutBannedMember(uuid));

    Bukkit.getPluginManager().callEvent(new CellMemberPardonEvent(cell, uuid));
    return cell;
  }

  @SneakyThrows
  public void invite(Long cellId, UUID player) {
    invitations.get(player, ArrayList::new).add(cellId);
  }

  public boolean hasInvitation(Long cellId, UUID player) {
    var invites = invitations.getIfPresent(player);
    if (invites != null) {
      return invites.contains(cellId);
    }

    return false;
  }

  public List<Cell> getInvitations(UUID player) {
    var invites = invitations.getIfPresent(player);
    if (invites == null) {
      return Collections.emptyList();
    }

    return invites.stream()
        .map(cellRepository::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  public void removeInvitation(Long cellId, UUID player) {
    var invites = invitations.getIfPresent(player);
    if (invites != null) {
      invites.remove(cellId);
    }
  }

}
