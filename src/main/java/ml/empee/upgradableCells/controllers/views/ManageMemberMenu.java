package ml.empee.upgradableCells.controllers.views;

import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import ml.empee.upgradableCells.model.events.CellMemberPardonEvent;
import ml.empee.upgradableCells.model.events.CellMemberRoleChangeEvent;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;

import com.cryptomorin.xseries.XMaterial;

import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.Member;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu that allows you to manage cell members
 */

@Singleton
@RequiredArgsConstructor
public class ManageMemberMenu implements Listener {

  @Instance
  private static ManageMemberMenu instance;

  private final CellController cellController;
  private final LangConfig langConfig;
  private final List<Menu> menus = new ArrayList<>();

  @EventHandler
  public void onMenuUpdate(CellMemberLeaveEvent event) {
    menus.forEach(m -> {
      if (event.getMember().getUuid().equals(m.target.getUniqueId())) {
        m.getPlayer().closeInventory();
      }
    });
  }

  @EventHandler
  public void onMenuUpdate(CellMemberRoleChangeEvent event) {
    menus.forEach(m -> {
      if (event.getMember().getUuid().equals(m.target.getUniqueId())) {
        m.getPlayer().closeInventory();
      }
    });
  }

  @EventHandler
  public void onMemberUpdate(CellMemberLeaveEvent event) {
    menus.forEach(m -> {
      if (event.getMember().getUuid().equals(m.getPlayer().getUniqueId())) {
        m.getPlayer().closeInventory();
      }
    });
  }

  @EventHandler
  public void onMemberUpdate(CellMemberRoleChangeEvent event) {
    menus.forEach(m -> {
      if (event.getMember().getUuid().equals(m.getPlayer().getUniqueId())) {
        m.getPlayer().closeInventory();
      }
    });
  }

  public static void open(Player viewer, Long cellId, OfflinePlayer target) {
    instance.create(viewer, cellId, target).open();
  }

  private Menu create(Player viewer, Long cellId, OfflinePlayer target) {
    return new Menu(viewer, cellId, target);
  }

  private class Menu extends InventoryMenu {
    private final Long cellId;
    private final OfflinePlayer target;
    private final GTheme gTheme = new GTheme();

    public Menu(Player viewer, Long cellId, OfflinePlayer target) {
      super(viewer, 5);

      this.cellId = cellId;
      this.target = target;
      this.title = langConfig.translate("menus.manage-members.title");
    }

    @Override
    public void onOpen() {
      menus.add(this);

      var top = new StaticPane(9, 5);
      top.fill(GItem.of(gTheme.background()));

      top.setItem(2, 1, setRankMemberItem());
      top.setItem(4, 1, setRankGuardItem());
      top.setItem(6, 1, setRankManagerItem());

      top.setItem(3, 3, kickItem());
      top.setItem(5, 3, banItem());

      top.setItem(0, 4, closeItem());

      addPane(0, 0, top);
    }

    @Override
    public void onClose() {
      menus.remove(this);
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.close.name"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            ManageCellMenu.open((Player) e.getWhoClicked(), cellId);
          }).build();
    }

    private GItem setRankMemberItem() {
      var item = ItemBuilder.from(XMaterial.COBBLESTONE.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.set-member.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.set-member.lore"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellController.setRank(cellId, (Player) e.getWhoClicked(), target, Member.Rank.MEMBER);
          }).build();
    }

    private GItem setRankGuardItem() {
      var item = ItemBuilder.from(XMaterial.LAPIS_ORE.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.set-guard.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.set-guard.lore"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellController.setRank(cellId, (Player) e.getWhoClicked(), target, Member.Rank.GUARD);
          }).build();
    }

    private GItem setRankManagerItem() {
      var item = ItemBuilder.from(XMaterial.GOLD_ORE.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.set-manager.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.set-manager.lore"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellController.setRank(cellId, player, target, Member.Rank.MANAGER);
          }).build();
    }

    private GItem kickItem() {
      var item = ItemBuilder.from(XMaterial.LEATHER_BOOTS.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.kick.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.kick.lore"))
          .color(Color.RED).flags(ItemFlag.values())
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellController.kickMember(cellId, player, target);
          }).build();
    }

    private GItem banItem() {
      var item = ItemBuilder.from(XMaterial.BARRIER.parseItem())
          .setName(langConfig.translate("menus.manage-members.items.ban.name"))
          .setLore(langConfig.translateBlock("menus.manage-members.items.ban.lore"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellController.banMember(cellId, player, target);
          }).build();
    }

  }

}
