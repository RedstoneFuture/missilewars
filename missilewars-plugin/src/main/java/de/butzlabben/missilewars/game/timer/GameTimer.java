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

import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.Game;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * @author Butzlabben
 * @since 06.01.2018
 */
public class GameTimer extends Timer {

    public GameTimer(Game game) {
        super(game);
        seconds = game.getArena().getGameDuration() * 60;
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
                broadcast(Messages.getMessage(true, Messages.MessageEnum.GAME_TIMER_GAME_ENDS_IN_MINUTES)
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
                broadcast(Messages.getMessage(true, Messages.MessageEnum.GAME_TIMER_GAME_ENDS_IN_SECONDS)
                        .replace("%seconds%", Integer.toString(seconds)));
                break;
            case 0:
                game.sendGameResult();
                game.stopGame();
                break;
            default:
                break;
        }

        if (seconds % 10 == 0) {
            game.getScoreboardManager().updateScoreboard();
        }
        
        if (seconds % 4 == 0) {
            game.getPlayers().values().forEach(mwPlayer -> {
                if (mwPlayer.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
                
                Player player = mwPlayer.getPlayer();
                if (game.isInGameArea(player.getLocation())) return;
                
                player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.ARENA_LEAVED));
                mwPlayer.getTeam().teleportToTeamSpawn(player);
            });
        }

        game.checkPortals();

        seconds--;
    }
}
