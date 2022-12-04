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

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@ToString
@AllArgsConstructor
@Getter
public class Area implements ConfigurationSerializable {

    @SerializedName("min_x") private int minX;
    @SerializedName("min_y") private int minY;
    @SerializedName("min_z") private int minZ;
    @SerializedName("max_x") private int maxX;
    @SerializedName("max_y") private int maxY;
    @SerializedName("max_z") private int maxZ;
    private transient boolean checked;

    public static Area deserialize(Map<String, Object> serialized) {
        int minX = (int) serialized.get("min_x");
        int minY = (int) serialized.get("min_y");
        int minZ = (int) serialized.get("min_z");

        int maxX = (int) serialized.get("max_x");
        int maxY = (int) serialized.get("max_y");
        int maxZ = (int) serialized.get("max_z");
        return new Area(minX, minY, minZ, maxX, maxY, maxZ, false);
    }

    public static Area defaultAreaAround(Location location) {
        return new Area(location.getBlockX() - 20,
                location.getBlockY() - 20,
                location.getBlockZ() - 20,
                location.getBlockX() + 20,
                location.getBlockY() + 20,
                location.getBlockZ() + 20, true);
    }

    public boolean isInArea(double x, double y, double z) {
        checkValues();
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    public boolean isInArea(Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return isInArea(x, y, z);
    }

    void checkValues() {
        if (checked)
            return;

        if (minX >= maxX) {
            int oldMin = minX;
            this.minX = maxX;
            this.maxX = oldMin;
        }

        if (minY >= maxY) {
            int oldMin = minY;
            this.minY = maxY;
            this.maxY = oldMin;
        }

        if (minZ >= maxZ) {
            int oldMin = minZ;
            this.minZ = maxZ;
            this.maxZ = oldMin;
        }
        checked = true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("min_x", minX);
        serialized.put("min_y", minY);
        serialized.put("min_z", minZ);
        serialized.put("max_x", maxX);
        serialized.put("max_y", maxY);
        serialized.put("max_z", maxZ);
        return serialized;
    }
}
