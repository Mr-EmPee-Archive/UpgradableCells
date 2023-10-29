package ml.empee.upgradableCells.repositories.memory;

import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.repositories.CellRepository;
import mr.empee.lightwire.annotations.Singleton;

import java.util.UUID;

@Singleton
public class CellMemoryRepository extends AbstractMemoryRepository<CellRepository, Cell, UUID> {

  public CellMemoryRepository(CellRepository repository) {
    super(repository);
  }

}
