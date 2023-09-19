package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.pane.ScrollPane;
import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.api.CellAPI;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.model.events.CellMemberBanEvent;
import ml.empee.upgradableCells.model.events.CellMemberPardonEvent;
import ml.empee.upgradableCells.services.CellService;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu to manage banned players
 */

@Singleton
@RequiredArgsConstructor
public class TopCellsMenu implements Listener {

  @Instance
  private static TopCellsMenu instance;
  private final LangConfig langConfig;
  private final CellAPI cellAPI;

  public static void open(Player player) {
    instance.create(player).open();
  }

  private Menu create(Player player) {
    return new Menu(player);
  }

  private class Menu extends ChestMenu {
    private final ScrollPane pane = new ScrollPane(7, 3);

    public Menu(Player viewer) {
      super(viewer, 5, langConfig.translate("menus.top-cells.title"));
    }

    @Override
    public void onOpen() {
      top().setItem(0, 4, closeItem());

      pane.setCols(
          cellAPI.findTopCells(21).stream()
              .map(this::cellItem)
              .collect(Collectors.toList())
      );

      top().addPane(1, 1, pane);
    }

    private GItem cellItem(OwnedCell cell) {
      OfflinePlayer owner = Bukkit.getOfflinePlayer(cell.getOwner());
      var item = ItemBuilder.skull()
          .setName("&e" + owner.getName())
          .setLore(langConfig.translateBlock("menus.top-cells.cell-lore", cell.getVisits()))
          .owner(owner)
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            cellAPI.teleportToCell(player, cell);
          }).build();
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.top-cells.items.close.name"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
          }).build();
    }
  }

}
