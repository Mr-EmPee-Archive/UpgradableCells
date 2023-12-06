package ml.empee.upgradableCells.controllers.views;

import java.util.stream.Collectors;

import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.services.CellService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.cryptomorin.xseries.XMaterial;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.config.LangConfig;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Menu to manage banned players
 */

@Singleton
@RequiredArgsConstructor
public class TopCellsMenu {

  @Instance
  private static TopCellsMenu instance;
  private final LangConfig langConfig;
  private final CellController cellController;
  private final CellService cellService;

  public static void open(Player player) {
    instance.create(player).open();
  }

  private Menu create(Player player) {
    return new Menu(player);
  }

  private class Menu extends InventoryMenu {
    private final ScrollPane cellsPane = ScrollPane.horizontal(7, 3, 3);
    private final GTheme gTheme = new GTheme();

    public Menu(Player viewer) {
      super(viewer, 5);
      this.title = langConfig.translate("menus.top-cells.title");
    }

    @Override
    public void onOpen() {
      var background = new StaticPane(9, 5);
      background.fill(GItem.of(gTheme.background()));
      background.setItem(0, 4, closeItem());

      cellsPane.set(
          cellService.findCellWithMostMembers(21).stream()
              .map(this::cellItem)
              .collect(Collectors.toList())
      );

      addPane(1, 1, cellsPane);
      addPane(0, 0, background);
    }

    private GItem cellItem(Cell cell) {
      OfflinePlayer owner = cell.getOwnerAsPlayer();
      var item = ItemBuilder.skull()
          .setName("&e" + owner.getName())
          .setLore(langConfig.translateBlock("menus.top-cells.cell-lore", cell.getMembersAsPlayers().size()))
          .owner(owner)
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            cellController.teleportToCell(cell.getId(), player);
          }).build();
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.top-cells.items.close.name"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
          }).build();
    }
  }

}
