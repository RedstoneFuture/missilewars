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

package de.butzlabben.missilewars.wrapper.player;

import java.util.Objects;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */
public class Interval {

    private int min;
    private int max;

    public Interval(int min, int max) {
        this.max = max;
        this.min = min;
        if (min > max)
            throw new IllegalArgumentException("Min value must be higher than max value");
    }

    public boolean isIn(double d) {
        return d >= min && d <= max;
    }

    public Interval add(double d) {
        return new Interval(min += d, max += d);
    }

    public boolean isIn(int d) {
        return d >= min && d <= max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof Interval) {
            Interval i = (Interval) paramObject;
            return i.max == max && i.min == min;
        }
        return false;
    }
}
