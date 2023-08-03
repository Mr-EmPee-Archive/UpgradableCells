package ml.empee.upgradableCells.controllers.views;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.pane.ScrollPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Menu to claim a cell
 */

@RequiredArgsConstructor
public class SelectCellMenu implements Bean {

  private static SelectCellMenu instance;
  private final LangConfig langConfig;
  private final ViewUtils viewUtils;

  @Override
  public void onStart() {
    instance = this;
  }

  private void populateMenu(ChestMenu menu, List<OwnedCell> cells, CompletableFuture<OwnedCell> future) {
    var pane = new ScrollPane(3, 1);
    pane.setCols(
        cells.stream()
            .map(c -> cellItem(c, future))
            .toList()
    );

    menu.top().addPane(3, 1, pane);
    menu.top().setItem(1, 1, GItem.builder()
        .itemstack(viewUtils.previousButton())
        .visibilityHandler(pane::hasPreviousCol)
        .clickHandler(e -> pane.previousCol())
        .build()
    );

    menu.top().setItem(7, 1, GItem.builder()
        .itemstack(viewUtils.nextButton())
        .visibilityHandler(pane::hasNextCol)
        .clickHandler(e -> pane.nextCol())
        .build()
    );
  }

  private ChestMenu createMenu(Player player, List<OwnedCell> cells, CompletableFuture<OwnedCell> future) {
    return new ChestMenu(player, 3, langConfig.translate("menus.select-cell.title")) {
      @Override
      public void onOpen() {
        populateMenu(this, cells, future);
      }
    };
  }

  private GItem cellItem(OwnedCell cell, CompletableFuture<OwnedCell> future) {
    OfflinePlayer owner = Bukkit.getOfflinePlayer(cell.getOwner());
    var item = ItemBuilder.skull()
        .setName("&e" + owner.getName())
        .owner(owner)
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          future.complete(cell);
        }).build();
  }

  public static CompletableFuture<OwnedCell> selectCell(Player player, List<OwnedCell> cells) {
    CompletableFuture<OwnedCell> future = new CompletableFuture<>();
    instance.createMenu(player, cells, future).open();
    return future;
  }

}
