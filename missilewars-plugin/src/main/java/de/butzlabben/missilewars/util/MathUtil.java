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

package de.butzlabben.missilewars.util;

import org.bukkit.util.Vector;

public class MathUtil {

    /**
     * Checks if two doubles are close enough to be considered "equal".
     * As we have a limited precision, the normal "==" operator would make some of our math not working.
     * As long as the difference is smaller than 1.0E-8D, it will return true. This value was chosen, as
     * {@link org.bukkit.util.Vector#equals(Object)} uses a more losen tolerance.
     *
     * @param value1 the first double
     * @param value2 the second double
     *
     * @return true if the double values are close enough to be considered equal
     */
    public static boolean closeEnoughEquals(final double value1, final double value2) {
        return Math.abs(value1 - value2) < 1.0E-8D;
    }


    public static boolean areMultiples(final Vector vector1, final Vector vector2) {
        double factor = 0;
        if (vector1.getX() != 0 && vector2.getX() != 0) factor = vector1.getX() / vector2.getX();
        if (vector1.getY() != 0 && vector2.getY() != 0 && factor != 0) factor = vector1.getY() / vector2.getY();
        if (vector1.getZ() != 0 && vector2.getZ() != 0 && factor != 0) factor = vector1.getZ() / vector2.getZ();
        return vector1.equals(vector2.clone().multiply(factor));
    }
}
