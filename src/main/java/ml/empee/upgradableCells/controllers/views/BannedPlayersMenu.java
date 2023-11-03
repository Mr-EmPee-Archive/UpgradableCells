package ml.empee.upgradableCells.controllers.views;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.services.CellService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.cryptomorin.xseries.XMaterial;

import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.controllers.CellController;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.Member;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Menu to manage banned players
 */

@Singleton
public class BannedPlayersMenu implements Listener {

  //TODO: Update menu when ban/pardon

  @Instance
  private static BannedPlayersMenu instance;
  private final LangConfig langConfig;
  private final CellController cellController;
  private final CellService cellService;

  public BannedPlayersMenu(
      UpgradableCells plugin, LangConfig langConfig,
      CellService cellService, CellController cellController
  ) {
    this.langConfig = langConfig;
    this.cellController = cellController;
    this.cellService = cellService;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  public static void open(Player player, Long cellId) {
    instance.create(player, cellId).open();
  }

  private Menu create(Player player, Long cellId) {
    return new Menu(player, cellId);
  }

  private class Menu extends InventoryMenu {
    private final GTheme gTheme = new GTheme();

    private final Long cellId;
    private final ScrollPane playersPane = ScrollPane.horizontal(7, 3, 3);

    public Menu(Player viewer, Long cellId) {
      super(viewer, 5);
      this.cellId = cellId;
      this.title = langConfig.translate("menus.banned-players.title");
    }

    @Override
    public void onOpen() {
      var background = new StaticPane(9, 5);
      background.fill(GItem.of(gTheme.background()));
      background.setItem(0, 4, closeItem());

      var cell = cellService.findCellById(cellId).orElseThrow();
      var playerRank = cell.getMember(player.getUniqueId()).orElseThrow().getRank();

      playersPane.set(
          cell.getBannedMembers().stream()
              .filter(m -> m.getRank() == null || playerRank.canManage(m.getRank()))
              .map(this::playerItem)
              .collect(Collectors.toList())
      );

      addPane(1, 1, playersPane);
      addPane(0, 0, background);
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
