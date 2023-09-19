package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.upgradableCells.api.CellAPI;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

/**
 * Menu that allows you to manage cell members
 */

@Singleton
@RequiredArgsConstructor
public class ManageMemberMenu {

  @Instance
  private static ManageMemberMenu instance;

  private final CellAPI cellAPI;
  private final LangConfig langConfig;

  //TODO: Close menu if member not aviable anymore, or permissions changed

  public static void open(Player viewer, OwnedCell cell, OfflinePlayer target) {
    instance.create(viewer, cell, target).open();
  }

  private Menu create(Player viewer, OwnedCell cell, OfflinePlayer target) {
    return new Menu(viewer, cell, target);
  }

  private class Menu extends ChestMenu {
    private final OwnedCell cell;
    private final OfflinePlayer target;

    public Menu(Player viewer, OwnedCell cell, OfflinePlayer target) {
      super(viewer, 5, langConfig.translate("menus.manage-members.title"));

      this.cell = cell;
      this.target = target;
    }

    @Override
    public void onOpen() {
      top().setItem(2, 1, setRankMemberItem());
      top().setItem(4, 1, setRankGuardItem());
      top().setItem(6, 1, setRankManagerItem());

      top().setItem(3, 3, kickItem());
      top().setItem(5, 3, banItem());

      top().setItem(0, 4, closeItem());
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.close.name"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            ManageCellMenu.open((Player) e.getWhoClicked(), cell);
          }).build();
    }

    private GItem setRankMemberItem() {
      var item = ItemBuilder.from(XMaterial.COBBLESTONE.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.set-member.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.set-member.lore"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.setRank(cell, (Player) e.getWhoClicked(), target, Member.Rank.MEMBER);
          }).build();
    }

    private GItem setRankGuardItem() {
      var item = ItemBuilder.from(XMaterial.LAPIS_ORE.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.set-guard.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.set-guard.lore"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.setRank(cell, (Player) e.getWhoClicked(), target, Member.Rank.GUARD);
          }).build();
    }

    private GItem setRankManagerItem() {
      var item = ItemBuilder.from(XMaterial.GOLD_ORE.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.set-manager.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.set-manager.lore"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.setRank(cell, player, target, Member.Rank.MANAGER);
          }).build();
    }

    private GItem kickItem() {
      var item = ItemBuilder.from(XMaterial.LEATHER_BOOTS.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.kick.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.kick.lore"))
          .color(Color.RED).flags(ItemFlag.values())
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.kickMember(cell, player, target);
          }).build();
    }

    private GItem banItem() {
      var item = ItemBuilder.from(XMaterial.BARRIER.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.ban.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.ban.lore"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.banMember(cell, player, target);
          }).build();
    }

  }

}
