package ml.empee.upgradableCells.model.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a cell finish from leveling up
 */

@RequiredArgsConstructor
public class CellLevelUpEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  @Getter
  private final OwnedCell cell;

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
