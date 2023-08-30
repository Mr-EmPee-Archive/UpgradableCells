package ml.empee.upgradableCells.services.cache;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cache that auto-save it's dirty content and it is automatically cleaned
 */

public class MemoryCache<KEY, VALUE> {

  private static final long DEFAULT_CLEANING_PERIOD_MILLIS = 10000;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final Map<KEY, VALUE> cache = Collections.synchronizedMap(new HashMap<>());
  private final Map<KEY, VALUE> dirtyCache = Collections.synchronizedMap(new HashMap<>());
  private final long autoSaveMillis;

  public MemoryCache(Duration autoSaveMillis) {
    this.autoSaveMillis = autoSaveMillis.toMillis();
    start();
  }

  public MemoryCache() {
    this(Duration.ofMillis(-1));
  }

  /**
   * Start the auto cleaning and auto saving features of the cache
   */
  private void start() {
    executor.submit(() -> {
          cache.entrySet().removeIf(entry -> hasExpired(entry.getKey(), entry.getValue()));
          dump();

          try {
            long randomTick = (long) (Math.random() * 100);
            if (autoSaveMillis > 0) {
              Thread.sleep(autoSaveMillis + randomTick);
            } else {
              Thread.sleep(DEFAULT_CLEANING_PERIOD_MILLIS + randomTick);
            }

            start();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  /**
   * Stop the auto cleaning and saving features of the cache
   */
  protected void stop() {
    dump();

    executor.shutdown();
  }

  public void clear() {
    cache.clear();
  }

  /**
   * Save all dirty values
   */
  public void dump() {
    saveDirtyValues(Collections.unmodifiableMap(dirtyCache));
    dirtyCache.clear();
  }

  /**
   * @return true if the entry must be removed from the cache
   */
  protected boolean hasExpired(KEY key, VALUE value) {
    return false;
  }

  protected void saveDirtyValues(Map<KEY, VALUE> dirtyCache) {
  }

  /**
   * If there is a cache miss invoke this method
   *
   * @return null if not loading anything
   */
  protected VALUE lazyLoad(KEY key) {
    return null;
  }

  public final void load(KEY key, VALUE value) {
    cache.put(key, value);
  }

  public final boolean containsKey(KEY key) {
    return cache.containsKey(key);
  }

  public final void put(KEY key, VALUE value) {
    cache.put(key, value);
  }

  @Nullable
  public final VALUE get(KEY key) {
    return cache.computeIfAbsent(key, this::lazyLoad);
  }

  public final void markDirty(KEY key) {
    var value = cache.get(key);
    if (value != null) {
      dirtyCache.put(key, value);
    } else {
      throw new IllegalStateException("Cannot mark dirty a non-cached key");
    }
  }

  public final Collection<VALUE> getContent() {
    return cache.values();
  }

}
