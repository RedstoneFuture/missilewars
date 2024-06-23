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

import de.butzlabben.missilewars.game.Game;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * @author Butzlabben
 * @since 11.09.2018
 */
@RequiredArgsConstructor
public class ShieldListener implements Listener {

    private final Player player;
    private final Game game;
    private final Snowball snowball;
    
    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!event.getEntity().equals(snowball)) return;

        HandlerList.unregisterAll(this);
        game.spawnShield(player, snowball);
    }
    
}
