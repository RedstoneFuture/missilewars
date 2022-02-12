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
import de.butzlabben.missilewars.game.timer.Timer;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

@RequiredArgsConstructor
public class ScoreboardManager {

    private Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

    private final Team team1;
    private final Team team2;
    private final String arenaDisplayName;
    private final Timer gameTimer;

    // get config options
    String scoreBoardTitle = Config.getScoreboardTitle();
    String memberListStyle = Config.getScoreboardMembersStyle();
    int memberListMaxSize = Config.getScoreboardMembersMax();

    public void updateInGameScoreboard() {
        removeScoreboard();

        Objective obj = board.registerNewObjective("Info", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(scoreBoardTitle);

        for (String cleanLine : Config.getScoreboardEntries()) {
            int i = 1;
            String replacedLine;

            if (cleanLine.contains("%team1_members%") || cleanLine.contains("%team2_members%")) {

                // team member list
                Team placeholderTeam;

                // set the current placeholder team
                if (cleanLine.contains("%team1_members%")) {
                    placeholderTeam = team1;
                } else {
                    placeholderTeam = team2;
                }

                int players = 0;

                for (MWPlayer player : placeholderTeam.getMembers()) {

                    // limit check
                    if (players >= memberListMaxSize) {
                        break;
                    }

                    String playerName = player.getPlayer().getName();
                    String teamColor = placeholderTeam.getColor();

                    replacedLine = memberListStyle.replace("%playername%", playerName)
                            .replace("%team_color%", teamColor);
                    setScoreBoardLine(obj, replacedLine, i);

                    players++;
                    i++;
                }

            } else {

                // normal placeholders
                replacedLine = replaceScoreboardPlaceholders(cleanLine);
                setScoreBoardLine(obj, replacedLine, i);

                i++;
            }
        }
    }

    public void removeScoreboard() {
        Objective old = board.getObjective(DisplaySlot.SIDEBAR);
        if (old != null)
            old.unregister();
    }

    /**
     * This method replaces the placeholders with the current value.
     * @param text (String) the original config String
     * @return the replaced text as String
     */
    private String replaceScoreboardPlaceholders(String text) {

        String time = "" + Integer.toString(gameTimer.getSeconds() / 60);


        text.replace("%team1%", team1.getFullname());
        text.replace("%team2%", team2.getFullname());

        text.replace("%team1_color%", team1.getColor());
        text.replace("%team2_color%", team2.getColor());

        text.replace("%team1_amount%", Integer.toString(team1.getMembers().size()));
        text.replace("%team2_amount%", Integer.toString(team2.getMembers().size()));

        text.replace("%arena_name%", arenaDisplayName);

        text.replace("%time%", time);

        return text;
    }

    /**
     * This methode set the scoreboard line.
     *
     * @param scoreBoardObject the vanilla scoreboard object
     * @param message the text line
     * @param lineNr the target line number (= object "score")
     */
    private static void setScoreBoardLine(Objective scoreBoardObject, String message, int lineNr) {
        // Get the "score object" (instead of a player with a text line)
        // and set the scoreboard line number with the definition of his score.
        scoreBoardObject.getScore(message).setScore(lineNr);
    }

}
