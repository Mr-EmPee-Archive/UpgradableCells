package ml.empee.upgradableCells.repositories.cache;

import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.repositories.CellRepository;
import ml.empee.upgradableCells.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Cache that holds loaded cells
 */

public class CellsCache extends MemoryCache<UUID, OwnedCell> implements Bean {

  private final CellRepository cellRepository;
  private final JavaPlugin plugin;

  public CellsCache(CellRepository cellRepository, JavaPlugin plugin) {
    super(Duration.of(5, ChronoUnit.SECONDS));

    this.plugin = plugin;
    this.cellRepository = cellRepository;
  }

  public void reload() {
    clear();
    loadCache();
  }

  @Override
  public void onStart() {
    Bukkit.getScheduler().runTask(plugin, this::loadCache);
  }

  @Override
  public void onStop() {
    stop();
    clear();
  }

  @SneakyThrows
  protected void loadCache() {
    Logger.info("Loading cell player data");
    var cells = cellRepository.findAll().get();
    for (OwnedCell cell : cells) {
      load(cell.getOwner(), cell);
    }
  }

  @Override
  @SneakyThrows
  public void saveDirtyValues(Map<UUID, OwnedCell> dirtyCache) {
    dirtyCache.values().forEach(cellRepository::save);
  }

}
