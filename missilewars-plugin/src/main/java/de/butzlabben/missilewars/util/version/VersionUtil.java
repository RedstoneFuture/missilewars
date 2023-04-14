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
import de.butzlabben.missilewars.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            // Detect version
            String v = Bukkit.getVersion();
            if (v.contains("1.20")) version = 20;
            else if (v.contains("1.19")) version = 19;
            else if (v.contains("1.18")) version = 18;
            else if (v.contains("1.17")) version = 17;
            else if (v.contains("1.16")) version = 16;
            else if (v.contains("1.15")) version = 15;
            else if (v.contains("1.14")) version = 14;
            else if (v.contains("1.13")) version = 13;
            else if (v.contains("1.12")) version = 12;
            else if (v.contains("1.11")) version = 11;
            else if (v.contains("1.10")) version = 10;
            else if (v.contains("1.9")) version = 9;
            else if (v.contains("1.8")) version = 8;
            else if (v.contains("1.7")) version = 7;
            else if (v.contains("1.6")) version = 6;
            else if (v.contains("1.5")) version = 5;
            else if (v.contains("1.4")) version = 4;
            else if (v.contains("1.3")) version = 3;
        }

        if (version == 0) {
            Logger.WARN.log("Unknown version: " + Bukkit.getVersion());
            Logger.WARN.log("Choosing version 1.13");
            version = 13;
        }
        return version;
    }
    
    public static Material getFireball() {
        return Material.valueOf("FIRE_CHARGE");
    }

    public static Material getSnowball() {
        return Material.valueOf("SNOWBALL");
    }

    public static Material getMonsterEgg(EntityType type) {
        if (type == EntityType.MUSHROOM_COW) {
            //noinspection SpellCheckingInspection
            return Material.valueOf("MOOSHROOM_SPAWN_EGG");

        }
        return Material.valueOf(type.name() + "_SPAWN_EGG");
    }

    public static boolean isMonsterEgg(Material material) {
        if (material == null) return false;
        
        String name = material.name();
        
        if (name.equals("EGG")) return false;
        if (name.contains("SPAWN_EGG")) return true;
        return name.equals("MONSTER_EGG");
    }

    public static Material getPortal() {
        return Material.valueOf("NETHER_PORTAL");
    }

    public static Material getSunFlower() {
        return Material.valueOf("SUNFLOWER");
    }
    
    public static ItemStack getGlassPlane(Team team) {
        String colorCode = team.getColorCode();
        ItemStack is = new ItemStack(ColorConverter.getGlassPaneFromColorCode(colorCode));
        
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(team.getFullname());
        is.setItemMeta(im);
        return is;
    }

    public static boolean isStainedGlassPane(Material material) {
        if (material == null) return false;
        return material.name().contains("STAINED_GLASS_PANE");
    }
    
    public static ItemStack getGlassPlane(String colorCode) {
        return new ItemStack(ColorConverter.getGlassPaneFromColorCode(colorCode));
    }

    public static Material getPlayerSkullMaterial() {
        return Material.valueOf("PLAYER_HEAD");
    }

    public static boolean isWallSignMaterial(Material material) {
        return material.name().contains("_SIGN");
    }

    public static void setUnbreakable(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        im.setUnbreakable(true);
        is.setItemMeta(im);
    }
    
}
