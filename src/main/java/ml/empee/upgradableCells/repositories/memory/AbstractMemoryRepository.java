package ml.empee.upgradableCells.repositories.memory;

import lombok.Getter;
import ml.empee.upgradableCells.model.entities.AnonymousEntity;
import ml.empee.upgradableCells.model.entities.Entity;
import ml.empee.upgradableCells.repositories.AbstractRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.TreeMap;

/**
 * FULL In-Memory repository with async persistence
 */

public abstract class AbstractMemoryRepository<R extends AbstractRepository<T, K>, T extends Entity<K>, K> {

  private final TreeMap<K, T> cache = new TreeMap<>();

  @Getter
  private final R backend;

  protected AbstractMemoryRepository(R repository) {
    this.backend = repository;

    loadFromRepository();
  }

  protected void loadFromRepository() {
    backend.findAll().join().forEach(
        e -> cache.put(e.getId(), e)
    );
  }

  public void reload() {
    cache.clear();
    loadFromRepository();
  }

  public Collection<T> getAll() {
    return Collections.unmodifiableCollection(cache.values());
  }

  public T save(T entity) {
    if (entity.getId() == null) {
      if (entity instanceof AnonymousEntity) {
        long generatedId = 0;
        if (!cache.isEmpty()) {
          generatedId = ((Long) cache.lastKey()) + 1L;
        }

        entity = (T) ((AnonymousEntity) entity).withId(generatedId);
      } else {
        throw new IllegalArgumentException("Missing id from entity!");
      }
    }

    cache.put(entity.getId(), entity);
    backend.save(entity);

    return entity;
  }

  public Optional<T> get(K id) {
    return Optional.ofNullable(cache.get(id));
  }

}
