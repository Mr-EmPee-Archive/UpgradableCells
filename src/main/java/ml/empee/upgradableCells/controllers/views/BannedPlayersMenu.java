package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.pane.ScrollPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.model.events.CellMemberBanEvent;
import ml.empee.upgradableCells.model.events.CellMemberPardonEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu to manage banned players
 */

@RequiredArgsConstructor
public class BannedPlayersMenu implements Bean, RegisteredListener {

  private static BannedPlayersMenu instance;
  private final LangConfig langConfig;
  private final CellController cellController;
  private final List<Menu> openedMenus = new ArrayList<>();
  private final ScrollPane pane = new ScrollPane(7, 3);


  public static void open(Player player, OwnedCell cell) {
    instance.create(player, cell).open();
  }

  @Override
  public void onStart() {
    instance = this;
  }

  @EventHandler
  public void onMemberBan(CellMemberBanEvent event) {
    openedMenus.stream()
        .filter(menu -> menu.cell.equals(event.getCell()))
        .forEach(menu -> {
          menu.updateBannedPlayerList();
          menu.refresh();
        });
  }

  @EventHandler
  public void onMemberPardon(CellMemberPardonEvent event) {
    openedMenus.stream()
        .filter(menu -> menu.cell.equals(event.getCell()))
        .forEach(menu -> {
          menu.updateBannedPlayerList();
          menu.refresh();
        });
  }

  private Menu create(Player player, OwnedCell cell) {
    return new Menu(player, cell);
  }

  private class Menu extends ChestMenu {
    private final OwnedCell cell;
    private final ScrollPane pane = new ScrollPane(7, 3);


    public Menu(Player viewer, OwnedCell cell) {
      super(viewer, 5, langConfig.translate("menus.banned-players.title"));
      this.cell = cell;
    }

    public void updateBannedPlayerList() {
      var playerRank = cell.getMember(player.getUniqueId()).getRank();

      pane.setCols(
          cell.getBannedMembers().stream()
              .filter(m -> m.getRank() == null || playerRank.canManage(m.getRank()))
              .map(this::playerItem)
              .toList()
      );
    }

    @Override
    public void onOpen() {
      openedMenus.add(this);

      top().setItem(0, 4, closeItem());

      updateBannedPlayerList();
      top().addPane(1, 1, pane);
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.banned-players.items.close.name"))
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> {
            ManageCellMenu.open((Player) e.getWhoClicked(), cell);
          }).build();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
      openedMenus.remove(this);
    }

    private GItem playerItem(Member member) {
      var target = Bukkit.getOfflinePlayer(member.getUuid());
      var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
      var item = ItemBuilder.skull()
          .setName("&e" + target.getName())
          .setLore(
              langConfig.translateBlock(
                  "menus.banned-players.player-lore",
                  member.getBannedSince().format(formatter)
              )
          )
          .owner(target)
          .build();

      return GItem.builder()
          .itemstack(item)
          .clickHandler(e -> cellController.pardonMember(cell, player, target))
          .build();
    }
  }

}
