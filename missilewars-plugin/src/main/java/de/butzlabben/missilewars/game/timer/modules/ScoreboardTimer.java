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

package de.butzlabben.missilewars.game.timer.modules;

import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.misc.ScoreboardManager;
import de.butzlabben.missilewars.game.timer.Timer;

/**
 * @author Butzlabben
 * @since 11.01.2018
 */
public class ScoreboardTimer extends Timer {
    
    public ScoreboardTimer(Game game) {
        super(game, 0);
        resetSeconds();
    }

    @Override
    public void tick() {
        ScoreboardManager scoreboard = getGame().getScoreboardManager();
        
        if (!scoreboard.isBoardIsReady()) return;
        
        // Updating the scoreboard:
        scoreboard.updateScoreboard();
        scoreboard.increaseScoreboardTeamPage(getGame().getTeamManager().getTeam1());
        scoreboard.increaseScoreboardTeamPage(getGame().getTeamManager().getTeam2());
    }
    
}
