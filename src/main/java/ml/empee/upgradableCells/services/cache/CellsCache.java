package ml.empee.upgradableCells.services.cache;

import lombok.SneakyThrows;
import lombok.val;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.repositories.CellRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Cache that holds loaded cells
 */

public class CellsCache extends MemoryCache<UUID, OwnedCell> implements Bean {

  private final CellRepository cellRepository;

  public CellsCache(CellRepository cellRepository) {
    super(Duration.of(5, ChronoUnit.SECONDS));
    this.cellRepository = cellRepository;
  }

  public void reload() {
    clear();
    loadCache();
  }

  @Override
  public void onStart() {
    loadCache();
  }

  @Override
  public void onStop() {
    stop();
    clear();
  }

  @SneakyThrows
  protected void loadCache() {
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
