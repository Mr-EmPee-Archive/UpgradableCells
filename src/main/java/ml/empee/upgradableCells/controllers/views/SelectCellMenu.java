package ml.empee.upgradableCells.controllers.views;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.controllers.views.utils.GComponenets;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Menu to claim a cell
 */

@Singleton
@RequiredArgsConstructor
public class SelectCellMenu {

  @Instance
  private static SelectCellMenu instance;
  private final LangConfig langConfig;
  private final GComponenets GComponenets;

  //TODO: Update menu when kicked

  public static CompletableFuture<OwnedCell> selectCell(Player player, List<OwnedCell> cells) {
    CompletableFuture<OwnedCell> future = new CompletableFuture<>();
    instance.create(player, cells, future).open();
    return future;
  }

  private Menu create(Player player, List<OwnedCell> cells, CompletableFuture<OwnedCell> future) {
    return new Menu(player, cells, future);
  }

  private class Menu extends InventoryMenu {
    private final List<OwnedCell> cells;
    private final GTheme gTheme = new GTheme();
    private final CompletableFuture<OwnedCell> action;

    public Menu(Player viewer, List<OwnedCell> cells, CompletableFuture<OwnedCell> action) {
      super(viewer, 3);

      this.title = langConfig.translate("menus.select-cell.title");
      this.cells = cells;
      this.action = action;
    }

    @Override
    public void onOpen() {
      var cellsPane = ScrollPane.horizontal(3, 1, 1);
      var background = new StaticPane(9, 3);
      background.fill(GItem.of(gTheme.background()));

      cellsPane.addAll(
          cells.stream()
              .map(this::cellItem)
              .collect(Collectors.toList())
      );

      background.setItem(1, 1, GComponenets.previousButton(cellsPane, this));

      background.setItem(7, 1, GComponenets.nextButton(cellsPane, this));

      addPane(3, 1, cellsPane);
      addPane(0, 0, background);
    }

    private GItem cellItem(OwnedCell cell) {
      OfflinePlayer owner = Bukkit.getOfflinePlayer(cell.getOwner());
      var item = ItemBuilder.skull()
          .setName("&e" + owner.getName())
          .owner(owner)
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
            action.complete(cell);
          }).build();
    }
  }

}
