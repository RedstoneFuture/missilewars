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

package de.butzlabben.missilewars.game.timer;

import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;

/**
 * @author Butzlabben
 * @since 11.01.2018
 */
public class LobbyTimer extends Timer {
    
    public LobbyTimer(Game game, int startTime) {
        super(game, startTime);
        resetSeconds();
    }

    @Override
    public void tick() {
        if (getGame().getPlayers().isEmpty()) return;

        // Displaying countdown:
        setLevel(seconds);
        
        // Checking team size:
        if ((getGame().getTeamManager().hasEmptyPlayerTeam()) || (getGame().areToFewPlayers())) {
            resetSeconds();
            return;
        }
        
        // Sending start time info:
        switch (seconds) {
            case 120:
            case 60:
            case 30:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
                sendBroadcast(PluginMessages.getMessage(true, PluginMessages.MessageEnum.LOBBY_TIMER_GAME_STARTS_IN)
                        .replace("%seconds%", Integer.toString(seconds)));
                playPling();
                break;
            default:
                break;
        }
        
        // Executing other checks and the initial Game start:
        switch (seconds) {
            case 10:
                if (getGame().getGameConfig().getMapChooseProcedure() == MapChooseProcedure.MAPVOTING) {
                    getGame().getMapVoting().setVotedArena();
                }
                break;
            case 0:
                if (!getGame().getTeamManager().hasBalancedTeamSizes()) {
                    sendBroadcast(PluginMessages.getMessage(true, PluginMessages.MessageEnum.LOBBY_TEAMS_UNEQUAL));
                    resetSeconds();
                    return;
                }
                executeGameStart();
                return;
            default:
                break;
        }

        seconds--;
    }
    
    /**
     * This method executes the game start. In addition, the participants
     * are informed about the start.
     */
    public void executeGameStart() {
        sendBroadcast(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_GAME_STARTS));
        getGame().startGame();
    }
    
}
