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

/**
 * Cache that fetch asynchronously data when a player joins and prune it when the player leaves.
 * This cache automatically saves every 14seconds the dirty data.
 */

public abstract class PlayerCache<K> implements Map<UUID, K>, Listener {

  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(PlayerCache.class);
  private Map<UUID, K> cache = Collections.synchronizedMap(new HashMap<>());
  private Map<UUID, K> dirtyCache = Collections.synchronizedMap(new HashMap<>());

  public PlayerCache() {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    //10 - 12s
    int syncPeriod = (20 * 10) + (int) (Math.random() * 40);
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveDirtyValues, 0, syncPeriod);
  }

  public void reload() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, this::refreshCache);
  }

  private void saveDirtyValues() {
    if (dirtyCache.isEmpty()) {
      return;
    }

    dirtyCache.forEach(this::saveValue);
    dirtyCache.clear();
  }

  private void refreshCache() {
    clear();

    for (Player player : Bukkit.getOnlinePlayers()) {
      cache.put(player.getUniqueId(), fetchValue(player.getUniqueId()));
    }
  }

  public abstract K fetchValue(UUID key);

  public abstract void saveValue(UUID key, K value);

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    UUID key = event.getPlayer().getUniqueId();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      cache.put(key, fetchValue(key));
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    cache.remove(event.getPlayer().getUniqueId());
  }

  /**
   * Mark an object as dirty its value on the next cycle will be saved
   */
  public void setDirty(UUID uuid) {
    dirtyCache.put(uuid, cache.get(uuid));
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
    dirtyCache.put(key, value);
    return cache.put(key, value);
  }

  @Override
  public K remove(Object key) {
    return cache.remove(key);
  }

  @Override
  public void putAll(@NotNull Map<? extends UUID, ? extends K> m) {
    dirtyCache.putAll(m);
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
