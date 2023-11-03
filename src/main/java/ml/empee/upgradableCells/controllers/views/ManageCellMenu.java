package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.StaticPane;
import ml.empee.upgradableCells.controllers.CellAPI;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.views.utils.GTheme;
import ml.empee.upgradableCells.model.CellProject;
import ml.empee.upgradableCells.model.Member;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Singleton;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.stream.Collectors;

/**
 * GUI from where you can manage a cell
 */

@Singleton
@RequiredArgsConstructor
public class ManageCellMenu {

  @Instance
  private static ManageCellMenu instance;

  private final LangConfig langConfig;
  private final CellAPI cellAPI;

  public static void open(Player player, Cell cell) {
    instance.create(player, cell).open();
  }

  private Menu create(Player viewer, Cell cell) {
    return new Menu(viewer, cell);
  }

  private class Menu extends InventoryMenu {
    private final Cell cell;
    private final GTheme gTheme = new GTheme();

    public Menu(Player player, Cell cell) {
      super(player, 6);

      this.cell = cell;
      this.title = langConfig.translate("menus.manage-cell.title", cell.getOwnerAsPlayer().getName());
    }

    @Override
    public void onOpen() {
      StaticPane top = new StaticPane(9, 6);
      top.fill(GItem.of(gTheme.background()));

      top.setItem(2, 1, homeItem());
      top.setItem(4, 1, cellInfoItem());
      top.setItem(6, 1, cellVisibilityItem());
      top.setItem(2, 3, upgradeItem());
      top.setItem(4, 3, manageMembersItem());
      top.setItem(6, 3, bannedPlayersItem());
      top.setItem(0, 5, closeItem());

      addPane(0, 0, top);
    }

    private GItem cellVisibilityItem() {
      ItemBuilder item;

      var shield = XMaterial.SHIELD.parseItem();
      var shieldMeta = (BlockStateMeta) shield.getItemMeta();
      var bannerMeta = (Banner) shieldMeta.getBlockState();
      if (cell.isPublicVisible()) {
        bannerMeta.setBaseColor(DyeColor.GREEN);
        shieldMeta.setBlockState(bannerMeta);
        shield.setItemMeta(shieldMeta);
        item = ItemBuilder.from(shield);
        item.setName(langConfig.translate("menus.manage-cell.items.visibility.public.name"));
        item.setLore(langConfig.translateBlock("menus.manage-cell.items.visibility.public.lore"));
      } else {
        bannerMeta.setBaseColor(DyeColor.RED);
        shieldMeta.setBlockState(bannerMeta);
        shield.setItemMeta(shieldMeta);
        item = ItemBuilder.from(shield);
        item.setName(langConfig.translate("menus.manage-cell.items.visibility.private.name"));
        item.setLore(langConfig.translateBlock("menus.manage-cell.items.visibility.private.lore"));
      }

      return GItem.builder()
          .itemStack(item.build())
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.setCellVisibility(player, cell, !cell.isPublicVisible());
          }).build();
    }

    private GItem cellInfoItem() {
      String name = cell.getName();
      if (name == null || name.isBlank()) {
        name = "Cell Of " + cell.getOwnerAsPlayer().getName();
      }

      var item = ItemBuilder.from(XMaterial.OAK_SIGN.parseItem());
      item.setName("&e" + name);

      if (cell.getDescription() != null && !cell.getDescription().isBlank()) {
        item.setLore(
            WordUtils.wrap(
                "&7" + cell.getDescription(), 32, "\n&7", false
            ).split("\n")
        );
      }

      return GItem.builder()
          .itemStack(item.build())
          .build();
    }

    private GItem homeItem() {
      var item = ItemBuilder.from(XMaterial.IRON_DOOR.parseItem())
          .setName(langConfig.translate("menus.manage-cell.items.home.name"))
          .setLore(langConfig.translateBlock("menus.manage-cell.items.home.lore"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();

            cellAPI.teleportToCell(player, cell);
          }).build();
    }

    private GItem manageMembersItem() {
      var item = ItemBuilder.from(XMaterial.PLAYER_HEAD.parseItem())
          .setName(langConfig.translate("menus.manage-cell.items.members.name"))
          .setLore(langConfig.translateBlock("menus.manage-cell.items.members.lore"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            var player = (Player) e.getWhoClicked();
            var playerRank = cell.getMember(player.getUniqueId()).orElseThrow().getRank();
            var players = cell.getMembers().stream()
                .filter(p -> playerRank.canManage(p.getRank()))
                .filter(p -> !p.getUuid().equals(player.getUniqueId()))
                .map(p -> Bukkit.getOfflinePlayer(p.getUuid()))
                .collect(Collectors.toList());

            if (players.isEmpty()) {
              Logger.log(player, langConfig.translate("cell.members.no-members"));
              player.closeInventory();
              return;
            }

            SelectMemberMenu.selectPlayer(player, cell, players).thenAccept(
                target -> ManageMemberMenu.open(player, cell, target)
            );
          }).build();
    }

    private GItem upgradeItem() {
      CellProject project = null;
      if (cell.getLevel() != cellAPI.getLastProject().getLevel()) {
        project = cellAPI.getCellProject(cell.getLevel() + 1);
      }

      var item = ItemBuilder.from(XMaterial.GRASS_BLOCK.parseItem());
      item.setName(langConfig.translate("menus.manage-cell.items.upgrade.name"));
      if (project != null) {
        item.setLore(langConfig.translateBlock("menus.manage-cell.items.upgrade.default-lore", project.getCost()));
      } else {
        item.setLore(langConfig.translateBlock("menus.manage-cell.items.upgrade.max-level-lore"));
      }

      return GItem.builder()
          .itemStack(item.build())
          .clickHandler(e -> {
            var source = (Player) e.getWhoClicked();
            source.closeInventory();
            cellAPI.upgradeCell(source, cell);
          }).build();
    }

    private GItem bannedPlayersItem() {
      var item = ItemBuilder.from(XMaterial.BARRIER.parseItem());
      item.setName(langConfig.translate("menus.manage-cell.items.banned-players.name"));
      item.setLore(langConfig.translateBlock("menus.manage-cell.items.banned-players.lore"));

      return GItem.builder()
          .itemStack(item.build())
          .clickHandler(e -> {
            var source = (Player) e.getWhoClicked();
            if (!cell.getMember(source.getUniqueId()).orElseThrow().getRank().hasPermission(Member.Permissions.MANAGE_MEMBERS)) {
              Logger.log(player, langConfig.translate("cell.members.no-members"));
              player.closeInventory();
              return;
            }

            BannedPlayersMenu.open(source, cell);
          }).build();
    }

    private GItem closeItem() {
      var item = ItemBuilder.from(XMaterial.WHITE_BED.parseItem())
          .setName(langConfig.translate("menus.manage-cell.items.close.name"))
          .build();

      return GItem.builder()
          .itemStack(item)
          .clickHandler(e -> {
            e.getWhoClicked().closeInventory();
          }).build();
    }
  }

}
