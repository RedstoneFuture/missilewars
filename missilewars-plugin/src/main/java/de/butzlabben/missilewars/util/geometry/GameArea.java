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

    private Direction direction;
    
    /**
     * This method creates a new GameArena object.
     *
     * The GameArena is a rectangular area. Its border (1 block wide) is 
     * still part of the arena (= inside).
     *
     * @param pos1 (Location) one corner of the desired area border
     * @param pos2 (Location) the opposite corner of the desired area border
     */
    public GameArea(Location pos1, Location pos2) {
        
        if (!Geometry.bothLocInSameWorld(pos1, pos2)) throw new IllegalArgumentException("Defined positions are not in the same world!");
        if (pos1.equals(pos2)) throw new IllegalArgumentException("The selected positions do not differ.");
        
        this.world = pos1.getWorld();
        
        this.position1 = pos1;
        this.position2 = pos2;
        
        initialize();
    }

    /**
     * This method creates a new GameArena object.
     *
     * The GameArena is a rectangular area. Its border (1 block wide) is 
     * still part of the arena (= inside).
     *
     * @param center (Location) the horizontal center of the desired area; it's also the vertical minimum of the area (the floor)
     * @param offset (int) the horizontal offset, which should go from the center to the end of the desired area
     * @param height (int) the height of the desired area
     */
    public GameArea(Location center, int offset, int height) {
        
        if (offset < 1) throw new IllegalArgumentException("The offset must be higher than 0.");
        if (height < 2) throw new IllegalArgumentException("The height must be higher than 1.");
        
        this.world = center.getWorld();
        
        long x1 = center.getBlockX() + offset;
        long x2 = center.getBlockX() - offset;
        long z1 = center.getBlockZ() + offset;
        long z2 = center.getBlockZ() - offset;

        long y1 = center.getBlockY();
        long y2 = y1 + height;

        this.position1 = new Location(center.getWorld(), x1, y1, z1);
        this.position2 = new Location(center.getWorld(), x2, y2, z2);

        initialize();
    }

    /**
     * This method creates a new GameArena object.
     *
     * The GameArena is a rectangular area. Its border (1 block wide) is 
     * still part of the arena (= inside).
     *
     * @param world (World) the target world for the desired area
     * @param areaConfig (AreaConfiguration) the loaded Area-Configuration from which the data is taken
     */
    public GameArea(World world, AreaConfiguration areaConfig) {
        
        this.world = world;
        
        this.position1 = new Location(world, areaConfig.getMinX(), areaConfig.getMinY(), areaConfig.getMinZ());
        this.position2 = new Location(world, areaConfig.getMaxX(), areaConfig.getMaxY(), areaConfig.getMaxZ());

        if (position1.equals(position2)) throw new IllegalArgumentException("The selected positions do not differ.");
        
        initialize();
    }

    /**
     * This method calculates and saves the MIN and MAX positions 
     * according to the current values. The assigned MIN and MAX 
     * information can be used to later compare the GameArea more 
     * easily with current live positions/areas. In addition, the 
     * area direction is calculated afterwards.
     */
    private void initialize() {

        // Calculation of min & max X coordinate:
        if (position1.getBlockX() < position2.getBlockX()) {
            minX = position1.getBlockX();
            maxX = position2.getBlockX();
        } else {
            maxX = position1.getBlockX();
            minX = position2.getBlockX();
        }

        // Calculation of min & max Y coordinate:
        if (position1.getBlockY() < position2.getBlockY()) {
            minY = position1.getBlockY();
            maxY = position2.getBlockY();
        } else {
            maxY = position1.getBlockY();
            minY = position2.getBlockY();
        }

        // Calculation of min & max Z coordinate:
        if (position1.getBlockZ() < position2.getBlockZ()) {
            minZ = position1.getBlockZ();
            maxZ = position2.getBlockZ();
        } else {
            maxZ = position1.getBlockZ();
            minZ = position2.getBlockZ();
        }

        // Calculation of area direction:
        if (getXSize() < getZSize()) {
            direction = Direction.NORTH_SOUTH;
        } else {
            direction = Direction.EAST_WEST;
        }
    }

    public AreaConfiguration getAreaConfiguration() {
        AreaConfiguration newAreaConfig = new AreaConfiguration(position1.getBlockX(), position1.getBlockY(), position1.getBlockZ(), 
                position2.getBlockX(), position2.getBlockY(), position2.getBlockZ());
        return newAreaConfig;
    }

    /**
     * This method defines the horizontal direction / rotation of the 
     * area based on the alignment of the team spawn points.
     * 
     * NORTH-SOUTH = primarily along the Z axis
     * EAST-WEST = primarily along the X axis
     */
    public enum Direction {
        NORTH_SOUTH,
        EAST_WEST
    }

    /**
     * This method returns the arena length along the X coordinate.
     * 
     * @return (Integer) the X size
     */
    public int getXSize() {
        return maxX - minX;
    }

    /**
     * This method returns the arena length along the Y coordinate.
     *
     * @return (Integer) the Y size
     */
    public int getYSize() {
        return maxY - minY;
    }

    /**
     * This method returns the arena length along the Z coordinate.
     *
     * @return (Integer) the Z size
     */
    public int getZSize() {
        return maxZ - minZ;
    }
    
}