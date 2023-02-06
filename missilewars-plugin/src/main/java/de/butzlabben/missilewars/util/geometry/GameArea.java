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

package de.butzlabben.missilewars.util.geometry;

import de.butzlabben.missilewars.configuration.arena.AreaConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
public class GameArea {

    private World world;
    @Setter private Location position1, position2;

    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    public GameArea(Location pos1, Location pos2) {
        
        if (!Geometry.bothLocInSameWorld(pos1, pos2)) throw new IllegalArgumentException("Defined positions are not in the same world!");
        if (pos1 == pos2) throw new IllegalArgumentException("The selected positions do not differ.");
        
        this.world = pos1.getWorld();
        
        this.position1 = pos1;
        this.position2 = pos2;
        
        calculate();
    }
    
    public GameArea(Location center, int offset, int high) {
        
        if (offset < 1) throw new IllegalArgumentException("The offset must be higher than 0.");
        if (high < 2) throw new IllegalArgumentException("The high must be higher than 1.");
        
        this.world = center.getWorld();
        
        long x1 = center.getBlockX() + offset;
        long x2 = center.getBlockX() - offset;
        long z1 = center.getBlockZ() + offset;
        long z2 = center.getBlockZ() - offset;

        long y1 = center.getBlockY();
        long y2 = y1 + high;

        this.position1 = new Location(center.getWorld(), x1, y1, z1);
        this.position2 = new Location(center.getWorld(), x2, y2, z2);

        calculate();
    }
    
    public GameArea(World world, AreaConfiguration areaConfig) {
        
        this.world = world;
        
        this.position1 = new Location(world, areaConfig.getMinX(), areaConfig.getMinY(), areaConfig.getMinZ());
        this.position2 = new Location(world, areaConfig.getMaxX(), areaConfig.getMaxY(), areaConfig.getMaxZ());

        if (position1 == position2) throw new IllegalArgumentException("The selected positions do not differ.");
        
        calculate();
    }

    private void calculate() {

        if (position1.getBlockX() < position2.getBlockX()) {
            minX = position1.getBlockX();
            maxX = position2.getBlockX();
        } else {
            maxX = position1.getBlockX();
            minX = position2.getBlockX();
        }

        if (position1.getBlockY() < position2.getBlockY()) {
            minY = position1.getBlockY();
            maxY = position2.getBlockY();
        } else {
            maxY = position1.getBlockY();
            minY = position2.getBlockY();
        }

        if (position1.getBlockZ() < position2.getBlockZ()) {
            minZ = position1.getBlockZ();
            maxZ = position2.getBlockZ();
        } else {
            maxZ = position1.getBlockZ();
            minZ = position2.getBlockZ();
        }
    }

    public AreaConfiguration getAreaConfiguration() {
        AreaConfiguration newAreaConfig = new AreaConfiguration(position1.getBlockX(), position1.getBlockY(), position1.getBlockZ(), 
                position2.getBlockX(), position2.getBlockY(), position2.getBlockZ());
        return newAreaConfig;
    }

    private enum Direction {
        NULL,
        NORTH_SOUTH,
        EAST_WEST;

    }
}