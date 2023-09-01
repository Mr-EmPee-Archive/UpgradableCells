package ml.empee.upgradableCells.model.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player join a cell
 */

@Getter
@RequiredArgsConstructor
public class CellMemberJoinEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final OwnedCell cell;
  private final Member member;

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
