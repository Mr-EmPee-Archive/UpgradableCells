package ml.empee.upgradableCells.controllers.views;

import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.model.events.CellMemberJoinEvent;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import ml.empee.upgradableCells.model.events.CellMemberRoleChangeEvent;
import ml.empee.upgradableCells.services.CellService;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Menu to claim a cell
 */

@Singleton
public class SelectMemberMenu implements Listener {

  @Instance
  private static SelectMemberMenu instance;
  private final LangConfig langConfig;
  private final CellService cellService;
  private final List<Menu> menus = new ArrayList<>();

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

  @EventHandler
  public void onMenuUpdate(CellMemberJoinEvent event) {
    menus.forEach(m -> {
      if (m.players.stream().anyMatch(p -> p.getUniqueId().equals(event.getMember().getUuid()))) {
        m.getPlayer().closeInventory();
      }
    });
  }

  @EventHandler
  public void onMenuUpdate(CellMemberLeaveEvent event) {
    menus.forEach(m -> {
      if (m.players.stream().anyMatch(p -> p.getUniqueId().equals(event.getMember().getUuid()))) {
        m.getPlayer().closeInventory();
      }
    });
  }

  @EventHandler
  public void onMenuUpdate(CellMemberRoleChangeEvent event) {
    menus.forEach(m -> {
      if (m.players.stream().anyMatch(p -> p.getUniqueId().equals(event.getMember().getUuid()))) {
        m.getPlayer().closeInventory();
      }
    });
  }

  public SelectMemberMenu(UpgradableCells plugin, LangConfig langConfig, CellService cellService) {
    this.langConfig = langConfig;
    this.cellService = cellService;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public static CompletableFuture<OfflinePlayer> selectPlayer(Player player, Long cellId, List<OfflinePlayer> players) {
    var action = new CompletableFuture<OfflinePlayer>();
    instance.create(player, cellId, players, action).open();
    return action;
  }

  private Menu create(Player player, Long cellId, List<OfflinePlayer> players, CompletableFuture<OfflinePlayer> future) {
    return new Menu(player, cellId, players, future);
  }

  private class Menu extends ChestMenu {

    private final CompletableFuture<OfflinePlayer> action;
    private final ScrollPane membersPane = ScrollPane.horizontal(7, 3, 3);

    private final List<OfflinePlayer> players;
    private final GTheme gTheme = new GTheme();

    private Cell cell;

    public Menu(Player viewer, Long cellId, List<OfflinePlayer> players, CompletableFuture<OfflinePlayer> action) {
      super(viewer, 5);

      this.action = action;
      this.players = new ArrayList<>(players);

      this.cell = cellService.findCellById(cellId).orElseThrow();
    }

    @Override
    public String title() {
      return langConfig.translate("menus.select-player.title");
    }

    @Override
    public void onOpen() {
      menus.add(this);
      var background = new StaticPane(9, 5);
      background.fill(GItem.of(gTheme.background()));

      membersPane.set(
          players.stream()
              .map(this::playerItem)
              .collect(Collectors.toList())
      );

      addPane(1, 1, membersPane);
      addPane(0, 0, background);
    }

    @Override
    public void onClose() {
      menus.remove(this);
    }

    private GItem playerItem(OfflinePlayer player) {
      var member = cell.getMember(player.getUniqueId()).orElseThrow();
      var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
      var item = ItemBuilder.skull()
          .setName("&e" + player.getName())
          .setLore(
              langConfig.translateBlock(
                  "menus.select-player.player-lore",
                  member.getRank().name(), member.getMemberSince().format(formatter)))
          .owner(player)
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
            action.complete(player);
          }).build();
    }
  }

}
