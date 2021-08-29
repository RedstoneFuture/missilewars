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

package de.butzlabben.missilewars.util;

import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.game.Game;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

@RequiredArgsConstructor
public class ScoreboardManager {

    private final Game game;
    private final Scoreboard board;

    public void updateInGameScoreboard() {
        removeScoreboard();

        Objective obj = board.registerNewObjective("Info", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(Config.getScoreboardTitle());

        HashMap<String, Integer> entries = Config.getScoreboardEntries();

        for (String entry : entries.keySet()) {
            String s = rep(entry);
            obj.getScore(s).setScore(entries.get(entry));
        }
    }

    public void removeScoreboard() {
        Objective old = board.getObjective(DisplaySlot.SIDEBAR);
        if (old != null)
            old.unregister();
    }

    private String rep(String entry) {
        return replaceTeam1(replaceTeam2(replaceTime(entry)));
    }


    private String replaceTeam2(String str) {
        return str.replace("%team2%", game.getTeam2().getFullname())
                .replace("%team2_amount%", "" + game.getTeam2().getMembers().size())
                .replace("%team2_color%", game.getTeam2().getColorCode());
    }

    private String replaceTeam1(String str) {
        return str.replace("%team1%", game.getTeam1().getFullname())
                .replace("%team1_amount%", "" + game.getTeam1().getMembers().size())
                .replace("%team1_color%", game.getTeam1().getColorCode());
    }

    private String replaceTime(String str) {
        return str.replace("%time%", "" + game.getTimer().getSeconds() / 60);
    }
}
