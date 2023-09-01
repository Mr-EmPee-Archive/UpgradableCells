package ml.empee.upgradableCells.controllers;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.upgradableCells.config.CommandsConfig;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.controllers.views.ClaimCellMenu;
import ml.empee.upgradableCells.controllers.views.ManageCellMenu;
import ml.empee.upgradableCells.controllers.views.SelectCellMenu;
import ml.empee.upgradableCells.model.entities.Member;
import ml.empee.upgradableCells.model.entities.OwnedCell;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Controller use to manage cell operations
 */

@RequiredArgsConstructor
public class CellController implements Bean {

  private final CommandsConfig commandsConfig;
  private final CellService cellService;
  private final Economy economy;
  private final LangConfig langConfig;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  @CommandMethod("claim")
  public void claimCell(Player sender) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      ClaimCellMenu.open(sender);
    } else {
      Logger.log(sender, langConfig.translate("cell.already-bought"));
    }
  }

  /**
   * Open the cell management menu
   */
  @CommandMethod("cell")
  public void openCell(Player sender) {
    var cells = cellService.findCellsByMember(sender.getUniqueId());

    if (cells.isEmpty()) {
      ClaimCellMenu.open(sender);
      return;
    }

    if (cells.size() == 1) {
      ManageCellMenu.open(sender, cells.get(0));
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> ManageCellMenu.open(sender, c)
      );
    }
  }

  /**
   * Teleport a player to his cell
   */
  @CommandMethod("home")
  public void teleportToCell(Player sender) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    teleportToCell(sender, cell);
  }

  @CommandMethod("cell join <target>")
  public void joinCell(Player sender, @Argument OfflinePlayer target) {
    //TODO: Get invites and show GUI
    OwnedCell cell = cellService.findCellByOwner(target.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-existing"));
      return;
    }

    joinCell(sender, cell);
  }

  /**
   * Invite a player to a specific cell
   */
  @CommandMethod("cell invite <target>")
  public void inviteToCell(Player sender, @Argument Player target) {
    var cells = cellService.findCellsByMember(sender.getUniqueId()).stream()
        .filter(c -> c.getMember(sender.getUniqueId()).getRank().canInvite())
        .toList();

    if (cells.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cells.size() == 1) {
      invitePlayer(cells.get(0), sender, target);
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> invitePlayer(c, sender, target)
      );
    }
  }

  /**
   * Leave a cell
   */
  @CommandMethod("cell leave")
  public void leaveCell(Player sender) {
    var cells = cellService.findCellsByMember(sender.getUniqueId());

    if (cells.isEmpty()) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    if (cells.size() == 1) {
      leaveCell(sender, cells.get(0));
    } else {
      SelectCellMenu.selectCell(sender, cells).thenAccept(
          c -> leaveCell(sender, c)
      );
    }
  }

  @CommandMethod("cell name <name>")
  public void setCellName(Player sender, @Argument @Greedy String name) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    setCellName(sender, cell, name);
  }

  @CommandMethod("cell description <description>")
  public void setCellDescription(Player sender, @Argument @Greedy String description) {
    var cell = cellService.findCellByOwner(sender.getUniqueId()).orElse(null);

    if (cell == null) {
      Logger.log(sender, langConfig.translate("cell.not-bought"));
      return;
    }

    setCellDescription(sender, cell, description);
  }

  public void setCellName(Player sender, OwnedCell cell, String name) {
    if (name.length() > 32) {
      Logger.log(sender, langConfig.translate("cell.illegal-name"));
      return;
    }

    cellService.setName(cell, name);
    Logger.log(sender, langConfig.translate("cell.name-updated"));
  }

  public void setCellDescription(Player sender, OwnedCell cell, String description) {
    if (description.length() > 132) {
      Logger.log(sender, langConfig.translate("cell.illegal-description"));
      return;
    }

    cellService.setDescription(cell, description);
    Logger.log(sender, langConfig.translate("cell.description-updated"));
  }

  public void pardonMember(OwnedCell cell, OfflinePlayer target) {
    cellService.pardonMember(cell, target.getUniqueId());
    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member, langConfig.translate("cell.members.unbanned", target.getName(), cell.getOwnerPlayer().getName()));
    }
  }

  public void banMember(OwnedCell cell, OfflinePlayer target) {
    cellService.banMember(cell, target.getUniqueId());
    if (target.isOnline()) {
      Logger.log(target.getPlayer(), langConfig.translate("cell.members.banned", target.getName(), cell.getOwnerPlayer().getName()));
    }

    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member, langConfig.translate("cell.members.banned", target.getName(), cell.getOwnerPlayer().getName()));
    }
  }

  public void kickMember(OwnedCell cell, OfflinePlayer target) {
    cellService.removeMember(cell, target.getUniqueId());
    if (target.isOnline()) {
      Logger.log(target.getPlayer(), langConfig.translate("cell.members.kicked", target.getName(), cell.getOwnerPlayer().getName()));
    }

    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member, langConfig.translate("cell.members.kicked", target.getName(), cell.getOwnerPlayer().getName()));
    }
  }

  /**
   * Change the rank of a cell member
   */
  public void setRank(OwnedCell cell, Player source, OfflinePlayer target, Member.Rank rank) {
    var sourceRank = cell.getMember(source.getUniqueId()).getRank();
    if (!sourceRank.canManageMembers()) {
      Logger.log(source, langConfig.translate("cmd.missing-permission"));
      return;
    }

    var targetRank = cell.getMember(target.getUniqueId()).getRank();
    if (!sourceRank.canManage(targetRank) || !sourceRank.canManage(rank)) {
      Logger.log(source, langConfig.translate("cell.members.set-rank-failed"));
      return;
    }

    cellService.setMember(cell, target.getUniqueId(), rank);
    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member, langConfig.translate("cell.members.set-rank", target.getName(), rank, cell.getOwnerPlayer().getName()));
    }
  }

  /**
   * Invite a player to a cell
   */
  public void invitePlayer(OwnedCell cell, Player source, Player target) {
    if (!cell.getMember(source.getUniqueId()).getRank().canInvite()) {
      Logger.log(source, langConfig.translate("cell.invitation.missing-perm"));
      return;
    }

    if (cell.isBannedMember(target.getUniqueId())) {
      Logger.log(source, langConfig.translate("cell.invitation.banned"));
      return;
    }

    if (cell.hasMember(target.getUniqueId())) {
      Logger.log(source, langConfig.translate("cell.invitation.already-joined"));
      return;
    }

    if (cellService.hasInvitation(cell, target.getUniqueId())) {
      Logger.log(source, langConfig.translate("cell.invitation.already-invited"));
      return;
    }

    cellService.invite(cell, target.getUniqueId());
    Logger.log(source, langConfig.translate("cell.invitation.sent", target.getName()));
    Logger.log(target, langConfig.translate("cell.invitation.received", cell.getOwnerPlayer().getName()));
  }

  /**
   * Join a cell if invited
   */
  public void joinCell(Player player, OwnedCell cell) {
    if (cell.hasMember(player.getUniqueId())) {
      Logger.log(player, langConfig.translate("cell.invitation.already-joined"));
      return;
    }

    if (!cellService.hasInvitation(cell, player.getUniqueId())) {
      Logger.log(player, langConfig.translate("cell.invitation.missing"));
      return;
    }

    if (cell.isBannedMember(player.getUniqueId())) {
      return;
    }

    cellService.setMember(cell, player.getUniqueId(), Member.Rank.MEMBER);
    cellService.removeInvitation(cell, player.getUniqueId());

    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member, langConfig.translate("cell.members.has-joined", player.getName(), cell.getOwnerPlayer().getName()));
    }
  }

  /**
   * Leave a cell
   */
  public void leaveCell(Player player, OwnedCell cell) {
    var member = cell.getMember(player.getUniqueId());
    if (member == null) {
      Logger.log(player, langConfig.translate("cell.members.not-member"));
      return;
    }

    if (member.getRank() == Member.Rank.OWNER) {
      Logger.log(player, langConfig.translate("cell.owner-leave"));
      return;
    }

    cellService.removeMember(cell, player.getUniqueId());
    Logger.log(player, langConfig.translate("cell.members.has-left", player.getName(), cell.getOwnerPlayer().getName()));
    for (Player m : cell.getOnlineMembers()) {
      Logger.log(m, langConfig.translate("cell.members.has-left", player.getName(), cell.getOwnerPlayer().getName()));
    }
  }

  /**
   * Create a cell
   */
  public void createCell(Player player) {
    if (cellService.findCellByOwner(player.getUniqueId()).isPresent()) {
      Logger.log(player, langConfig.translate("cell.already-bought"));
      return;
    }

    var project = cellService.getCellProject(0);
    if (!economy.has(player, project.getCost())) {
      Logger.log(player, langConfig.translate("economy.missing-money", project.getCost()));
      return;
    }

    economy.withdrawPlayer(player, project.getCost());
    cellService.createCell(player.getUniqueId());
    Logger.log(player, langConfig.translate("cell.bought-cell"));
  }

  /**
   * Upgrade a cell to the next-level
   */
  public void upgradeCell(Player player, OwnedCell cell) {
    if (!cell.getMember(player.getUniqueId()).getRank().canUpgrade()) {
      Logger.log(player, langConfig.translate("cmd.missing-permission"));
      return;
    }

    if (cellService.getLastProject().getLevel() == cell.getLevel()) {
      Logger.log(player, langConfig.translate("cell.max-level"));
      return;
    }

    if (cell.isPasting()) {
      Logger.log(player, langConfig.translate("cell.still-building"));
      return;
    }

    var project = cellService.getCellProject(cell.getLevel() + 1);
    if (!economy.has(player, project.getCost())) {
      Logger.log(player, langConfig.translate("economy.missing-money", project.getCost()));
      return;
    }

    economy.withdrawPlayer(player, project.getCost());
    cellService.upgradeCell(cell, project.getLevel());
    Logger.log(player, langConfig.translate("cell.bought-upgrade"));
  }

  /**
   * Teleport a player to a cell
   */
  public void teleportToCell(Player player, OwnedCell cell) {
    if (cell.getLevel() == 0 && cell.isPasting()) {
      Logger.log(player, langConfig.translate("cell.still-building"));
      return;
    }

    Member member = cell.getMember(player.getUniqueId());
    if (member == null) {
      Logger.log(player, langConfig.translate("cmd.missing-permission"));
      return;
    }

    player.teleport(cellService.getSpawnpoint(cell));
  }

}
