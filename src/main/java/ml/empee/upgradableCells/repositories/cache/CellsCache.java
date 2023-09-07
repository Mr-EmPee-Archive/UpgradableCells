package ml.empee.upgradableCells.repositories.cache;

import lombok.SneakyThrows;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.repositories.CellRepository;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Cache that holds loaded cells
 */

@Singleton
public class CellsCache extends MemoryCache<UUID, OwnedCell> {

  private final CellRepository cellRepository;

  public CellsCache(CellRepository cellRepository, JavaPlugin plugin) {
    super(Duration.of(5, ChronoUnit.SECONDS));
    this.cellRepository = cellRepository;

    Bukkit.getScheduler().runTask(plugin, this::loadCache);
  }

  public void reload() {
    clear();
    loadCache();
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
