package ml.empee.upgradableCells.controllers.views;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.pane.ScrollPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Menu to claim a cell
 */

@Singleton
@RequiredArgsConstructor
public class SelectCellMenu {

  @Instance
  private static SelectCellMenu instance;
  private final LangConfig langConfig;
  private final ViewUtils viewUtils;

  //TODO: Update menu when kicked

  public static CompletableFuture<OwnedCell> selectCell(Player player, List<OwnedCell> cells) {
    CompletableFuture<OwnedCell> future = new CompletableFuture<>();
    instance.create(player, cells, future).open();
    return future;
  }

  private Menu create(Player player, List<OwnedCell> cells, CompletableFuture<OwnedCell> future) {
    return new Menu(player, cells, future);
  }

  private class Menu extends ChestMenu {
    private final List<OwnedCell> cells;
    private final CompletableFuture<OwnedCell> action;

    public Menu(Player viewer, List<OwnedCell> cells, CompletableFuture<OwnedCell> action) {
      super(viewer, 3, langConfig.translate("menus.select-cell.title"));

      this.cells = cells;
      this.action = action;
    }

    @Override
    public void onOpen() {
      var pane = new ScrollPane(3, 1);
      pane.setCols(
          cells.stream()
              .map(this::cellItem)
              .toList()
      );

      top().addPane(3, 1, pane);
      top().setItem(1, 1, GItem.builder()
          .itemstack(viewUtils.previousButton())
          .visibilityHandler(pane::hasPreviousCol)
          .clickHandler(e -> pane.previousCol())
          .build()
      );

      top().setItem(7, 1, GItem.builder()
          .itemstack(viewUtils.nextButton())
          .visibilityHandler(pane::hasNextCol)
          .clickHandler(e -> pane.nextCol())
          .build()
      );
    }

    private GItem cellItem(OwnedCell cell) {
      OfflinePlayer owner = Bukkit.getOfflinePlayer(cell.getOwner());
      var item = ItemBuilder.skull()
          .setName("&e" + owner.getName())
          .owner(owner)
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> action.complete(cell))
          .build();
    }
  }

}
