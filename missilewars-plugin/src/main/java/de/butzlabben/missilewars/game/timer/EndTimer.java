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
 * @since 14.01.2018
 */
public class EndTimer extends Timer {

    public EndTimer(Game game) {
        super(game);
        seconds = 21;
    }

    @Override
    public void tick() {

        switch(seconds) {
            case 15:
                broadcast(MessageConfig.getMessage("game_starts_new_in").replace("%seconds%", Integer.toString(seconds)));
                break;
            case 0:
                getGame().reset();
                break;
            default:
                break;
        }

        seconds--;
    }

}
