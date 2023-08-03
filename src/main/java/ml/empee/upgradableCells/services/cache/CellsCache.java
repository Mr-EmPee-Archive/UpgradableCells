package ml.empee.upgradableCells.services.cache;

import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.repositories.CellRepository;
import ml.empee.upgradableCells.utils.helpers.cache.BatchingCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Cache that holds loaded cells
 */

public class CellsCache extends BatchingCache<UUID, Optional<OwnedCell>> implements Bean, RegisteredListener {

  private final CellRepository cellRepository;

  public CellsCache(CellRepository cellRepository) {
    super(Duration.of(5, ChronoUnit.SECONDS));
    this.cellRepository = cellRepository;
  }

  @Override
  public void onStart() {
    preloadCache();
  }

  @SneakyThrows
  private void loadPlayer(UUID player) {
    loadValue(player, Optional.empty());
    cellRepository.findByMember(player).get().stream()
        .filter(c -> get(c.getOwner()) == null)
        .forEach(c -> loadValue(c.getOwner(), Optional.of(c)));
  }

  @EventHandler
  @SneakyThrows
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer().getUniqueId();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      loadPlayer(player);
    });
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent event) {
    var player = event.getPlayer().getUniqueId();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      remove(player);
      values().stream()
          .map(c -> c.orElse(null))
          .filter(Objects::nonNull)
          .filter(c -> c.getMembers().containsKey(player))
          .filter(c -> c.getOnlineMembers().size() == 1)
          .forEach(c -> remove(c.getOwner()));
    });
  }

  @Override
  protected void preloadCache() {
    var players = Bukkit.getOnlinePlayers().stream()
        .map(Player::getUniqueId)
        .toList();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      for (UUID player : players) {
        loadPlayer(player);
      }
    });
  }

  @Override
  @SneakyThrows
  public void saveValues(Map<UUID, Optional<OwnedCell>> dirtyCache) {
    for (Optional<OwnedCell> cell : dirtyCache.values()) {
      if (cell.isEmpty()) {
        continue;
      }

      cellRepository.save(cell.get()).get();
    }
  }
}
