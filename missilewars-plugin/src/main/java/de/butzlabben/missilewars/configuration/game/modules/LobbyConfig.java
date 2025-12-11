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

package de.butzlabben.missilewars.configuration.game.modules;

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.AreaConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@ToString
@RequiredArgsConstructor
public class LobbyConfig {
    
    @SerializedName("world") private String worldName = getBukkitDefaultWorld().getName();
    @SerializedName("lobby_time") private int lobbyTime = 60;
    @Setter @SerializedName("spawn_point") private Location spawnPoint = Config.getFallbackSpawn().add(40, 0, 0);
    @Setter @SerializedName("after_game_spawn") private Location afterGameSpawn = Config.getFallbackSpawn();
    @Setter @SerializedName("area") private AreaConfig areaConfig = AreaConfig.aroundLocation(spawnPoint, 20);
    
    private World getBukkitDefaultWorld() {
        return Bukkit.getWorlds().get(0);
    }
    
    public World getBukkitWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Logger.ERROR.log("Could not find any world with the name: " + worldName);
            Logger.ERROR.log("Please correct this in the configuration.");
        }
        return world;
    }
}