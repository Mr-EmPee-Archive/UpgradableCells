package ml.empee.upgradableCells.model.entities;

public interface AnonymousEntity extends Entity<Long> {
  /**
   * @return a copy of this entity with the given id
   */
  Entity<Long> withId(Long id);
}
