package ml.empee.upgradableCells.controllers.views;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.cryptomorin.xseries.XMaterial;

import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.upgradableCells.UpgradableCells;
import ml.empee.upgradableCells.api.CellAPI;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
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
  private final CellAPI cellAPI;

  public BannedPlayersMenu(UpgradableCells plugin, LangConfig langConfig, CellAPI cellAPI) {
    this.langConfig = langConfig;
    this.cellAPI = cellAPI;

    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  public static void open(Player player, OwnedCell cell) {
    instance.create(player, cell).open();
  }

  private Menu create(Player player, OwnedCell cell) {
    return new Menu(player, cell);
  }

  private class Menu extends InventoryMenu {
    private final OwnedCell cell;
    private final ScrollPane pane = ScrollPane.horizontal(7, 3, 3);

    public Menu(Player viewer, OwnedCell cell) {
      super(viewer, 5, langConfig.translate("menus.banned-players.title"));
      this.cell = cell;
    }

    @Override
    public void onOpen() {
      top().setItem(0, 4, closeItem());

      var playerRank = cell.getMember(player.getUniqueId()).getRank();

      pane.addAll(
          cell.getBannedMembers().stream()
              .filter(m -> m.getRank() == null || playerRank.canManage(m.getRank()))
              .map(this::playerItem)
              .collect(Collectors.toList())
      );
      
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
          .clickHandler(e -> cellAPI.pardonMember(cell, player, target))
          .build();
    }
  }

}
