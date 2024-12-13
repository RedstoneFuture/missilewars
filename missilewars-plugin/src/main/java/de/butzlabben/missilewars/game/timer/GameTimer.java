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

import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.redstoneworld.redutilities.player.Messages;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * @author Butzlabben
 * @since 06.01.2018
 */
public class GameTimer extends Timer {
    
    int actionbarMsgCounter = 0;
    
    public GameTimer(Game game) {
        super(game);
        seconds = game.getArenaConfig().getGameDuration() * 60;
    }

    @Override
    public void tick() {
        Game game = getGame();

        switch (seconds) {
            case 7200:
            case 5400:
            case 3600:
            case 1800:
            case 1200:
            case 600:
            case 300:
            case 180:
                broadcast(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_TIMER_GAME_ENDS_IN_MINUTES)
                        .replace("%minutes%", Integer.toString(seconds / 60)));
                break;
            case 60:
            case 30:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
                broadcast(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_TIMER_GAME_ENDS_IN_SECONDS)
                        .replace("%seconds%", Integer.toString(seconds)));
                break;
            case 0:
                game.sendGameResult();
                game.stopGame();
                break;
            default:
                break;
        }

        if (seconds % 5 == 0) {
            game.getScoreboardManager().updateScoreboard();
            
            game.getPlayers().values().forEach(mwPlayer -> {
                Player player = mwPlayer.getPlayer();
                
                if (mwPlayer.getTeam().getTeamType() == TeamType.PLAYER) {
                    
                    if (mwPlayer.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
                    
                    if (game.isInGameArea(player.getLocation())) return;
                    
                    player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.ARENA_LEAVED));
                    mwPlayer.getTeam().teleportToTeamSpawn(player);
                    
                }
            
            });
        }
        
        if ((Config.getActionbarForSpecEntries().length > 0) && (seconds % Config.getActionbarForSpecDelay() == 0)) {
            game.getPlayers().values().forEach(mwPlayer -> {
                Player player = mwPlayer.getPlayer();
                
                if (mwPlayer.getTeam().getTeamType() == TeamType.PLAYER) return;
                Messages.sendActionbarMsg(player, Config.getActionbarForSpecEntries()[actionbarMsgCounter]);
            });
            
            // Array-Iteration:
            if (actionbarMsgCounter >= Config.getActionbarForSpecEntries().length - 1) {
                actionbarMsgCounter = 0;
            } else {
                actionbarMsgCounter++;
            }
        }

        game.checkPortals();

        seconds--;
    }
}
