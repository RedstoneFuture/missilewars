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

package de.butzlabben.missilewars.wrapper.geometry;


import org.bukkit.Location;

public class FlatArea extends Area {

    public FlatArea(int minX, int minZ, int maxX, int maxZ) {
        super(minX, 0, minZ, maxX, 0, maxZ, false);
    }

    public boolean isInArea(double x, double z) {
        return super.isInArea(x, 0, z);
    }

    @Override
    public boolean isInArea(Location loc) {
        long x = Math.round(loc.getX());
        long z = Math.round(loc.getZ());
        return isInArea(x, z);
    }
}
