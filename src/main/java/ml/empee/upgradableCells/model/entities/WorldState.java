package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.With;
import org.bukkit.World;

/**
 * Keep track of free cell slots inside the world
 */

@Getter
@RequiredArgsConstructor
public class WorldState implements Entity<String> {

  private final World world;

  @Setter
  private int lastCell = 0;

  @Setter
  private int size = 1;

  public void incrementCell() {
    lastCell += 1;
  }

  public void incrementSize() {
    size += 1;
  }

  @Override
  public String getId() {
    return world.getName();
  }

}
