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

import org.bukkit.Color;
import org.bukkit.Material;

public class ColorConverter {

    private ColorConverter() {
    }

    public static Color getColorFromCode(String s) {
        if (s.startsWith("§")) {
            s = s.substring(1);
        }
        switch (s) {
            case "0":
                return Color.fromRGB(0, 0, 0);
            case "1":
                return Color.fromRGB(0, 0, 170);
            case "2":
                return Color.fromRGB(0, 170, 0);
            case "3":
                return Color.fromRGB(0, 170, 170);
            case "4":
                return Color.fromRGB(170, 0, 0);
            case "5":
                return Color.fromRGB(170, 0, 170);
            case "6":
                return Color.fromRGB(255, 170, 0);
            case "7":
                return Color.fromRGB(170, 170, 170);
            case "8":
                return Color.fromRGB(85, 85, 85);
            case "9":
                return Color.fromRGB(85, 85, 255);
            case "a":
                return Color.fromRGB(85, 255, 85);
            case "b":
                return Color.fromRGB(85, 255, 255);
            case "c":
                return Color.fromRGB(255, 85, 85);
            case "d":
                return Color.fromRGB(255, 85, 255);
            case "e":
                return Color.fromRGB(255, 255, 85);
            default:
                return Color.WHITE;
        }
    }

    public static Material getGlassPaneFromColorCode(String s) {
        if (s.startsWith("§")) {
            s = s.substring(1);
        }
        switch (s) {
            case "f":
            case "0":
                return Material.valueOf("WHITE_STAINED_GLASS_PANE");
            case "6":
                return Material.valueOf("ORANGE_STAINED_GLASS_PANE");
            case "d":
                return Material.valueOf("PINK_STAINED_GLASS_PANE");
            case "b":
                return Material.valueOf("CYAN_STAINED_GLASS_PANE");
            case "e":
                return Material.valueOf("YELLOW_STAINED_GLASS_PANE");
            case "a":
            case "2":
                return Material.valueOf("GREEN_STAINED_GLASS_PANE");
            case "8":
                return Material.valueOf("GRAY_STAINED_GLASS_PANE");
            case "7":
                return Material.valueOf("LIGHT_GRAY_STAINED_GLASS_PANE");
            case "3":
                return Material.valueOf("LIGHT_BLUE_STAINED_GLASS_PANE");
            case "5":
            case "1":
                return Material.valueOf("MAGENTA_STAINED_GLASS_PANE");
            case "9":
                return Material.valueOf("BLUE_STAINED_GLASS_PANE");
            case "c":
            case "4":
                return Material.valueOf("RED_STAINED_GLASS_PANE");
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * @param i from colored Blocks for example Wool
     *
     * @return Color Code in Form "§f"
     *
     * @throws IllegalArgumentException if i is < 0 or > 15
     * @author Butzlabben
     * @since 26.09.2017
     */
    public static String getLegacyColorCodefromBlock(int i) {
        if (i < 0 || i > 15) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                return "§f";
            case 1:
                return "§6";
            case 2:
                return "§d";
            case 3:
                return "§b";
            case 4:
                return "§e";
            case 5:
                return "§a";
            case 6:
                return "§c";
            case 7:
                return "§8";
            case 8:
                return "§7";
            case 9:
                return "§3";
            case 10:
                return "§5";
            case 11:
                return "§1";
            case 12:
                return "";
            case 13:
                return "§2";
            case 14:
                return "§4";
            case 15:
                return "§0";
        }
        return null;
    }

    /**
     * @param s Code in Form "§f"
     *
     * @return SubID from colored Blocks for example Wool
     *
     * @throws IllegalArgumentException if there is no ColorCode found
     * @author Butzlabben
     * @since 26.09.2017
     */
    public static byte getColorIDforBlockFromColorCode(String s) {
        if (s.startsWith("§")) {
            s = s.substring(1);
        }
        switch (s) {
            case "f":
                return 0;
            case "6":
                return 1;
            case "d":
                return 2;
            case "b":
                return 3;
            case "e":
                return 4;
            case "a":
                return 5;
            case "8":
                return 7;
            case "7":
                return 8;
            case "3":
                return 9;
            case "5":
                return 10;
            case "1":
            case "9":
                return 11;
            case "2":
                return 12;
            case "c":
            case "4":
                return 14;
            case "0":
                return 15;
            default:
                throw new IllegalArgumentException();

        }
    }

    public static Material getGlassFromColorCode(String s) {
        if (s.startsWith("§")) {
            s = s.substring(1);
        }
        switch (s) {
            case "f":
            case "0":
                return Material.valueOf("WHITE_STAINED_GLASS");
            case "6":
                return Material.valueOf("ORANGE_STAINED_GLASS");
            case "d":
                return Material.valueOf("PINK_STAINED_GLASS");
            case "b":
                return Material.valueOf("CYAN_STAINED_GLASS");
            case "e":
                return Material.valueOf("YELLOW_STAINED_GLASS");
            case "a":
            case "2":
                return Material.valueOf("GREEN_STAINED_GLASS");
            case "8":
                return Material.valueOf("GRAY_STAINED_GLASS");
            case "7":
                return Material.valueOf("LIGHT_GRAY_STAINED_GLASS");
            case "3":
                return Material.valueOf("LIGHT_BLUE_STAINED_GLASS");
            case "5":
            case "1":
                return Material.valueOf("MAGENTA_STAINED_GLASS");
            case "9":
                return Material.valueOf("BLUE_STAINED_GLASS");
            case "c":
            case "4":
                return Material.valueOf("RED_STAINED_GLASS");
            default:
                throw new IllegalArgumentException();
        }
    }
}
