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

package de.butzlabben.missilewars.game.misc;

import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.timer.modules.ScoreboardTimer;
import de.butzlabben.missilewars.game.timer.TaskManager;
import de.butzlabben.missilewars.player.MWPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Scoreboard Management: https://www.spigotmc.org/wiki/making-scoreboard-with-teams-no-flicker

@RequiredArgsConstructor
public class ScoreboardManager {

    private final Game game;

    private Team team1;
    private Team team2;

    @Setter private String arenaDisplayName;
    
    // get config options
    private static final String SCOREBOARD_TITLE = Config.getScoreboardTitle();
    private static final String MEMBER_LIST_STYLE = Config.getScoreboardMembersStyle();
    private static final int MEMBER_LIST_MAX_SIZE = Config.getScoreboardMembersMax();
    private static final List<String> SCOREBOARD_ENTRIES = Config.getScoreboardEntries();

    private boolean isTeam1ListUsed = false;
    private boolean isTeam2ListUsed = false;

    @Getter private Scoreboard board;
    @Getter private boolean boardIsReady = false;
    private Objective obj;
    private Map<Integer, org.bukkit.scoreboard.Team> teams = new HashMap<>();
    private Map<Team, Integer> scoreboardTeamPage = new HashMap<>();
    private static final String[] COLOR_CODES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    private TaskManager taskManager;

    /**
     * This method registers the scoreboard.
     */
    private void createScoreboard() {

        team1 = game.getTeamManager().getTeam1();
        team2 = game.getTeamManager().getTeam2();

        if (game.getArenaConfig() == null) {
            // using of placeholders until the arena is not set
            setArenaDisplayName("?");
        } else {
            setArenaDisplayName(game.getArenaConfig().getDisplayName());
        }

        // register Scoreboard
        if (board == null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        obj = board.registerNewObjective("Info", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(SCOREBOARD_TITLE);

        // check if the team lists are used
        for (String cleanLine : SCOREBOARD_ENTRIES) {
            if (cleanLine.contains("%team1_members%")) {
                isTeam1ListUsed = true;
            } else if (cleanLine.contains("%team2_members%")) {
                isTeam2ListUsed = true;
            }
        }
        
        resetScoreboardTeamPage(team1);
        resetScoreboardTeamPage(team2);
        
        updateScoreboard();
        
        boardIsReady = true;
        
        if (taskManager == null) {
            taskManager = new TaskManager(game);
            taskManager.setTimer(new ScoreboardTimer(game));
            taskManager.runTimer(0, 60);
        }
    }

    /**
     * This method creates a team for the scoreboard and adds it to the teams ArrayList.
     *
     * @param line the Scoreboard line number
     */
    private void addScoreboardTeam(int line) {
        org.bukkit.scoreboard.Team team;

        if (teams.size() < line) {
            team = board.registerNewTeam(arenaDisplayName + "-" + line);
            team.addEntry("§" + COLOR_CODES[line - 1]);
            obj.getScore("§" + COLOR_CODES[line - 1]).setScore(line);
            teams.put(line, team);
        }
    }

    public void updateScoreboard() {

        // the number of lines required for the complete Scoreboard
        int scoreboardLine = SCOREBOARD_ENTRIES.size() + getLineOffset();

        // add new teams
        for (int i = 1; i <= scoreboardLine; i++) {
            addScoreboardTeam(i);
        }

        String replacedLine;

        for (String cleanLine : SCOREBOARD_ENTRIES) {
            if (scoreboardLine <= 0) {
                break;
            }

            if (cleanLine.contains("%team1_members%") || cleanLine.contains("%team2_members%")) {

                // team member list placeholder management:

                // Note: A team-page switch and a 'member_list_max' setting (max items for one page) 
                // is used here, as the scoreboard can't display more than 15 items.
                
                Team placeholderTeam;

                // set the current placeholder team
                if (cleanLine.contains("%team1_members%")) {
                    placeholderTeam = team1;
                } else {
                    placeholderTeam = team2;
                }

                // check if there is no one in the team at the moment
                if (placeholderTeam.getMembers().isEmpty()) {
                    continue;
                }
                
                // reset team-page number if there are no longer enough players on the team for this page
                if (getScoreboardTeamPage(placeholderTeam) > Math.ceil((double)placeholderTeam.getMembers().size() / (double)MEMBER_LIST_MAX_SIZE)) {
                    resetScoreboardTeamPage(placeholderTeam);
                }

                int playerCounter = 1;

                // list all team members
                for (MWPlayer mwPlayer : placeholderTeam.getMembers()) {
                    
                    // min-item limit check for the current page
                    if (playerCounter <= (getScoreboardTeamPage(placeholderTeam) - 1) * MEMBER_LIST_MAX_SIZE) {
                        playerCounter++;
                        continue;
                    }
                    
                    // max-item limit check for the current page
                    if (playerCounter > getScoreboardTeamPage(placeholderTeam) * MEMBER_LIST_MAX_SIZE) {
                        resetScoreboardTeamPage(placeholderTeam);
                        break;
                    }

                    String playerName = mwPlayer.getPlayer().getName();
                    String teamColor = placeholderTeam.getColor();

                    replacedLine = MEMBER_LIST_STYLE.replace("%playername%", playerName)
                            .replace("%team_color%", teamColor);
                    teams.get(scoreboardLine).setPrefix(replacedLine);

                    playerCounter++;
                    scoreboardLine--;
                }
                
                // Fill the rest of the player-list lines with a blank line, if no more player exists, starting on page 2.
                if (getScoreboardTeamPage(placeholderTeam) == 1) continue;
                for (int i = playerCounter; i <= getScoreboardTeamPage(placeholderTeam) * MEMBER_LIST_MAX_SIZE; i++) {
                    teams.get(scoreboardLine).setPrefix("");
                    
                    playerCounter++;
                    scoreboardLine--;
                }

            } else {

                // normal placeholders management:

                replacedLine = replaceScoreboardPlaceholders(cleanLine);
                teams.get(scoreboardLine).setPrefix(replacedLine);

                scoreboardLine--;
            }
        }
        
    }

    /**
     * This method calculates the offset lines based of the amount of players
     * and the using of the member-list placeholders for both teams.
     *
     * @return (int) the amount of offset lines
     */
    private int getLineOffset() {

        int team1ListSize = 0;
        int team2ListSize = 0;

        if (isTeam1ListUsed) {
            team1ListSize = Math.min(team1.getMembers().size(), MEMBER_LIST_MAX_SIZE);
            team1ListSize--;
        }

        if (isTeam2ListUsed) {
            team2ListSize = Math.min(team2.getMembers().size(), MEMBER_LIST_MAX_SIZE);
            team2ListSize--;
        }

        return team1ListSize + team2ListSize;
    }

    /**
     * This method deletes the old scoreboard object, if one exists.
     */
    public void removeScoreboard() {
        
        boardIsReady = false;
        
        if (obj != null) {
            obj.unregister();
            obj = null;
        }

        if (!teams.isEmpty()) {
            teams.forEach((k, v) -> v.unregister());
            teams.clear();
        }

    }
    
    /**
     * This method deletes the scoreboard timer, if one exists.
     */
    public void stopScoreboardTimer() {
        if (taskManager != null) {
            taskManager.stopTimer();
        }
    }

    /**
     * This method deletes the current scoreboard and creates a new one.
     */
    public void resetScoreboard() {
        removeScoreboard();
        createScoreboard();
    }

    /**
     * This method replaces the placeholders with the current value.
     *
     * @param text (String) the original config String
     *
     * @return the replaced text as String
     */
    private String replaceScoreboardPlaceholders(String text) {
        
        text = text.replace("%team1%", team1.getFullname());
        text = text.replace("%team2%", team2.getFullname());

        text = text.replace("%team1_color%", team1.getColor());
        text = text.replace("%team2_color%", team2.getColor());

        text = text.replace("%team1_amount%", Integer.toString(team1.getMembers().size()));
        text = text.replace("%team2_amount%", Integer.toString(team2.getMembers().size()));

        text = text.replace("%game_name%", game.getGameConfig().getDisplayName());
        text = text.replace("%arena_name%", arenaDisplayName);

        text = text.replace("%time%", Integer.toString(game.getGameDuration()));

        return text;
    }
    
    /**
     * This method gets the team page number for the scoreboard.
     * 
     * @param team the target MissileWars player team
     */
    public int getScoreboardTeamPage(Team team) {
        if (scoreboardTeamPage.containsKey(team)) {
            return scoreboardTeamPage.get(team);
        }
        return 0;
    }
    
    /**
     * This method increase the team page number to display more 
     * team-player with over the time in the scoreboard.
     * 
     * @param team the target MissileWars player team
     */
    public void increaseScoreboardTeamPage(Team team) {
        scoreboardTeamPage.put(team, scoreboardTeamPage.get(team) + 1);
    }
    
    /**
     * This method resets the team page number for the scoreboard
     * to restart with page '1' again.
     * 
      @param team the target MissileWars player team
     */
    public void resetScoreboardTeamPage(Team team) {
        scoreboardTeamPage.put(team, 1);
    }
    
}
