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

package de.butzlabben.missilewars.wrapper.abstracts;

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.wrapper.abstracts.arena.*;
import de.butzlabben.missilewars.wrapper.geometry.FlatArea;
import de.butzlabben.missilewars.wrapper.geometry.Plane;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Arena {

    private String name = "arena0";
    @SerializedName("display_name") private String displayName = "&eDefault map";
    @SerializedName("display_material") private String displayMaterial = "STONE";
    @SerializedName("template_world") private String templateWorld = "default_map";
    @SerializedName("auto_respawn") private boolean autoRespawn = true;
    @SerializedName("game_spawn") private GameSpawnConfiguration spawn = new GameSpawnConfiguration();
    @SerializedName("game_respawn") private GameRespawnConfiguration respawn = new GameRespawnConfiguration();
    @SerializedName("do_tile_drops") private boolean doTileDrops = false;
    @SerializedName("keep_inventory") private boolean keepInventory = false;
    @SerializedName("max_height") private int maxHeight = 170;
    @SerializedName("death_height") private int deathHeight = 65;
    @SerializedName("max_spectators") private int maxSpectators = -1;
    @SerializedName("game_duration") private int gameDuration = 30;
    @SerializedName("fireball") private FireballConfiguration fireballConfiguration = new FireballConfiguration();
    @SerializedName("shield") private ShieldConfiguration shieldConfiguration = new ShieldConfiguration();
    @SerializedName("arrow_occurrence") private int arrowOccurrence = 2;
    @SerializedName("save_statistics") private boolean saveStatistics = true;
    @SerializedName("fall_protection") private FallProtectionConfiguration fallProtection = new FallProtectionConfiguration();
    @SerializedName("money") private MoneyConfiguration money = new MoneyConfiguration();
    @SerializedName("equipment_interval") private EquipmentIntervalConfiguration interval = new EquipmentIntervalConfiguration();
    @SerializedName("missile_configuration") private MissileConfiguration missileConfiguration = new MissileConfiguration();
    @SerializedName("spectator_spawn") private Location spectatorSpawn = new Location(null, 0, 100, 0, 90, 0);
    @SerializedName("area") private FlatArea gameArea = new FlatArea(-30, -72, 30, 72);
    @SerializedName("team1_spawn") private Location team1Spawn = new Location(null, 0.5, 100, 45.5, 180, 0);
    @SerializedName("team2_spawn") private Location team2Spawn = new Location(null, 0.5, 100, -45.5, 0, 0);

    public Plane getPlane1() {
        Vector spawn1 = team1Spawn.toVector();
        Vector normal = team2Spawn.toVector().subtract(spawn1);
        return new Plane(spawn1, normal);
    }

    public Plane getPlane2() {
        Vector spawn2 = team2Spawn.toVector();
        Vector normal = team1Spawn.toVector().subtract(spawn2);
        return new Plane(spawn2, normal);
    }

    public boolean isInBetween(Vector point, Plane plane1, Plane plane2) {
        double distanceBetween = plane1.distanceSquared(plane2.getSupport());
        double distance1 = plane1.distanceSquared(point);
        double distance2 = plane2.distanceSquared(point);
        return distanceBetween > distance1 + distance2;
    }
}
