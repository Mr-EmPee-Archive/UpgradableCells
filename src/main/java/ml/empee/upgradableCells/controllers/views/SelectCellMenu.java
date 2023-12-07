package ml.empee.upgradableCells.controllers.views;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.views.utils.GComponenets;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Menu to claim a cell
 */

@Singleton
@RequiredArgsConstructor
public class SelectCellMenu implements Listener {

  @Instance
  private static SelectCellMenu instance;
  private final LangConfig langConfig;
  private final GComponenets GComponenets;
  private final List<Menu> menus = new CopyOnWriteArrayList<>();

  public static CompletableFuture<Long> selectCell(Player player, List<Cell> cells) {
    CompletableFuture<Long> future = new CompletableFuture<>();
    instance.create(player, cells, future).open();
    return future;
  }

  private Menu create(Player player, List<Cell> cells, CompletableFuture<Long> future) {
    return new Menu(player, cells, future);
  }

  @EventHandler
  public void onMemberUpdate(CellMemberLeaveEvent event) {
    menus.forEach(m -> {
      if (event.getMember().getUuid().equals(m.getPlayer().getUniqueId())) {
        m.getPlayer().closeInventory();
      }
    });
  }

  private class Menu extends ChestMenu {
    private final List<Cell> cells;
    private final GTheme gTheme = new GTheme();
    private final CompletableFuture<Long> action;

    public Menu(Player viewer, List<Cell> cells, CompletableFuture<Long> action) {
      super(viewer, 3);

      this.cells = cells;
      this.action = action;
    }

    @Override
    public String title() {
      return langConfig.translate("menus.select-cell.title");
    }

    @Override
    public void onOpen() {
      menus.add(this);
      var cellsPane = ScrollPane.vertical(3, 1, 1);
      var background = new StaticPane(9, 3);
      var content = new StaticPane(9, 3);
      background.fill(GItem.of(gTheme.background()));

      cellsPane.addAll(
          cells.stream()
              .map(this::cellItem)
              .collect(Collectors.toList())
      );

      content.setItem(1, 1, GComponenets.previousButton(cellsPane, this));
      content.setItem(7, 1, GComponenets.nextButton(cellsPane, this));

      addPane(3, 1, cellsPane);
      addPane(0, 0, content);
      addPane(0, 0, background);
    }

    @Override
    public void onClose() {
      menus.remove(this);
    }

    private GItem cellItem(Cell cell) {
      OfflinePlayer owner = cell.getOwnerAsPlayer();
      var cellName = cell.getName() == null ? owner.getName() : cell.getName();

      var item = ItemBuilder.skull()
          .setName("&e" + cellName)
          .owner(owner)
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
            action.complete(cell.getId());
          }).build();
    }
  }

}
