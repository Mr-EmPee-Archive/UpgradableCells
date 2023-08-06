package ml.empee.upgradableCells.controllers.views;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.ChestMenu;
import ml.empee.simplemenu.model.pane.ScrollPane;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Menu to claim a cell
 */

@RequiredArgsConstructor
public class SelectPlayerMenu implements Bean {

  private static SelectPlayerMenu instance;
  private final LangConfig langConfig;

  @Override
  public void onStart() {
    instance = this;
  }

  private void populateMenu(ChestMenu menu, List<OfflinePlayer> cells, CompletableFuture<OfflinePlayer> future) {
    var pane = new ScrollPane(7, 4);
    pane.setCols(
        cells.stream()
            .map(c -> playerItem(c, future))
            .toList()
    );

    menu.top().addPane(1, 1, pane);
  }

  private ChestMenu createMenu(Player player, List<OfflinePlayer> cells, CompletableFuture<OfflinePlayer> future) {
    return new ChestMenu(player, 3, langConfig.translate("menus.select-player.title")) {
      @Override
      public void onOpen() {
        populateMenu(this, cells, future);
      }
    };
  }

  private GItem playerItem(OfflinePlayer player, CompletableFuture<OfflinePlayer> future) {
    var item = ItemBuilder.skull()
        .setName("&e" + player.getName())
        .owner(player)
        .build();

    return GItem.builder()
        .itemstack(item)
        .clickHandler(e -> {
          future.complete(player);
        }).build();
  }

  public static CompletableFuture<OfflinePlayer> selectPlayer(Player player, List<OfflinePlayer> players) {
    CompletableFuture<OfflinePlayer> future = new CompletableFuture<>();
    instance.createMenu(player, players, future).open();
    return future;
  }

}

