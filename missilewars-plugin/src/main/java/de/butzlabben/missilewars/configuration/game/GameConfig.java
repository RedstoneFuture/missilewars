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

package de.butzlabben.missilewars.configuration.game;

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.configuration.arena.ArenaConfig;
import de.butzlabben.missilewars.configuration.game.modules.GameTeamConfig;
import de.butzlabben.missilewars.configuration.game.modules.LobbyConfig;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class GameConfig {

    // The values defined here are only valid if there is no Config yet.
    private String name = "game1";
    @SerializedName("display_name") private String displayName = "&eDefault game";
    @SerializedName("auto_load") private boolean autoLoad = true;
    @SerializedName("min_players") private int minPlayers = 2;
    @SerializedName("max_players") private int maxPlayers = 20;
    @SerializedName("max_spectators") private int maxSpectators = -1;
    @SerializedName("team_1") private GameTeamConfig team1Config = new GameTeamConfig("Team1", "&c");
    @SerializedName("team_2") private GameTeamConfig team2Config = new GameTeamConfig("Team2", "&a");
    @SerializedName("team_spectator") private GameTeamConfig teamConfigSpec = new GameTeamConfig("Spectator", "&f");
    @SerializedName("map_choose_procedure") private MapChooseProcedure mapChooseProcedure = MapChooseProcedure.FIRST;
    @SerializedName("join_ongoing_game") private JoinIngameBehavior joinIngameBehavior = JoinIngameBehavior.SPECTATOR;
    @SerializedName("rejoin_ongoing_game") private RejoinIngameBehavior rejoinIngameBehavior = RejoinIngameBehavior.LAST_TEAM;
    @SerializedName("lobby") private LobbyConfig lobbyConfig = new LobbyConfig();
    @SerializedName("possible_arenas") private List<String> possibleArenas = new ArrayList<>() {{
        add("arena1");
    }};

    // These values are only set after the Config has been read.
    @Setter private transient GameArea area;
    @Setter private transient File file;
    
    
    public List<ArenaConfig> getArenas() {
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
