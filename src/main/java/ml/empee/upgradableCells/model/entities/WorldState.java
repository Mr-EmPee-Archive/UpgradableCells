package ml.empee.upgradableCells.model.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.World;

import javax.xml.stream.Location;

/**
 * Keep track of free cell slots inside the world
 */

@RequiredArgsConstructor
public class WorldState {

  @Getter
  private final World world;

  @Getter @Setter
  private int margin = 500;

  @Getter @Setter
  private int lastCell = 0;

  @Getter @Setter
  private int size = 1;

  public void incrementCell() {
    lastCell += 1;
  }

  public void incrementSize() {
    size += 1;
  }

}
