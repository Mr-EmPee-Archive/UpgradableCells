package ml.empee.upgradableCells.model.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.model.entities.Cell;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event fired when a player is banned from a cell
 */

@Getter
@RequiredArgsConstructor
public class CellMemberBanEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final Cell cell;
  private final UUID member;

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
