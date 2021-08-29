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

import de.butzlabben.missilewars.util.MathUtil;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.util.Vector;

@Getter
@AllArgsConstructor
@ToString
public class Plane {

    private final Vector support, normal;

    public boolean isIn(Vector point) {
        return MathUtil.closeEnoughEquals(point.clone().subtract(support).dot(normal), 0);
    }

    public Vector closestPointTo(Vector point) {
        if (isIn(point)) return point.clone();

        Line supportLine = new Line(point, normal);
        // we can safely get the value, as we know that this plane and the line are not parallel
        return getBreakThroughPoint(supportLine).get();
    }

    public double distance(Vector point) {
        Vector closestPoint = closestPointTo(point);
        return point.distance(closestPoint);
    }

    public double distanceSquared(Vector point) {
        Vector closestPoint = closestPointTo(point);
        return point.distanceSquared(closestPoint);
    }

    public Optional<Vector> getBreakThroughPoint(Line line) {
        if (MathUtil.closeEnoughEquals(line.getDirection().dot(normal), 0)) return Optional.empty();
        double d = support.dot(normal);
        double a = normal.getX();
        double b = normal.getY();
        double c = normal.getZ();
        Vector x = line.getSupport();
        Vector y = line.getDirection();
        double t = (d - a * x.getX() - b * x.getY() - c * x.getZ()) / (a * y.getX() + b * y.getY() + c * y.getZ());
        Vector result = line.getSupport().add(line.getDirection().multiply(t)).clone();
        return Optional.of(result);
    }
}
