package ml.empee.upgradableCells.controllers.views;

import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.pane.ScrollPane;
import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.model.events.CellMemberJoinEvent;
import ml.empee.upgradableCells.model.events.CellMemberLeaveEvent;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Menu to claim a cell
 */

@Singleton
public class SelectMemberMenu implements Listener {

  @Instance
  private static SelectMemberMenu instance;
  private final List<Menu> openedMenus = new ArrayList<>();
  private final LangConfig langConfig;

  public SelectMemberMenu(UpgradableCells plugin, LangConfig langConfig) {
    this.langConfig = langConfig;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public static CompletableFuture<OfflinePlayer> selectPlayer(Player player, OwnedCell cell,
      List<OfflinePlayer> players) {
    var action = new CompletableFuture<OfflinePlayer>();
    instance.create(player, cell, players, action).open();
    return action;
  }

  @EventHandler
  public void onCellMemberLeave(CellMemberLeaveEvent event) {
    openedMenus.stream()
        .filter(menu -> menu.cell.equals(event.getCell()))
        .forEach(menu -> menu.removeSelectablePlayer(event.getMember().getUuid()));
  }

  @EventHandler
  public void onCellMemberJoin(CellMemberJoinEvent event) {
    openedMenus.stream()
        .filter(menu -> menu.cell.equals(event.getCell()))
        .forEach(menu -> menu.addSelectablePlayer(event.getMember().getUuid()));
  }

  private Menu create(Player player, OwnedCell cell, List<OfflinePlayer> players,
      CompletableFuture<OfflinePlayer> future) {
    return new Menu(player, cell, players, future);
  }

  private class Menu extends ChestMenu {

    private final OwnedCell cell;
    private final CompletableFuture<OfflinePlayer> action;
    private final ScrollPane pane = new ScrollPane(7, 3);

    private final List<OfflinePlayer> players;

    public Menu(Player viewer, OwnedCell cell, List<OfflinePlayer> players, CompletableFuture<OfflinePlayer> action) {
      super(viewer, 5, langConfig.translate("menus.select-player.title"));

      this.cell = cell;
      this.action = action;
      this.players = new ArrayList<>(players);
    }

    public void removeSelectablePlayer(UUID uuid) {
      players.removeIf(p -> p.getUniqueId().equals(uuid));
      pane.setCols(
          players.stream()
              .map(this::playerItem)
              .collect(Collectors.toList())
      );

      refresh();
    }

    public void addSelectablePlayer(UUID uuid) {
      players.add(Bukkit.getOfflinePlayer(uuid));
      pane.setCols(
          players.stream()
              .map(this::playerItem)
              .collect(Collectors.toList())
      );

      refresh();
    }

    @Override
    public void onOpen() {
      openedMenus.add(this);

      pane.setCols(
          players.stream()
              .map(this::playerItem)
              .collect(Collectors.toList())
      );

      top().addPane(1, 1, pane);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
      openedMenus.remove(this);
    }

    private GItem playerItem(OfflinePlayer player) {
      var member = cell.getMember(player.getUniqueId());
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
          .itemstack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
            action.complete(player);
          }).build();
    }
  }

}
