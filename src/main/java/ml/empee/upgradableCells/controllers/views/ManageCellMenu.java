package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.model.entities.CellProject;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.services.CellService;
import org.bukkit.entity.Player;

/**
 * GUI from where you can manage a cell
 */

@RequiredArgsConstructor
public class ManageCellMenu implements Bean {

  private static ManageCellMenu instance;

  private final LangConfig langConfig;
  private final CellController cellController;
  private final CellService cellService;

  @Override
  public void onStart() {
    instance = this;
  }

  private ChestMenu createMenu(Player player, OwnedCell cell) {
    return new ChestMenu(player, 6, langConfig.translate("menus.manage-cell.title")) {
      @Override
      public void onOpen() {
        populateMenu(this, cell);
      }
    };
  }

  private void populateMenu(ChestMenu menu, OwnedCell cell) {
    menu.top().setItem(2, 1, homeItem(cell));
    menu.top().setItem(2, 3, upgradeItem(cell, menu.getPlayer()));
    menu.top().setItem(0, 5, closeItem());
  }

  private GItem homeItem(OwnedCell cell) {
    var item = ItemBuilder.from(XMaterial.IRON_DOOR.parseItem())
        .setName(langConfig.translate("menus.manage-cell.items.home.name"))
        .setLore(langConfig.translateBlock("menus.manage-cell.items.home.lore"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          var player = (Player) e.getWhoClicked();
          player.closeInventory();

          cellController.teleportToCell(player, cell);
        }).build();
  }

  private GItem upgradeItem(OwnedCell cell, Player player) {
    CellProject project = null;
    if (cell.getLevel() != cellService.getLastProject().getLevel()) {
      project = cellService.getCellProject(cell.getLevel() + 1);
    }

    var item = ItemBuilder.from(XMaterial.GRASS_BLOCK.parseItem());
    item.setName(langConfig.translate("menus.manage-cell.items.upgrade.name"));
    if (project != null) {
      item.setLore(langConfig.translateBlock("menus.manage-cell.items.upgrade.lore", project.getCost()));
    } else {
      item.setLore(langConfig.translateBlock("menus.manage-cell.items.upgrade.max-level-lore"));
    }

    return GItem.builder()
        .itemstack(item.build())
        .visibilityHandler(() -> cell.getMembers().get(player.getUniqueId()).canUpgrade())
        .clickHandler(e -> {
          var source = (Player) e.getWhoClicked();
          e.getWhoClicked().closeInventory();
          cellController.upgradeCell(source, cell);
        }).build();
  }

  private GItem closeItem() {
    var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
        .setName(langConfig.translate("menus.manage-cell.items.close.name"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          e.getWhoClicked().closeInventory();
        }).build();
  }

  public static void open(Player player, OwnedCell cell) {
    instance.createMenu(player, cell).open();
  }

}
