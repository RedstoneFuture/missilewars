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
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.arena.AreaConfiguration;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.util.geometry.GameArea;
import de.butzlabben.missilewars.util.serialization.Serializer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class Lobby {

    // The values defined here are only valid if there is no Config yet.
    private String name = "lobby0";
    @SerializedName("display_name") private String displayName = "&eDefault game";
    @SerializedName("auto_load") private boolean autoLoad = true;
    @SerializedName("world") private String worldName = getBukkitDefaultWorld().getName();
    @SerializedName("lobby_time") private int lobbyTime = 60;
    @SerializedName("join_ongoing_game") private boolean joinOngoingGame = false;
    @SerializedName("min_size") private int minSize = 2;
    @SerializedName("max_size") private int maxSize = 20;
    @SerializedName("team1_name") private String team1Name = "Team1";
    @SerializedName("team1_color") private String team1Color = "&c";
    @SerializedName("team2_name") private String team2Name = "Team2";
    @SerializedName("team2_color") private String team2Color = "&a";
    @Setter @SerializedName("spawn_point") private Location spawnPoint = getBukkitDefaultWorld().getSpawnLocation();
    @Setter @SerializedName("after_game_spawn") private Location afterGameSpawn = getBukkitDefaultWorld().getSpawnLocation();
    @Setter @SerializedName("area") private AreaConfiguration areaConfig = AreaConfiguration.aroundLocation(getBukkitDefaultWorld().getSpawnLocation(), 30);
    @SerializedName("map_choose_procedure") private MapChooseProcedure mapChooseProcedure = MapChooseProcedure.MAPCYCLE;
    @SerializedName("possible_arenas") private List<String> possibleArenas = new ArrayList<>() {{
        add("arena0");
        add("dam");
    }};

    // These values are only set after the Config has been read.
    @Setter private transient GameArea area;
    @Setter private transient File file;

    public World getBukkitWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Logger.ERROR.log("Could not find any world with the name: " + worldName);
            Logger.ERROR.log("Please correct this in the configuration of lobby \"" + name + "\"");
        }
        return world;
    }

    private World getBukkitDefaultWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public List<Arena> getArenas() {
        return possibleArenas
                .stream()
                .map(Arenas::getFromName)
                .collect(Collectors.toList());
    }

    public void updateConfig() {
        try {
            Serializer.serialize(file, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
