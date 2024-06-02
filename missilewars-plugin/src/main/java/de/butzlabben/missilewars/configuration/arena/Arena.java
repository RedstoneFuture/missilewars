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
import de.butzlabben.missilewars.util.geometry.GameArea;
import de.butzlabben.missilewars.util.serialization.Serializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;

@Getter
@ToString
public class Arena implements Cloneable {

    // The values defined here are only valid if there is no Config yet.
    private String name = "arena0";
    @SerializedName("display_name") private String displayName = "&eDefault map";
    @SerializedName("display_material") private String displayMaterial = "STONE";
    @SerializedName("template_world") private String templateWorld = "default_map";
    @SerializedName("auto_respawn") private boolean autoRespawn = true;
    @SerializedName("game_spawn") private GameSpawnConfiguration spawn = new GameSpawnConfiguration();
    @SerializedName("game_respawn") private GameRespawnConfiguration respawn = new GameRespawnConfiguration();
    @SerializedName("do_tile_drops") private boolean doTileDrops = false;
    @SerializedName("keep_inventory") private boolean keepInventory = false;
    @SerializedName("max_move_height") private int maxMoveHeight = 170;
    @SerializedName("death_height") private int deathHeight = 65;
    @SerializedName("game_duration") private int gameDuration = 30;
    @SerializedName("fireball") private FireballConfiguration fireballConfiguration = new FireballConfiguration();
    @SerializedName("arrow") private ArrowConfiguration arrowConfiguration = new ArrowConfiguration();
    @SerializedName("save_statistics") private boolean saveStatistics = true;
    @SerializedName("fall_protection") private FallProtectionConfiguration fallProtection = new FallProtectionConfiguration();
    @SerializedName("money") private MoneyConfiguration money = new MoneyConfiguration();
    @SerializedName("equipment_interval") private EquipmentIntervalConfiguration interval = new EquipmentIntervalConfiguration();
    @SerializedName("missile") private MissileConfiguration missileConfiguration = new MissileConfiguration();
    @SerializedName("shield") private ShieldConfiguration shieldConfiguration = new ShieldConfiguration();
    @Setter @SerializedName("area") private AreaConfiguration areaConfig = new AreaConfiguration(-30, 0, -72, 30, 256, 72);
    @SerializedName("teamchange_ongoing_game") private boolean teamchangeOngoingGame = false;

    @SerializedName("spectator_spawn")
    @Setter
    private Location spectatorSpawn = new Location(null, 0, 100, 0, 90, 0);

    @SerializedName("team1_spawn")
    @Setter
    private Location team1Spawn = new Location(null, 0.5, 100, 45.5, 180, 0);

    @SerializedName("team2_spawn")
    @Setter
    private Location team2Spawn = new Location(null, 0.5, 100, -45.5, 0, 0);

    // These values are only set after the Config has been read.
    @Setter private transient GameArea area;
    @Setter private transient File file;

    public Arena() {

    }
    
    @Override
    public Arena clone() {
        try {
            Arena clone = (Arena) super.clone();
            clone.spectatorSpawn = spectatorSpawn.clone();
            clone.team1Spawn = team1Spawn.clone();
            clone.team2Spawn = team2Spawn.clone();

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void updateConfig() {
        try {
            Serializer.serialize(file, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
