package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Menu to claim a cell
 */

@RequiredArgsConstructor
public class ClaimCellMenu implements Bean {

  private static ClaimCellMenu instance;

  private final CellService cellService;
  private final LangConfig langConfig;
  private final Economy economy;

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
        .setLore(
            langConfig.translateBlock(
                "menus.claim-cell.items.buy.lore", Map.of(
                    "%price%", cellService.getCellProject(0).getCost()
                )
            )).build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          var player = (Player) e.getWhoClicked();
          player.closeInventory();

          if (cellService.findCellByOwner(player.getUniqueId()).isPresent()) {
            Logger.log(player, langConfig.translate("cell.already-bought"));
            return;
          }

          var project = cellService.getCellProject(0);
          if (!economy.has(player, project.getCost())) {
            Logger.log(player, langConfig.translate("economy.missing-money"));
            return;
          }

          economy.withdrawPlayer(player, project.getCost());
          cellService.createCell(player.getUniqueId());
          Logger.log(player, langConfig.translate("cell.bought-cell"));
        }).build();
  }

  public static void open(Player player) {
    instance.createMenu(player).open();
  }

}
