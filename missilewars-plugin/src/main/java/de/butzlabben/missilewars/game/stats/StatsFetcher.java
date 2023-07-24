/*
 * This file is part of MissileWars (https://github.com/Butzlabben/missilewars).
 * Copyright (c) 2018-2021 Daniel Nägele.
 *
 * MissileWars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MissileWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MissileWars.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.butzlabben.missilewars.game.stats;

import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.util.ConnectionHolder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class StatsFetcher {

    private final Date from;
    private final long time;
    private final String arena;

    public StatsFetcher(Date from, String arena) {
        this.from = from;
        if (arena == null) {
            arena = "";
        }
        this.arena = "%" + arena + "%";
        time = getTime(from);
    }

    public int getGameCount() {
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement(replace("SELECT COUNT(*) FROM $mw_fights WHERE timestart >= ? AND arena LIKE ?"));
            ps.setLong(1, time);
            ps.setString(2, arena);
            ResultSet rs = ConnectionHolder.executeQuery(ps);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public SavedStats getAverageSavedStats(boolean mustBeenWon) {
        try {
            String statement = "SELECT AVG(timeelapsed) as timeelapsed, " +
                    "COUNT(case teamwon when 1 then 1 else null end) AS teamwon1, " +
                    "COUNT(case teamwon when 2 then 1 else null end) AS teamwon2, " +
                    "AVG(playercount) as playercount " +
                    "FROM (" + getAllStatsQuery() + ") fights WHERE timestart >= ? AND teamwon != ?";
            PreparedStatement ps = ConnectionHolder.prepareStatement(statement);
            ps.setLong(1, time);
            ps.setString(2, arena);
            ps.setLong(3, time);
            ps.setInt(4, mustBeenWon ? 0 : -1);

            ResultSet rs = ConnectionHolder.executeQuery(ps);
            if (rs.next()) {
                double winRatio = (double) rs.getInt("teamwon1") / (double) rs.getInt("teamwon2");
                return new SavedStats(rs.getLong("timeelapsed"), 0L,
                        null, rs.getDouble("playercount"), winRatio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getDrawFights() {
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement(replace("SELECT COUNT(*) FROM $mw_fights " +
                    "WHERE teamwon = 0 AND timestart > ? AND arena LIKE ?"));
            ps.setLong(1, time);
            ps.setString(2, arena);
            ResultSet rs = ConnectionHolder.executeQuery(ps);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<SavedStats> getAllStats() {
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement(getAllStatsQuery());
            ps.setLong(1, time);
            ps.setString(2, arena);

            ResultSet rs = ConnectionHolder.executeQuery(ps);
            List<SavedStats> stats = new ArrayList<>();
            while (rs.next()) {
                stats.add(new SavedStats(rs.getLong("timeelapsed"), rs.getLong("timestart"),
                        rs.getString("arena"), rs.getInt("playercount"), rs.getInt("teamwon")));
            }
            return stats;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getUniquePlayers() {
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement(replace("SELECT COUNT(DISTINCT($mw_fightmember.player)) as unique_players " +
                    "FROM $mw_fightmember JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id AND $mw_fights.timestart > ? AND $mw_fights.arena LIKE ?"));
            ps.setLong(1, time);
            ps.setString(2, arena);
            ResultSet rs = ConnectionHolder.executeQuery(ps);
            if (rs.next()) return rs.getInt("unique_players");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<UUID> getPlayers() {
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement(replace("SELECT DISTINCT($mw_fightmember.player) as unique_player " +
                    "FROM $mw_fightmember JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id AND $mw_fights.timestart > ? AND $mw_fights.arena LIKE ?"));
            ps.setLong(1, time);
            ps.setString(2, arena);
            ResultSet rs = ConnectionHolder.executeQuery(ps);
            List<UUID> list = new ArrayList<>(getUniquePlayers());
            while (rs.next()) {
                list.add(UUID.fromString(rs.getString("unique_player")));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getAllStatsQuery() {
        String statement = "SELECT id, arena, teamwon, timeelapsed, timestart, COUNT($mw_fightmember.fid) as playercount " +
                "FROM $mw_fights JOIN $mw_fightmember " +
                "ON $mw_fights.id = $mw_fightmember.fid " +
                "AND $mw_fights.timestart >= ? AND $mw_fights.arena LIKE ? " +
                "GROUP BY $mw_fights.id";
        return replace(statement);
    }

    private long getTime(Date from) {
        long time = 0;
        if (from != null)
            time = from.getTime();
        return time;
    }

    private String replace(String statement) {
        // Replace dbs
        statement = statement.replace("$mw_fights", Config.getFightsTable()).replace("$mw_fightmember", Config.getFightMembersTable());
        return statement;
    }


    public PlayerStats getStatsFrom(UUID uuid) {
        try {
            String statement = "SELECT" +
                    " (SELECT Count(*) AS wins" +
                    "  FROM $mw_fightmember" +
                    "  JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id" +
                    "  WHERE $mw_fightmember.player = ?" +
                    "   AND (($mw_fights.teamwon = 1" +
                    "         AND $mw_fightmember.team = 1)" +
                    "        OR ($mw_fights.teamwon = 2" +
                    "            AND $mw_fightmember.team = 2)) ) AS wins," +
                    " (SELECT count(*) AS loses" +
                    "  FROM $mw_fightmember" +
                    "  JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id" +
                    "  WHERE $mw_fightmember.player = ?" +
                    "   AND (($mw_fights.teamwon = 1" +
                    "         AND $mw_fightmember.team = 2)" +
                    "        OR ($mw_fights.teamwon = 2" +
                    "            AND $mw_fightmember.team = 1)) ) AS loses," +
                    " (SELECT count(*) AS games" +
                    "  FROM $mw_fightmember" +
                    "  JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id" +
                    "  WHERE $mw_fightmember.player = ? ) AS games_played," +
                    " (SELECT count(*) AS games_team1" +
                    "  FROM $mw_fightmember" +
                    "  JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id" +
                    "  WHERE $mw_fightmember.player = ?" +
                    "   AND $mw_fightmember.team = 1 ) AS games_team1," +
                    " (SELECT count(*) AS games_team2" +
                    "  FROM $mw_fightmember" +
                    "  JOIN $mw_fights ON $mw_fightmember.fid = $mw_fights.id" +
                    "  WHERE $mw_fightmember.player = ?" +
                    "   AND $mw_fightmember.team = 2 ) AS games_team2 " +
                    "FROM $mw_fightmember " +
                    "WHERE $mw_fightmember.player = ? " +
                    "GROUP BY $mw_fightmember.player ";
            statement = replace(statement);
            PreparedStatement ps = ConnectionHolder.prepareStatement(statement);
            ps.setString(1, uuid.toString());
            ps.setString(2, uuid.toString());
            ps.setString(3, uuid.toString());
            ps.setString(4, uuid.toString());
            ps.setString(5, uuid.toString());
            ps.setString(6, uuid.toString());

            ResultSet rs = ConnectionHolder.executeQuery(ps);
            if (rs.next()) {
                return new PlayerStats(uuid, rs.getInt("wins"), rs.getInt("loses"),
                        rs.getInt("games_played"), rs.getInt("games_team1"), rs.getInt("games_team2"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
