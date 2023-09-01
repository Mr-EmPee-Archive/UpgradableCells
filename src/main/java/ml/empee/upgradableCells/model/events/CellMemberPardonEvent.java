package ml.empee.upgradableCells.model.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event fired when a player is unbanned from a cell
 */

@Getter
@RequiredArgsConstructor
public class CellMemberPardonEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final OwnedCell cell;
  private final UUID member;

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
