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

package de.butzlabben.missilewars.missile.paste.r1_13.fawe;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class R1_13Paster {

    public void pasteMissile(File schematic, org.bukkit.util.Vector pos, int rotation, org.bukkit.World world,
                             Material glassBlockReplace, int radius, Material replaceType, JavaPlugin plugin, int replaceTicks) {
        try {
            World weWorld = new BukkitWorld(world);

            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
            Clipboard clipboard = ClipboardFormat.findByFile(schematic).load(schematic).getClipboard();

            AffineTransform transform = new AffineTransform();
            transform = transform.rotateY(rotation);

            Vector origin = new Vector(clipboard.getOrigin().getX(), clipboard.getOrigin().getY(), clipboard.getOrigin().getZ());
//            BlockTransformExtent extent = new BlockTransformExtent(clipboard, transform, ((BukkitWorld) weWorld).getWorldData().getBlockRegistry());
            ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), origin, editSession, new Vector(pos.getX(), pos.getY(), pos.getZ()));
            copy.setTransform(transform);
            copy.setSourceMask(new ExistingBlockMask(clipboard));

            Operations.complete(copy);


            // Replace given blocks
            Set<Block> replace = new HashSet<>();
            Vector min = new Vector(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius);
            Vector max = new Vector(pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
            for (Vector v : new CuboidRegion(min, max)) {
                Block b = world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());
                if (b.getType() == replaceType) {
                    replace.add(b);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    replace.forEach(b -> b.setType(Material.AIR));
                }
            }.runTaskLater(plugin, replaceTicks);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pasteSchematic(File schematic, org.bukkit.util.Vector pos, org.bukkit.World world) {
        try {
            World weWorld = new BukkitWorld(world);

            EditSession editSession = ClipboardFormat.findByFile(schematic).load(schematic)
                    .paste(weWorld, new Vector(pos.getX(), pos.getY(), pos.getZ()), false, false, null);
            editSession.flushQueue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
