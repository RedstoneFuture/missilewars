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

package de.butzlabben.missilewars.listener;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class GameBoundListener implements Listener {

    private final Game game;

    protected GameBoundListener(Game game) {
        this.game = game;
    }

    public boolean isInLobbyArea(Location location) {
        return game.isInLobbyArea(location);
    }

    public boolean isInGameWorld(Location location) {
        return game.getGameWorld().isWorld(location.getWorld());
    }

    public Game getGame() {
        return game;
    }

    /**
     * This method gets the interaction protection variable for a player.
     *
     * @param player (Player) the target player
     */
    protected boolean isInteractDelay(Player player) {
        MWPlayer mwPlayer = getGame().getPlayer(player);
        if (mwPlayer == null) return false;

        return mwPlayer.isPlayerInteractEventCancel();
    }

    /**
     * This method sets an interaction protection variable for a player for
     * a short time.
     *
     * @param player (Player) the target player
     */
    protected void setInteractDelay(Player player) {
        MWPlayer mwPlayer = getGame().getPlayer(player);
        if (mwPlayer == null) return;

        mwPlayer.setPlayerInteractEventCancel(true);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> mwPlayer.setPlayerInteractEventCancel(false), 20);
    }
}
