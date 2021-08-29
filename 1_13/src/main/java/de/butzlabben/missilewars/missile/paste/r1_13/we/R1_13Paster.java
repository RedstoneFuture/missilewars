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

package de.butzlabben.missilewars.missile.paste.r1_13.we;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author Butzlabben
 * @since 23.09.2018
 */
public class R1_13Paster {

    public void pasteMissile(File schematic, org.bukkit.util.Vector pos, int rotation, org.bukkit.World world,
                             Material glassBlockReplace, int radius, Material replaceType, JavaPlugin plugin, int replaceTicks) {
        try {
            World weWorld = new BukkitWorld(world);
            ClipboardFormat format = ClipboardFormats.findByFile(schematic);

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematic));
                 EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1)) {
                ClipboardHolder clipboardHolder = new ClipboardHolder(reader.read());
                Clipboard clipboard = clipboardHolder.getClipboard();
                AffineTransform transform = new AffineTransform();
                transform = transform.rotateY(rotation);

                BlockTransformExtent extent = new BlockTransformExtent(clipboard, transform);
                ForwardExtentCopy copy = new ForwardExtentCopy(new BlockFilterExtent(extent, glassBlockReplace), clipboard.getRegion(), clipboard.getOrigin(), editSession, BlockVector3.at(pos.getX(), pos.getY(), pos.getZ()));
                copy.setTransform(transform);
                copy.setSourceMask(new ExistingBlockMask(clipboard));

                Operations.complete(copy);
            }

            // Replace given blocks
            Set<Block> replace = new HashSet<>();
            BlockVector3 min = BlockVector3.at(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius);
            BlockVector3 max = BlockVector3.at(pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
            for (BlockVector3 v : new CuboidRegion(min, max)) {
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

    public void pasteSchematic(File schematic, Vector pos, org.bukkit.World world) {
        try {
            World weWorld = new BukkitWorld(world);

            ClipboardFormat format = ClipboardFormats.findByFile(schematic);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematic));
                 EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1)) {
                ClipboardHolder clipboard = new ClipboardHolder(reader.read());

                Operation operation = clipboard
                        .createPaste(editSession)
                        .to(BlockVector3.at(pos.getX(), pos.getY(), pos.getZ()))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
