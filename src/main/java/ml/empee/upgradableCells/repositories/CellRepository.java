package ml.empee.upgradableCells.repositories;

import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import ml.empee.upgradableCells.config.client.DbClient;
import ml.empee.upgradableCells.model.entities.Cell;
import ml.empee.upgradableCells.utils.ObjectConverter;
import mr.empee.lightwire.annotations.Singleton;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Persist a cell data
 */

@Singleton
public class CellRepository extends AbstractRepository<Cell, Long> {

  public CellRepository(DbClient client) {
    super(client, "cells");
  }

  @Override
  protected List<String> schema() {
    return List.of(
        "id INTEGER PRIMARY KEY",
        "name TEXT",
        "description TEXT",
        "members TEXT DEFAULT \"\" NOT NULL",
        "banned_members TEXT DEFAULT \"\" NOT NULL",
        "level INTEGER NOT NULL",
        "origin TEXT NOT NULL",
        "updating INTEGER DEFAULT 0 NOT NULL",
        "public_visible INTEGER DEFAULT 1 NOT NULL"
    );
  }

  @Override
  protected void prepareStatement(PreparedStatement stm, Cell data) throws SQLException {
    stm.setLong(1, data.getId());
    stm.setString(2, data.getName());
    stm.setString(3, data.getDescription());
    stm.setString(4, ObjectConverter.parse(data.getMembers()));
    stm.setString(5, ObjectConverter.parse(data.getBannedMembers()));
    stm.setInt(6, data.getLevel());
    stm.setString(7, ObjectConverter.parseLocation(data.getOrigin()));
    stm.setInt(8, data.isUpdating() ? 1 : 0);
    stm.setInt(9, data.isPublicVisible() ? 1 : 0);
  }

  @SneakyThrows
  protected Cell parse(ResultSet rs) {
    return Cell.builder()
        .id(rs.getLong("id"))
        .name(rs.getString("name"))
        .description(rs.getString("description"))
        .members(ObjectConverter.parse(rs.getString("members"), new TypeToken<>() {}))
        .bannedMembers(ObjectConverter.parse(rs.getString("banned_members"), new TypeToken<>() {}))
        .origin(ObjectConverter.parseLocation(rs.getString("origin")))
        .level(rs.getInt("level"))
        .updating(rs.getInt("updating") == 1)
        .publicVisible(rs.getInt("public_visible") == 1)
        .build();
  }

}
