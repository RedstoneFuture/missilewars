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

import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.game.Game;

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
        if (seconds == 7200) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "120"));
        } else if (seconds == 5400) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "90"));
        } else if (seconds == 3600) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "60"));
        } else if (seconds == 1800) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "30"));
        } else if (seconds == 1200) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "20"));
        } else if (seconds == 600) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "10"));
        } else if (seconds == 300) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "5"));
        } else if (seconds == 180) {
            broadcast(MessageConfig.getMessage("game_ends_in_minutes").replace("%minutes%", "3"));
        } else if (seconds == 60) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "60"));
        } else if (seconds == 30) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "30"));
        } else if (seconds == 10) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "10"));
        } else if (seconds == 5) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "5"));
        } else if (seconds == 4) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "4"));
        } else if (seconds == 3) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "3"));
        } else if (seconds == 2) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "2"));
        } else if (seconds == 1) {
            broadcast(MessageConfig.getMessage("game_ends_in_seconds").replace("%seconds%", "1"));
        } else if (seconds == 0) {
            getGame().stopGame();
        }
        if (seconds % 10 == 0)
            getGame().getScoreboardManager().updateScoreboard();
        --seconds;
    }
}
