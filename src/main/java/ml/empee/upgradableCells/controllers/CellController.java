package ml.empee.upgradableCells.controllers;

import lombok.RequiredArgsConstructor;
import ml.empee.upgradableCells.config.LangConfig;
import ml.empee.upgradableCells.config.PluginConfig;
import ml.empee.upgradableCells.constants.Permissions;
import ml.empee.upgradableCells.model.Member;
import ml.empee.upgradableCells.services.CellService;
import ml.empee.upgradableCells.utils.Logger;
import mr.empee.lightwire.annotations.Singleton;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Controller use to manage cell operations
 */

@Singleton
@RequiredArgsConstructor
public class CellController {

  private final PluginConfig pluginConfig;
  private final CellService cellService;
  private final Economy economy;
  private final LangConfig langConfig;

  public boolean canBuild(Player player, Location target) {
    if (player.hasPermission(Permissions.ADMIN)) {
      return true;
    }

    var cell = cellService.findCellByLocation(target).orElse(null);
    if (cell == null) {
      return true;
    }

    Member member = cell.getMember(player.getUniqueId()).orElse(null);
    if (member == null || !member.getRank().hasPermission(Member.Permissions.BUILD)) {
      return false;
    }

    var project = cellService.getCellProject(cell.getLevel());
    return !project.isCellBlock(cell, target);
  }

  public boolean isCellBlock(Location target) {
    var cell = cellService.findCellByLocation(target).orElse(null);
    if (cell == null) {
      return false;
    }

    var project = cellService.getCellProject(cell.getLevel());
    return project.isCellBlock(cell, target);
  }

  public boolean canInteract(Player source, Location target, @Nullable Entity entity) {
    if (source.hasPermission(Permissions.ADMIN)) {
      return true;
    }

    var cell = cellService.findCellByLocation(target).orElse(null);
    if (cell == null) {
      return true;
    }

    Member member = cell.getMember(source.getUniqueId()).orElse(null);
    if (member == null) {
      return false;
    }

    if (entity == null) {
      Block block = target.getBlock();
      if (block.getType().name().contains("CHEST")) {
        return member.getRank().hasPermission(Member.Permissions.ACCESS_CHESTS);
      }
    }

    return true;
  }

  public void setCellName(Long cellId, Player source, String name) {
    if (name.length() > 32) {
      Logger.log(source, langConfig.translate("cell.illegal-name"));
      return;
    }

    cellService.setName(cellId, name);
    Logger.log(source, langConfig.translate("cell.name-updated"));
  }

  public void setCellDescription(Long cellId, Player source, String description) {
    if (description.length() > 132) {
      Logger.log(source, langConfig.translate("cell.illegal-description"));
      return;
    }

    cellService.setDescription(cellId, description);
    Logger.log(source, langConfig.translate("cell.description-updated"));
  }

  public void setCellVisibility(Long cellId, Player source, boolean publicVisible) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    var member = cell.getMember(source.getUniqueId()).orElseThrow();
    if (!member.getRank().hasPermission(Member.Permissions.CHANGE_VISIBILITY)) {
      Logger.log(source, langConfig.translate("cell.visibility.missing-perm"));
      return;
    }

    cellService.setVisibility(cell.getId(), publicVisible);
    if (publicVisible) {
      source.sendTitle(
          langConfig.translate("cell.visibility.title.public"),
          langConfig.translate("cell.visibility.title.lore"),
          10, 70, 10);
    } else {
      source.sendTitle(
          langConfig.translate("cell.visibility.title.private"),
          langConfig.translate("cell.visibility.title.lore"),
          10, 70, 10);
    }

    Logger.log(source, langConfig.translate("cell.visibility.changed"));
  }

  /**
   * Pardon a banned member
   */
  public void pardonMember(Long cellId, Player source, OfflinePlayer target) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    var member = cell.getMember(source.getUniqueId()).orElseThrow();
    var targetMember = cell.getBannedMember(target.getUniqueId()).orElseThrow();
    if (!member.getRank().canManage(targetMember.getRank())) {
      Logger.log(source, langConfig.translate("cell.members.un-manageable"));
      return;
    }

    cell = cellService.pardonMember(cell.getId(), target.getUniqueId());
    for (Player p : cell.getOnlineMembers()) {
      Logger.log(p, langConfig.translate("cell.members.unbanned", target.getName(), cell.getOwnerAsPlayer().getName()));
    }
  }

  /**
   * Ban a member
   */
  public void banMember(Long cellId, Player source, OfflinePlayer target) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    var member = cell.getMember(source.getUniqueId()).orElseThrow();
    var targetMember = cell.getMember(target.getUniqueId()).orElseThrow();
    if (!member.getRank().canManage(targetMember.getRank())) {
      Logger.log(source, langConfig.translate("cell.members.un-manageable"));
      return;
    }

    cell = cellService.banMember(cell.getId(), target.getUniqueId());
    if (target.isOnline()) {
      var player = target.getPlayer();
      var currentCell = cellService.findCellByLocation(player.getLocation()).orElse(null);
      if (cell.equals(currentCell)) {
        player.teleport(pluginConfig.getSpawnLocation());
      }

      Logger.log(target.getPlayer(), langConfig.translate("cell.members.banned", target.getName(), cell.getOwnerAsPlayer().getName()));
    }

    for (Player p : cell.getOnlineMembers()) {
      Logger.log(p, langConfig.translate("cell.members.banned", target.getName(), cell.getOwnerAsPlayer().getName()));
    }
  }

  /**
   * Kick a member
   */
  public void kickMember(Long cellId, Player source, OfflinePlayer target) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    var member = cell.getMember(source.getUniqueId()).orElseThrow();
    var targetMember = cell.getMember(target.getUniqueId()).orElseThrow();

    if (!member.getRank().canManage(targetMember.getRank())) {
      Logger.log(source, langConfig.translate("cell.members.un-manageable"));
      return;
    }

    cell = cellService.removeMember(cell.getId(), target.getUniqueId());
    if (target.isOnline()) {
      Logger.log(target.getPlayer(), langConfig.translate("cell.members.kicked", target.getName(), cell.getOwnerAsPlayer().getName()));
    }

    for (Player p : cell.getOnlineMembers()) {
      Logger.log(p, langConfig.translate("cell.members.kicked", target.getName(), cell.getOwnerAsPlayer().getName()));
    }
  }

  /**
   * Change the rank of a cell member
   */
  public void setRank(Long cellId, Player source, OfflinePlayer target, Member.Rank rank) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    var sourceRank = cell.getMember(source.getUniqueId()).orElseThrow().getRank();
    var targetRank = cell.getMember(target.getUniqueId()).orElseThrow().getRank();
    if (!sourceRank.canManage(targetRank) || !sourceRank.canManage(rank)) {
      Logger.log(source, langConfig.translate("cell.members.un-manageable"));
      return;
    }

    cell = cellService.setMember(cell.getId(), target.getUniqueId(), rank);
    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member,
          langConfig.translate("cell.members.set-rank", target.getName(), rank, cell.getOwnerAsPlayer().getName())
      );
    }
  }

  /**
   * Invite a player to a cell
   */
  public void invitePlayer(Long cellId, Player source, Player target) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    if (!cell.getMember(source.getUniqueId()).orElseThrow().getRank().hasPermission(Member.Permissions.INVITE)) {
      Logger.log(source, langConfig.translate("cell.invitation.missing-perm"));
      return;
    }

    if (cell.getBannedMember(target.getUniqueId()).isPresent()) {
      Logger.log(source, langConfig.translate("cell.invitation.banned"));
      return;
    }

    if (cell.getMember(target.getUniqueId()).isPresent()) {
      Logger.log(source, langConfig.translate("cell.invitation.already-joined"));
      return;
    }

    if (cellService.hasInvitation(cell, target.getUniqueId())) {
      Logger.log(source, langConfig.translate("cell.invitation.already-invited"));
      return;
    }

    if (cellService.getCellProject(cell.getLevel()).getMembers() <= cell.getMembersAsPlayers().size()) {
      Logger.log(source, langConfig.translate("cell.invitation.max-members"));
      return;
    }

    cellService.invite(cell, target.getUniqueId());
    Logger.log(source, langConfig.translate("cell.invitation.sent", target.getName()));
    Logger.log(target, langConfig.translate("cell.invitation.received", cell.getOwnerAsPlayer().getName()));
  }

  /**
   * Join a cell if invited
   */
  public void joinCell(Long cellId, Player source) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    if (cell.getMember(source.getUniqueId()).isPresent()) {
      Logger.log(source, langConfig.translate("cell.invitation.already-joined"));
      return;
    }

    if (!cellService.hasInvitation(cell, source.getUniqueId())) {
      Logger.log(source, langConfig.translate("cell.invitation.missing"));
      return;
    }

    if (cell.getBannedMember(source.getUniqueId()).isPresent()) {
      return;
    }

    if (cellService.getCellProject(cell.getLevel()).getMembers() <= cell.getMembersAsPlayers().size()) {
      Logger.log(source, langConfig.translate("cell.invitation.max-members"));
      return;
    }

    cell = cellService.setMember(cell.getId(), source.getUniqueId(), Member.Rank.MEMBER);
    cellService.removeInvitation(cell, source.getUniqueId());

    for (Player member : cell.getOnlineMembers()) {
      Logger.log(member, langConfig.translate("cell.members.has-joined", source.getName(), cell.getOwnerAsPlayer().getName()));
    }
  }

  /**
   * Leave a cell
   */
  public void leaveCell(Long cellId, Player source) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    var member = cell.getMember(source.getUniqueId()).orElse(null);
    if (member == null) {
      Logger.log(source, langConfig.translate("cell.members.not-member"));
      return;
    }

    if (member.getRank() == Member.Rank.OWNER) {
      Logger.log(source, langConfig.translate("cell.owner-leave"));
      return;
    }

    cell = cellService.removeMember(cell.getId(), source.getUniqueId());
    Logger.log(source, langConfig.translate("cell.members.has-left", source.getName(), cell.getOwnerAsPlayer().getName()));
    for (Player m : cell.getOnlineMembers()) {
      Logger.log(m, langConfig.translate("cell.members.has-left", source.getName(), cell.getOwnerAsPlayer().getName()));
    }
  }

  /**
   * Create a cell
   */
  public void createCell(Player source) {
    if (cellService.findCellByOwner(source.getUniqueId()).isPresent()) {
      Logger.log(source, langConfig.translate("cell.already-bought"));
      return;
    }

    var project = cellService.getCellProject(0);
    if (!economy.has(source, project.getCost())) {
      Logger.log(source, langConfig.translate("economy.missing-money", project.getCost()));
      return;
    }

    economy.withdrawPlayer(source, project.getCost());
    cellService.createCell(source.getUniqueId());
    Logger.log(source, langConfig.translate("cell.bought-cell"));
  }

  /**
   * Upgrade a cell to the next-level
   */
  public void upgradeCell(Long cellId, Player source) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    if (!cell.getMember(source.getUniqueId()).orElseThrow().getRank().hasPermission(Member.Permissions.UPGRADE)) {
      Logger.log(source, langConfig.translate("cmd.missing-permission"));
      return;
    }

    if (cellService.getLastProject().getLevel() == cell.getLevel()) {
      Logger.log(source, langConfig.translate("cell.max-level"));
      return;
    }

    if (cell.isUpdating()) {
      Logger.log(source, langConfig.translate("cell.still-building"));
      return;
    }

    var project = cellService.getCellProject(cell.getLevel() + 1);
    if (!economy.has(source, project.getCost())) {
      Logger.log(source, langConfig.translate("economy.missing-money", project.getCost()));
      return;
    }

    economy.withdrawPlayer(source, project.getCost());
    cellService.upgradeCell(cell.getId(), project.getLevel());
    Logger.log(source, langConfig.translate("cell.bought-upgrade"));
  }

  /**
   * Teleport a player to a cell
   */
  public void teleportToCell(Long cellId, Player source) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    if (cell.getLevel() == 0 && cell.isUpdating()) {
      Logger.log(source, langConfig.translate("cell.still-building"));
      return;
    }

    Member member = cell.getMember(source.getUniqueId()).orElse(null);
    if (member == null && !cell.isPublicVisible()) {
      Logger.log(source, langConfig.translate("cmd.missing-permission"));
      return;
    }

    if (cell.getBannedMember(source.getUniqueId()).isPresent()) {
      Logger.log(source, langConfig.translate("cell.banned-interaction"));
      return;
    }

    source.teleport(cellService.getSpawnpoint(cell));
  }

  /**
   * Ban all members of the cell (including the owner) and make it private
   */
  public void makeCellUnacessable(Long cellId, CommandSender source) {
    var cell = cellService.findCellById(cellId).orElseThrow();
    cellService.setVisibility(cell.getId(), false);

    for (var member : cell.getMembersAsPlayers()) {
      cellService.banMember(cell.getId(), member.getUniqueId());

      if (member.isOnline()) {
        member.getPlayer().teleport(pluginConfig.getSpawnLocation());
        Logger.log(member.getPlayer(), langConfig.translate("cell.deleted"));
      }
    }

    Logger.log(source, "&7The cell of &e%s&7 has been deleted!", cell.getOwnerAsPlayer().getName());
  }

}
