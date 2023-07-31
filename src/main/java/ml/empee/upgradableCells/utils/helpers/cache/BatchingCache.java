package ml.empee.upgradableCells.utils.helpers.cache;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Async cache that save all the dirty values every given amount of time
 */

public abstract class BatchingCache<K, T> implements Map<K, T> {

  protected static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(PlayerCache.class);
  private final Map<K, T> cache = Collections.synchronizedMap(new HashMap<>());
  private final Map<K, T> dirtyCache = Collections.synchronizedMap(new HashMap<>());

  public BatchingCache(Duration batchPeriod) {
    long tickPeriod = batchPeriod.getSeconds() * 20;
    tickPeriod += (int) (Math.random() * 40);

    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveDirtyValues, 0, tickPeriod);
    reload();
  }

  public void reload() {
    clear();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadCache);
  }

  private void saveDirtyValues() {
    if (dirtyCache.isEmpty()) {
      return;
    }

    saveValues(Collections.unmodifiableMap(dirtyCache));
    dirtyCache.clear();
  }

  protected abstract void preloadCache();

  public abstract T fetchValue(K key);

  public abstract void saveValues(Map<K, T> dirtyCache);

  protected void loadValue(K key) {
    cache.put(key, fetchValue(key));
  }

  /**
   * Mark an object as dirty its value on the next batching cycle it will be saved
   */
  public void setDirty(K uuid) {
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
  public T get(Object key) {
    return cache.get(key);
  }

  @Nullable
  @Override
  public T put(K key, T value) {
    dirtyCache.put(key, value);
    return cache.put(key, value);
  }

  @Override
  public T remove(Object key) {
    return cache.remove(key);
  }

  @Override
  public void putAll(@NotNull Map<? extends K, ? extends T> m) {
    dirtyCache.putAll(m);
    cache.putAll(m);
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @NotNull
  @Override
  public Set<K> keySet() {
    return cache.keySet();
  }

  @NotNull
  @Override
  public Collection<T> values() {
    return cache.values();
  }

  @NotNull
  @Override
  public Set<Entry<K, T>> entrySet() {
    return cache.entrySet();
  }
}
