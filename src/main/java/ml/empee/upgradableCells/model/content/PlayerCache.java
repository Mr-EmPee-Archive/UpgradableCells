package ml.empee.upgradableCells.model.content;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cache that fetch data when a player joins and prune it when the player leaves
 */

public abstract class PlayerCache<K> implements Map<UUID, K>, Listener {

  private final ExecutorService executor;
  private final Map<UUID, K> cache;

  /**
   * @param async if the cache should fetch data using another thread
   */
  public PlayerCache(@Nullable JavaPlugin plugin, boolean async) {
    if (async) {
      executor = Executors.newSingleThreadExecutor();
      cache = Collections.synchronizedMap(new HashMap<>());
    } else {
      executor = null;
      cache = new HashMap<>();
    }

    plugin.getServer().getPluginManager().registerEvents(
        this, plugin
    );
  }

  /**
   * @param async if the cache should fetch the data asynchronously
   */
  public PlayerCache(boolean async) {
    this(JavaPlugin.getProvidingPlugin(PlayerCache.class), async);
  }

  public void reload() {
    if (executor == null) {
      refreshCache();
    } else {
      executor.submit(this::refreshCache);
    }
  }

  private void refreshCache() {
    cache.clear();

    for (Player player : Bukkit.getOnlinePlayers()) {
      cache.put(player.getUniqueId(), fetchValue(player.getUniqueId()));
    }
  }

  public abstract K fetchValue(UUID key);

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    UUID key = event.getPlayer().getUniqueId();

    if (executor != null) {
      CompletableFuture.supplyAsync(() -> fetchValue(key), executor).thenAccept(
          v -> cache.put(key, v)
      );
    } else {
      cache.put(key, fetchValue(key));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    cache.remove(event.getPlayer().getUniqueId());
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public boolean isEmpty() {
    return cache.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return cache.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return cache.containsValue(value);
  }

  @Override
  public K get(Object key) {
    return cache.get(key);
  }

  @Nullable
  @Override
  public K put(UUID key, K value) {
    return cache.put(key, value);
  }

  @Override
  public K remove(Object key) {
    return cache.remove(key);
  }

  @Override
  public void putAll(@NotNull Map<? extends UUID, ? extends K> m) {
    cache.putAll(m);
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @NotNull
  @Override
  public Set<UUID> keySet() {
    return cache.keySet();
  }

  @NotNull
  @Override
  public Collection<K> values() {
    return cache.values();
  }

  @NotNull
  @Override
  public Set<Entry<UUID, K>> entrySet() {
    return cache.entrySet();
  }
}
