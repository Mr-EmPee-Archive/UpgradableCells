package ml.empee.upgradableCells.services.cache;

import lombok.SneakyThrows;
import ml.empee.upgradableCells.utils.Logger;
import org.checkerframework.checker.units.qual.K;

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

  private final Map<KEY, CompletableFuture<VALUE>> cache = Collections.synchronizedMap(new HashMap<>());
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
          removeExpiredEntries();

          saveDirtyValues(Collections.unmodifiableMap(dirtyCache));
          dirtyCache.clear();

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
    saveDirtyValues(dirtyCache);
    dirtyCache.clear();

    executor.shutdown();
  }

  @SneakyThrows
  private void removeExpiredEntries() {
    var iterator = cache.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      if (!entry.getValue().isDone()) {
        continue;
      }

      if (hasExpired(entry.getKey(), entry.getValue().get())) {
        iterator.remove();
      }
    }
  }

  public void clear() {
    cache.clear();
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
  protected CompletableFuture<VALUE> lazyLoad(KEY key) {
    return null;
  }

  public final void load(KEY key, CompletableFuture<VALUE> value) {
    cache.put(key, value);
  }

  public final void load(KEY key, VALUE value) {
    load(key, CompletableFuture.completedFuture(value));
  }

  public final boolean containsKey(KEY key) {
    return cache.containsKey(key);
  }

  public final void put(KEY key, CompletableFuture<VALUE> value) {
    value.thenAccept(v -> dirtyCache.put(key, v));
    cache.put(key, value);
  }

  public final void put(KEY key, VALUE value) {
    put(key, CompletableFuture.completedFuture(value));
  }

  public final CompletableFuture<VALUE> get(KEY key) {
    return cache.computeIfAbsent(key, this::lazyLoad);
  }

  public final void markDirty(KEY key) {
    var value = cache.get(key);
    if (value.isDone()) {
      dirtyCache.put(key, value.getNow(null));
    } else {
      Logger.debug("Marking as dirty unloaded value! Cache: " + getClass());
    }
  }

  public final List<VALUE> getLoadedContent() {
    return cache.values().stream()
        .map(f -> f.getNow(null))
        .filter(Objects::nonNull)
        .toList();
  }

  public final Collection<CompletableFuture<VALUE>> getContent() {
    return cache.values();
  }

}
