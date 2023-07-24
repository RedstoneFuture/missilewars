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

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.missile.paste.PasteProvider;
import java.io.File;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * @author Butzlabben
 * @since 06.01.2018
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class Missile {

    private final String schematic;
    @SerializedName("name") private final String displayName;
    private final EntityType egg;
    private final int down;
    private final int dist;
    @Getter private final int occurrence;

    public void paste(Player p, MissileFacing mf, Game game) {
        if (mf == null)
            return;
        try {
            Location loc = p.getLocation();
            Vector pastePos = new Vector(loc.getX(), loc.getY(), loc.getZ());
            pastePos = pastePos.add(new Vector(0, -down, 0));

            int rotation = 0;
            if (mf == MissileFacing.NORTH) {
                pastePos = pastePos.add(new Vector(0, 0, -dist));
            } else if (mf == MissileFacing.SOUTH) {
                pastePos = pastePos.add(new Vector(0, 0, dist));
                rotation = 180;
            } else if (mf == MissileFacing.EAST) {
                pastePos = pastePos.add(new Vector(dist, 0, 0));
                rotation = 270;
            } else if (mf == MissileFacing.WEST) {
                pastePos = pastePos.add(new Vector(-dist, 0, 0));
                rotation = 90;
            }


            PasteProvider.getPaster().pasteMissile(getSchematic(), pastePos, rotation, loc.getWorld(),
                    game.getPlayer(p).getTeam());
        } catch (Exception e) {
            Logger.ERROR.log("Could not load " + displayName);
            e.printStackTrace();
        }
    }

    public File getSchematic() {
        File missilesFolder = new File(Config.getMissilesFolder());
        return new File(missilesFolder, getSchematicName(false));
    }

    public String getSchematicName(boolean withoutExtension) {
        if (withoutExtension) {
            return schematic.replace(".schematic", "")
                    .replace(".schem", "");
        }
        return schematic;
    }

    public String getDisplayName() {
        String name = displayName;
        name = name.replace("%schematic_name%", getSchematicName(false))
                .replace("%schematic_name_compact%", getSchematicName(true));
        return name;
    }

    /**
     * This method provides the missile spawn item based on the
     * mob spawn item specification in the arena configuration.
     *
     * @return ItemStack = the spawn egg with the missile name
     */
    public ItemStack getItem() {
        ItemStack spawnEgg = new ItemStack(getSpawnEgg(egg));
        ItemMeta spawnEggMeta = spawnEgg.getItemMeta();
        spawnEggMeta.setDisplayName(getDisplayName());
        spawnEgg.setItemMeta(spawnEggMeta);
        return spawnEgg;
    }

    public static Material getSpawnEgg(EntityType type) {
        if (type == EntityType.MUSHROOM_COW) {
            //noinspection SpellCheckingInspection
            return Material.valueOf("MOOSHROOM_SPAWN_EGG");

        }
        return Material.valueOf(type.name() + "_SPAWN_EGG");
    }

    public static boolean isSpawnEgg(Material material) {
        if (material == null) return false;

        String name = material.name();
        return name.contains("SPAWN_EGG") || name.equals("MONSTER_EGG");
    }
}
