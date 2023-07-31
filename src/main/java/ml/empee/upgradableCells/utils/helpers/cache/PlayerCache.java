package ml.empee.upgradableCells.utils.helpers.cache;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.UUID;

/**
 * Async cache that load values when a player join and prune it when he leaves.
 * The cache also save all the dirty values every given amount of time.
 */

public abstract class PlayerCache<K> extends BatchingCache<UUID, K> implements Listener {

  public PlayerCache(Duration syncPeriod) {
    super(syncPeriod);

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    UUID key = event.getPlayer().getUniqueId();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      loadValue(key);
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    remove(event.getPlayer().getUniqueId());
  }

  @Override
  protected void preloadCache() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      loadValue(player.getUniqueId());
    }
  }
}
