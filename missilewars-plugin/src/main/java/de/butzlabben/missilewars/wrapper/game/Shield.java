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

package de.butzlabben.missilewars.wrapper.game;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.arena.ShieldConfiguration;
import de.butzlabben.missilewars.wrapper.missile.paste.PasteProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import java.io.File;

/**
 * @author Butzlabben
 * @since 11.09.2018
 */
@RequiredArgsConstructor
public class Shield implements Listener {

    private final Player player;
    private final ShieldConfiguration shieldConfiguration;
    private org.bukkit.entity.Snowball ball;
    
    public void onThrow(ProjectileLaunchEvent e) {
        ball = (org.bukkit.entity.Snowball) e.getEntity();
        Bukkit.getPluginManager().registerEvents(this, MissileWars.getInstance());
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> {
            try {
                if (!ball.isDead())
                    paste();
                HandlerList.unregisterAll(this);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }, shieldConfiguration.getFlyTime());
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity().equals(ball) || ball == e.getEntity()) {
            HandlerList.unregisterAll(this);
            paste();
        }
    }

    public void paste() {
        Location loc = ball.getLocation();
        Vector pastePos = new Vector(loc.getX(), loc.getY(), loc.getZ());
        File schem = new File(MissileWars.getInstance().getDataFolder(), shieldConfiguration.getSchematic());

        PasteProvider.getPaster().pasteSchematic(schem, pastePos, loc.getWorld());
        VersionUtil.playSnowball(player, player.getLocation());
    }
}
