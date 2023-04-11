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

import org.bukkit.Location;
import org.bukkit.World;

public class Geometry {

    /**
     * This method checks if a location is closer to a target location
     * than another location.
     *
     * @param targetLocation (Location) the start location for the distance measure
     * @param closerLocation (Location) the closer location
     * @param furtherAwayLocation (Location) the location that is further away
     * @return true, if the statement is correct
     */
    public static boolean isCloser(Location targetLocation, Location closerLocation, Location furtherAwayLocation) {
        return targetLocation.distanceSquared(closerLocation) < targetLocation.distanceSquared(furtherAwayLocation);
    }

    /**
     * This method checks if a location is within an arena. The border of the
     * arena (1 block wide) is still part of the arena (= inside).
     *
     * @param targetLocation (Location) the location to be checked
     * @param area (Location) the arena, which should be around the location
     * @return true, if the statement is correct
     */
    public static boolean isInsideIn(Location targetLocation, GameArea area) {

        if (!Geometry.bothLocInSameWorld(targetLocation, area.getPosition1())) return false;

        if (targetLocation.getBlockX() > area.getMaxX()) return false;
        if (targetLocation.getBlockX() < area.getMinX()) return false;

        if (targetLocation.getBlockY() > area.getMaxY()) return false;
        if (targetLocation.getBlockY() < area.getMinY()) return false;

        if (targetLocation.getBlockZ() > area.getMaxZ()) return false;
        return targetLocation.getBlockZ() >= area.getMinZ();
    }

    /**
     * This method checks, if both locations are in the same world.
     * 
     * @param pos1 (Location) location 1
     * @param pos2 (Location) location 2
     * @return true, if they are in the same world
     */
    public static boolean bothLocInSameWorld(Location pos1, Location pos2) {
        if ((pos1.getWorld() == null) || (pos2.getWorld() == null)) return false;
        return pos1.getWorld().getName().equals(pos2.getWorld().getName());
    }

    /**
     * This method checks if a location is in a specified world.
     * 
     * @param targetLocation (Location) the location to be checked
     * @param world (World) the target world
     * @return true, if the statement is correct
     */
    public static boolean isInWorld(Location targetLocation, World world) {
        return targetLocation.getWorld().getName().equals(world.getName());
    }
    
}
