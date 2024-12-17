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

package de.butzlabben.missilewars.util.version;

import de.butzlabben.missilewars.Logger;
import org.bukkit.Bukkit;

/**
 * @author Butzlabben
 * @since 14.08.2018
 */
public class VersionUtil {

    private static int version;

    private VersionUtil() {
    }

    public static int getVersion() {

        if (version == 0) {
            // Detect version:
            String v = Bukkit.getVersion();

            if (v.startsWith("1.22")) {
                version = 22;
            } else if (v.startsWith("1.21")) {
                version = 21;
            } else if (v.startsWith("1.20")) {
                version = 20;
            } else if (v.startsWith("1.19")) {
                version = 19;
            } else if (v.startsWith("1.18")) {
                version = 18;
            } else if (v.startsWith("1.17")) {
                version = 17;
            } else if (v.startsWith("1.16")) {
                version = 16;
            } else if (v.startsWith("1.15")) {
                version = 15;
            } else if (v.startsWith("1.14")) {
                version = 14;
            } else if (v.startsWith("1.13")) {
                version = 13;
            } else if (v.startsWith("1.12")) {
                version = 12;
            } else if (v.startsWith("1.11")) {
                version = 11;
            } else if (v.startsWith("1.10")) {
                version = 10;
            } else if (v.startsWith("1.9")) {
                version = 9;
            } else if (v.startsWith("1.8")) {
                version = 8;
            } else {
                version = 0;
            }
        }

        if (version == 0) {
            Logger.WARN.log("Unknown version: " + Bukkit.getVersion());
            Logger.WARN.log("Choosing version 1.8");
            version = 8;
        }
        return version;
    }

}
