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

package de.butzlabben.missilewars.missile.paste.r1_16.fawe;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author Daniel Nägele
 */
public class R1_16Paster {

    public void pasteMissile(File schematic, Vector pos, int rotation, org.bukkit.World world,
                             Material glassBlockReplace, int radius, Material replaceType, JavaPlugin plugin, int replaceTicks) {
        World weWorld = new BukkitWorld(world);

        try (Clipboard clipboard = ClipboardFormats.findByFile(schematic).load(schematic)) {
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            clipboardHolder.setTransform(new AffineTransform().rotateY(rotation));
            Operation pasteBuilder = clipboardHolder
                    .createPaste(weWorld)
                    .to(fromBukkitVector(pos))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.completeBlindly(pasteBuilder);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector min = pos.subtract(new Vector(radius, radius, radius));
                    Vector max = pos.add(new Vector(radius, radius, radius));
                    weWorld.replaceBlocks(new CuboidRegion(fromBukkitVector(min), fromBukkitVector(max)),
                    Set.of(BukkitAdapter.adapt(replaceType.createBlockData()).toBaseBlock()),
                    BukkitAdapter.adapt(Material.AIR.createBlockData()));
                }
            }.runTaskLater(plugin, replaceTicks);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pasteSchematic(File schematic, Vector pos, org.bukkit.World world) {
        World weWorld = new BukkitWorld(world);

        try (var clipboard = ClipboardFormats.findByFile(schematic).load(schematic);
             var session = WorldEdit.getInstance().newEditSession(weWorld)) {
            Operation paste = new ClipboardHolder(clipboard).createPaste(session).to(fromBukkitVector(pos))
                    .ignoreAirBlocks(true)
                    .build();
            Operations.completeBlindly(paste);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BlockVector3 fromBukkitVector(org.bukkit.util.Vector pos) {
        return BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
    }
}
