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

package de.butzlabben.missilewars.configuration.lobby;

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.arena.AreaConfiguration;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.enums.JoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.game.enums.RejoinIngameBehavior;
import de.butzlabben.missilewars.util.geometry.GameArea;
import de.butzlabben.missilewars.util.serialization.Serializer;
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
    @SerializedName("min_players") private int minPlayers = 2;
    @SerializedName("max_players") private int maxPlayers = 20;
    @SerializedName("max_spectators") private int maxSpectators = -1;
    @SerializedName("team_1") private GameTeamConfiguration team1Config = new GameTeamConfiguration("Team1", "&c");
    @SerializedName("team_2") private GameTeamConfiguration team2Config = new GameTeamConfiguration("Team2", "&a");
    @SerializedName("team_spectator") private GameTeamConfiguration teamConfigSpec = new GameTeamConfiguration("Spectator", "&f");
    @Setter @SerializedName("spawn_point") private Location spawnPoint = Config.getFallbackSpawn().add(40, 0, 0);
    @Setter @SerializedName("after_game_spawn") private Location afterGameSpawn = Config.getFallbackSpawn();
    @Setter @SerializedName("area") private AreaConfiguration areaConfig = AreaConfiguration.aroundLocation(spawnPoint, 20);
    @SerializedName("map_choose_procedure") private MapChooseProcedure mapChooseProcedure = MapChooseProcedure.FIRST;
    @SerializedName("join_ongoing_game") private JoinIngameBehavior joinIngameBehavior = JoinIngameBehavior.SPECTATOR;
    @SerializedName("rejoin_ongoing_game") private RejoinIngameBehavior rejoinIngameBehavior = RejoinIngameBehavior.LAST_TEAM;
    @SerializedName("possible_arenas") private List<String> possibleArenas = new ArrayList<>() {{
        add("arena0");
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
