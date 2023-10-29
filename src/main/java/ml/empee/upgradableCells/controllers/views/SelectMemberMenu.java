package ml.empee.upgradableCells.controllers.views;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.entities.Cell;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.config.LangConfig;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Menu to claim a cell
 */

@Singleton
public class SelectMemberMenu implements Listener {

  //TODO: Update menu when join/kick

  @Instance
  private static SelectMemberMenu instance;
  private final LangConfig langConfig;

  public SelectMemberMenu(UpgradableCells plugin, LangConfig langConfig) {
    this.langConfig = langConfig;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public static CompletableFuture<OfflinePlayer> selectPlayer(Player player, Cell cell,
      List<OfflinePlayer> players) {
    var action = new CompletableFuture<OfflinePlayer>();
    instance.create(player, cell, players, action).open();
    return action;
  }

  private Menu create(Player player, Cell cell, List<OfflinePlayer> players,
                      CompletableFuture<OfflinePlayer> future) {
    return new Menu(player, cell, players, future);
  }

  private class Menu extends InventoryMenu {

    private final Cell cell;
    private final CompletableFuture<OfflinePlayer> action;
    private final ScrollPane membersPane = ScrollPane.horizontal(7, 3, 3);

    private final List<OfflinePlayer> players;
    private final GTheme gTheme = new GTheme();

    public Menu(Player viewer, Cell cell, List<OfflinePlayer> players, CompletableFuture<OfflinePlayer> action) {
      super(viewer, 5);

      this.cell = cell;
      this.action = action;
      this.players = new ArrayList<>(players);
      this.title = langConfig.translate("menus.select-player.title");
    }

    @Override
    public void onOpen() {
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
