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

package de.butzlabben.missilewars.game.missile;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.arena.MissileConfiguration;
import de.butzlabben.missilewars.util.missile.Interval;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

/**
 * @author Butzlabben
 * @since 06.01.2018
 */
public enum MissileFacing {

    NORTH(new Interval(180, 270), new Interval(135, 315)),
    EAST(new Interval(270, 360), new Interval(225, 360), new Interval(0, 45)),
    SOUTH(new Interval(0, 90), new Interval(0, 135), new Interval(315, 360)),
    WEST(new Interval(90, 180), new Interval(45, 225));

    public final Interval primary;
    public final Interval[] secondary;

    MissileFacing(Interval primary, Interval... secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public static MissileFacing getFacing(double degree, MissileConfiguration configuration) {
        List<MissileFacing> values = Arrays.stream(MissileFacing.values()).filter(f -> configuration.getEnabledFacings().contains(f)).collect(Collectors.toList());
        MissileFacing facing = null;
        for (MissileFacing fac : values) {
            if (fac.primary.isIn(degree)) {
                facing = fac;
                break;
            }
        }
        if (facing == null) {
            for (MissileFacing fac : values) {
                for (int i = 0; i < fac.secondary.length; i++) {
                    if (fac.secondary[i].isIn(degree)) {
                        facing = fac;
                        break;
                    }
                }
            }
        }
        if (facing == null) {
            Logger.WARN.log("Could not find direction for degree: " + degree);
            facing = NORTH;
        }
        return facing;
    }

    public static MissileFacing getFacingPlayer(Player playerSelf, MissileConfiguration configuration) {
        float y = playerSelf.getLocation().getYaw();
        if (y < 0) {
            y += 360;
        }
        y %= 360;
        y += 45;
        if (y > 360)
            y -= 360;
        return getFacing(y, configuration);
    }

    public static String getFacing(int i) {
        String dir;
        if (i == 0) {
            dir = "west";
        } else if (i == 1) {
            dir = "west northwest";
        } else if (i == 2) {
            dir = "northwest";
        } else if (i == 3) {
            dir = "north northwest";
        } else if (i == 4) {
            dir = "north";
        } else if (i == 5) {
            dir = "north northeast";
        } else if (i == 6) {
            dir = "northeast";
        } else if (i == 7) {
            dir = "east northeast";
        } else if (i == 8) {
            dir = "east";
        } else if (i == 9) {
            dir = "east southeast";
        } else if (i == 10) {
            dir = "southeast";
        } else if (i == 11) {
            dir = "south southeast";
        } else if (i == 12) {
            dir = "south";
        } else if (i == 13) {
            dir = "south southwest";
        } else if (i == 14) {
            dir = "southwest";
        } else if (i == 15) {
            dir = "west southwest";
        } else {
            dir = "west";
        }
        return dir;
    }
}
