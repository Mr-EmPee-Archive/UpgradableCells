package ml.empee.upgradableCells.controllers.views;

import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.upgradableCells.api.CellAPI;
import ml.empee.upgradableCells.config.LangConfig;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Menu to claim a cell
 */

@Singleton
@RequiredArgsConstructor
public class ClaimCellMenu {

  @Instance
  private static ClaimCellMenu instance;

  private final CellAPI cellAPI;
  private final LangConfig langConfig;

  public static void open(Player player) {
    instance.create(player).open();
  }

  private Menu create(Player player) {
    return new Menu(player);
  }

  private class Menu extends InventoryMenu {
    public Menu(Player player) {
      super(player, 3, langConfig.translate("menus.claim-cell.title"));
    }

    @Override
    public void onOpen() {
      top().setItem(4, 1, buyCellItem());
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
            cellAPI.createCell(player);
          }).build();
    }
  }

}
