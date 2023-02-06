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

package de.butzlabben.missilewars.configuration.arena;

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.game.GameWorld;
import de.butzlabben.missilewars.util.geometry.ImprovedArea;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@ToString
@RequiredArgsConstructor
public class AreaArenaConfiguration {
    
    @SerializedName("min_x") private int minX = -50;
    @SerializedName("min_y") private int minY = 0;
    @SerializedName("min_z") private int minZ = -50;
    @SerializedName("max_x") private int maxX = 50;
    @SerializedName("max_y") private int maxY = 256;
    @SerializedName("max_z") private int maxZ = 50;

/*    public AreaArenaConfiguration(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static AreaArenaConfiguration deserialize(Map<String, Object> serialized) {
        int minX = (int) serialized.get("min_x");
        int minY = (int) serialized.get("min_y");
        int minZ = (int) serialized.get("min_z");

        int maxX = (int) serialized.get("max_x");
        int maxY = (int) serialized.get("max_y");
        int maxZ = (int) serialized.get("max_z");
        return new AreaArenaConfiguration(minX, minY, minZ, maxX, maxY, maxZ);
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
    }*/
    
    public ImprovedArea getArea(GameWorld gameWorld) {
        World world = gameWorld.getWorld();
        
        Location pos1 = new Location(world, minX, minY, minZ);
        Location pos2 = new Location(world, maxX, maxY, maxZ);
        
        return new ImprovedArea(pos1, pos2);
    }
    
}
