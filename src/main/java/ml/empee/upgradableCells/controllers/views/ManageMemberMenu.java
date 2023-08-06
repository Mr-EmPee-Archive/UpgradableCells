package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Menu that allows you to manage cell members
 */

//TODO: Kick and Ban player

@RequiredArgsConstructor
public class ManageMemberMenu implements Bean {

  @Getter
  private static ManageMemberMenu instance;

  private final CellController cellController;
  private final LangConfig langConfig;

  @Override
  public void onStart() {
    instance = this;
  }

  private void populateMenu(ChestMenu menu, OwnedCell cell, OfflinePlayer target) {
    menu.top().setItem(2, 1, setRankMemberItem(cell, target));
    menu.top().setItem(4, 1, setRankGuardItem(cell, target));
    menu.top().setItem(6, 1, setRankManagerItem(cell, target));

    menu.top().setItem(0, 5, closeItem(cell));
  }

  private GItem closeItem(OwnedCell cell) {
    var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
        .setName(langConfig.translate("menus.manage-cell.items.close.name"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          ManageCellMenu.open((Player) e.getWhoClicked(), cell);
        }).build();
  }

  private GItem setRankMemberItem(OwnedCell cell, OfflinePlayer target) {
    var item = ItemBuilder.from(XMaterial.COBBLESTONE.parseItem())
        .setName(langConfig.translate("menus.manage-members.items.set-member.name"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          var player = (Player) e.getWhoClicked();
          player.closeInventory();

          cellController.setRank(cell, (Player) e.getWhoClicked(), target, OwnedCell.Rank.MEMBER);
        }).build();
  }

  private GItem setRankGuardItem(OwnedCell cell, OfflinePlayer target) {
    var item = ItemBuilder.from(XMaterial.LAPIS_ORE.parseItem())
        .setName(langConfig.translate("menus.manage-members.items.set-guard.name"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          var player = (Player) e.getWhoClicked();
          player.closeInventory();

          cellController.setRank(cell, (Player) e.getWhoClicked(), target, OwnedCell.Rank.GUARD);
        }).build();
  }

  private GItem setRankManagerItem(OwnedCell cell, OfflinePlayer target) {
    var item = ItemBuilder.from(XMaterial.GOLD_ORE.parseItem())
        .setName(langConfig.translate("menus.manage-members.items.set-manager.name"))
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          var player = (Player) e.getWhoClicked();
          player.closeInventory();

          cellController.setRank(cell, player, target, OwnedCell.Rank.MANAGER);
        }).build();
  }

  private ChestMenu getInventory(OwnedCell cell, Player player, OfflinePlayer target) {
    return new ChestMenu(player, 6, langConfig.translate("menus.manage-members.title")) {
      @Override
      public void onOpen() {
        populateMenu(this, cell, target);
      }
    };
  }

  public static void open(OwnedCell cell, Player player, OfflinePlayer target) {
    instance.getInventory(cell, player, target).open();
  }

}
