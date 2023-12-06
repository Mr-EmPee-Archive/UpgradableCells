package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.Member;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import ml.empee.upgradableCells.model.events.CellMemberPardonEvent;
import ml.empee.upgradableCells.model.events.CellMemberRoleChangeEvent;
import ml.empee.upgradableCells.services.CellService;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu to manage banned players
 */

@Singleton
@RequiredArgsConstructor
public class BannedPlayersMenu implements Listener {

  @Instance
  private static BannedPlayersMenu instance;
  private final LangConfig langConfig;
  private final CellController cellController;
  private final CellService cellService;
  private final List<Menu> menus = new ArrayList<>();

  @EventHandler
  public void onMenuUpdate(CellMemberPardonEvent event) {
    menus.forEach(m -> {
      if (event.getCell().getId().equals(m.cellId)) {
        m.refresh();
      }
    });
  }

  @EventHandler
  public void onMenuUpdate(CellMemberLeaveEvent event) {
    if (!event.isBanned()) {
      return;
    }

    menus.forEach(m -> {
      if (event.getCell().getId().equals(m.cellId)) {
        m.refresh();
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

  public static void open(Player player, Long cellId) {
    instance.create(player, cellId).open();
  }

  private Menu create(Player player, Long cellId) {
    return new Menu(player, cellId);
  }

  private class Menu extends ChestMenu {
    private final GTheme gTheme = new GTheme();

    private final Long cellId;
    private final ScrollPane playersPane = ScrollPane.horizontal(7, 3, 3);

    public Menu(Player viewer, Long cellId) {
      super(viewer, 5);
      this.cellId = cellId;
    }

    @Override
    public String title() {
      return langConfig.translate("menus.banned-players.title");
    }

    @Override
    public void onOpen() {
      menus.add(this);

      var background = new StaticPane(9, 5);
      background.fill(GItem.of(gTheme.background()));
      background.setItem(0, 4, closeItem());

      setBannedMembersView();

      addPane(1, 1, playersPane);
      addPane(0, 0, background);
    }

    public void setBannedMembersView() {
      var cell = cellService.findCellById(cellId).orElseThrow();
      var playerRank = cell.getMember(player.getUniqueId()).orElseThrow().getRank();

      playersPane.set(
          cell.getBannedMembers().stream()
              .filter(m -> m.getRank() == null || playerRank.canManage(m.getRank()))
              .map(this::playerItem)
              .collect(Collectors.toList())
      );
    }

    public void refresh() {
      setBannedMembersView();
      update();
    }

    @Override
    public void onClose() {
      menus.remove(this);
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.banned-players.items.close.name"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            ManageCellMenu.open((Player) e.getWhoClicked(), cellId);
          }).build();
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
          .itemStack(item)
          .clickHandler(e -> {
            cellController.pardonMember(cellId, player, target);
            player.closeInventory();
          }).build();
    }
  }

}
