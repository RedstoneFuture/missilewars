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

package de.butzlabben.missilewars.configuration;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@AllArgsConstructor
public class AreaConfig implements ConfigurationSerializable {

    @SerializedName("min_x") private int minX;
    @SerializedName("min_y") private int minY;
    @SerializedName("min_z") private int minZ;
    @SerializedName("max_x") private int maxX;
    @SerializedName("max_y") private int maxY;
    @SerializedName("max_z") private int maxZ;

    /**
     * Creates a quadratic area around the given location with the specified margin.
     * The height will go from 0 to 256.
     *
     * @param location the location to put the area around.
     * @param margin   the distance between the border of the area and the location.
     *
     * @return an area configuration around the location
     */
    public static AreaConfig aroundLocation(Location location, int margin) {
        return new AreaConfig(location.getBlockX() - margin,
                0,
                location.getBlockZ() - margin,
                location.getBlockX() + margin,
                256,
                location.getBlockZ() + margin
        );
    }

    /**
     * This method is used to save the config entries in the config file.
     */
    @Override
    @NotNull
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
