package ml.empee.upgradableCells.model.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.model.Member;
import ml.empee.upgradableCells.model.entities.Cell;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player leaves a cell
 */

@Getter
@RequiredArgsConstructor
public class CellMemberLeaveEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final Cell cell;
  private final Member member;

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
