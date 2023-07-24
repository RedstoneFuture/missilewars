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
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.ConnectionHolder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FightStats {

    private final String arenaName;
    private final long timeStart, timeElapsed;
    private final int teamWon;
    private final Collection<MWPlayer> players;
    private Game game;

    public FightStats(Game game) {
        this.game = game;

        arenaName = game.getArena().getName();
        timeStart = game.getTimestart();
        timeElapsed = System.currentTimeMillis() - timeStart;
        teamWon = getGameResultCode();
        players = game.getPlayers().values();
    }


    /**
     * This method returns the game result code for the database.
     */
    private int getGameResultCode() {

        if (game.getTeam1().getGameResult() == GameResult.WIN) {
            return 1;
        } else if (game.getTeam2().getGameResult() == GameResult.WIN) {
            return 2;
        }

        return 0;
    }

    public static void checkTables() {
        if (!Config.isFightStatsEnabled())
            return;
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + Config.getFightsTable() + "(id int NOT NULL AUTO_INCREMENT PRIMARY KEY, arena TEXT NOT NULL, " +
                            "timestart bigint(20), timeelapsed bigint(20), teamwon int UNSIGNED)");
            ConnectionHolder.executeUpdate(ps);

            ps = ConnectionHolder.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + Config.getFightMembersTable() +
                            " (fid int, player varchar(36), team tinyint(4), PRIMARY KEY (player, fid))");

            ConnectionHolder.executeUpdate(ps);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert() {
        if (!Config.isFightStatsEnabled())
            return;
        try {
            PreparedStatement ps = ConnectionHolder.prepareStatement("INSERT INTO " + Config.getFightsTable() + " (arena, timestart, timeelapsed, teamwon) VALUES "
                    + " (?, ?, ?, ?)");
            ps.setString(1, arenaName);
            ps.setLong(2, timeStart);
            ps.setLong(3, timeElapsed);
            ps.setInt(4, teamWon);

            ConnectionHolder.executeUpdate(ps);
            ResultSet rs = ps.getGeneratedKeys();
            int fightID = -1;
            if (rs.next())
                fightID = rs.getInt(1);

            if (fightID == -1)
                return;
            for (MWPlayer mwPlayer : players) {
                if (mwPlayer.getTeam() != null) {
                    PreparedStatement statement = ConnectionHolder.prepareStatement("INSERT INTO " + Config.getFightMembersTable() + " (fid, player, team) VALUES "
                            + " (?, ?, ?)");
                    statement.setInt(1, fightID);
                    statement.setString(2, mwPlayer.getUuid().toString());

                    if (mwPlayer.getTeam() == game.getTeam1())
                        statement.setInt(3, 1);
                    else
                        statement.setInt(3, 2);

                    ConnectionHolder.executeUpdate(statement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
