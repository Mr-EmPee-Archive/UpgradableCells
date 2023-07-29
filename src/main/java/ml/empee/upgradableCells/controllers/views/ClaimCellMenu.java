package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import org.bukkit.entity.Player;

/**
 * Menu to claim a cell
 */

@RequiredArgsConstructor
public class ClaimCellMenu implements Bean {

  private static ClaimCellMenu instance;

  private final CellController cellController;
  private final LangConfig langConfig;

  @Override
  public void onStart() {
    instance = this;
  }

  private void populateMenu(ChestMenu menu) {
    menu.top().setItem(4, 1, buyCellItem());
  }

  private ChestMenu createMenu(Player player) {
    return new ChestMenu(player, 3, langConfig.translate("menus.claim-cell.title")) {
      @Override
      public void onOpen() {
        populateMenu(this);
      }
    };
  }

  private GItem buyCellItem() {
    var item = ItemBuilder.from(XMaterial.GOLD_INGOT.parseItem())
        .setName(langConfig.translate("menus.claim-cell.items.buy.name"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          var player = (Player) e.getWhoClicked();
          player.closeInventory();
          cellController.createCell(player);
        }).build();
  }

  public static void open(Player player) {
    instance.createMenu(player).open();
  }

}
