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

import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.player.MWPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

/**
 * @author Butzlabben
 * @since 14.01.2018
 */

@RequiredArgsConstructor
@Getter
public abstract class Timer implements Runnable {

    private final Game game;
    public final int initialSeconds;
    public int seconds;

    @Override
    public void run() {
        tick();
    }

    public abstract void tick();
    
    protected void sendBroadcast(String message) {
        game.broadcast(message);
    }
    
    protected void resetSeconds() {
        seconds = initialSeconds;
    }
    
    protected void setLevel(int level) {
        for (MWPlayer mwPlayer : getGame().getPlayers().values()) {
            mwPlayer.getPlayer().setLevel(level);
        }
    }
    
    protected void playPling() {
        for (MWPlayer mwPlayer : getGame().getPlayers().values()) {
            mwPlayer.getPlayer().playSound(mwPlayer.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100, 3);
        }
    }
}
