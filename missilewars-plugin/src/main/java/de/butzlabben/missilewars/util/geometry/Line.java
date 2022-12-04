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

import de.butzlabben.missilewars.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.util.Vector;

/**
 * A 3-dimensional line in the form:
 * supporter + t*direction
 */
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Line {

    private final Vector support, direction;

    public static Line fromPoints(final Vector point1, final Vector point2) {
        return new Line(point1.clone(), point2.clone().subtract(point1.clone()));
    }

    public boolean isIn(final Vector point) {
        final double tX = this.getTEquationSolved(this.support.getX(), this.direction.getX(), point.getX());
        final double tY = this.getTEquationSolved(this.support.getY(), this.direction.getY(), point.getY());
        final double tZ = this.getTEquationSolved(this.support.getZ(), this.direction.getZ(), point.getZ());
        return MathUtil.closeEnoughEquals(tX, tY) && MathUtil.closeEnoughEquals(tX, tZ) && MathUtil.closeEnoughEquals(tY, tZ);
    }

    public double distance(final Vector point) {
        final Vector closestPoint = this.closestPointTo(point);
        return point.distance(closestPoint);
    }

    public Vector closestPointTo(final Vector point) {
        if (this.isIn(point)) return point.clone();
        final Plane helperPlane = new Plane(point, this.direction);
        return helperPlane.getBreakThroughPoint(this).get();
    }

    private double getTEquationSolved(final double supportValue, final double directionValue, final double pointValue) {
        return (pointValue - supportValue) / directionValue;
    }

    public Vector getSupport() {
        return this.support.clone();
    }

    public Vector getDirection() {
        return this.direction.clone();
    }
}
