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

package de.butzlabben.missilewars.missile.paste.v1_20.fawe;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;

/**
 * @author Daniel Nägele
 */
public class FAWE_Paster {

    public void pasteMissile(File schematic, Vector locationVec, int rotation, org.bukkit.World world, boolean blockUpdate, 
                             Material replaceMaterial, int replaceTicks, int replaceRadius, JavaPlugin plugin) {
        
        pasteSchematic(schematic, locationVec, rotation, world, plugin);
        
        if (!blockUpdate) return;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                removeTempBlock(locationVec, world, replaceMaterial, replaceRadius);
            }
        }.runTaskLater(plugin, replaceTicks);
    }

    /**
     * This method executes the paste command via FAWE.
     * 
     * @param schematic (File) the target WorldEdit schematic file (all Schematic formats usable, '.schem' recommended)
     * @param locationVec (Vector) the abstract block location
     * @param world (World) the target world for the WorldEdit action
     * @param rotation (int) the target schematic rotation
     * @param plugin (JavaPlugin) the basis plugin
     */
    public void pasteSchematic(File schematic, Vector locationVec, int rotation, org.bukkit.World world, JavaPlugin plugin) {
        World weWorld = new BukkitWorld(world);
        BlockVector3 blockVec = getBlockVector(locationVec);
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schematic);
        
        try (ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(schematic));
             var session = WorldEdit.getInstance().newEditSession(weWorld);
             ClipboardHolder clipboardHolder = new ClipboardHolder(clipboardReader.read())) {
                clipboardHolder.setTransform(new AffineTransform().rotateY(rotation));
                Operation pasteBuilder = clipboardHolder
                        .createPaste(session)
                        .to(blockVec)
                        .ignoreAirBlocks(true)
                        .build();
                Operations.completeBlindly(pasteBuilder);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not paste schematic '" + schematic.getName() 
                    + "' with FAWE (" + WorldEdit.getVersion() + ")", e);
        }
    }
    
    /**
     * This method removes the temporary "Starter-Block", so that the 
     * (asynchronously on FAWE) pasted schematic structure gets a 
     * block-update. This remove process happens synchronously for this.
     * 
     * @param locationVec (Vector) the abstract block location
     * @param world (World) the target world for the WorldEdit action
     * @param replaceMaterial (Material) the target material for the replacement
     * @param replaceRadius (int) the configured "Replace radius" 
     *                      The value is used as the block-limit for the “Starter-Block” check and represents 
     *                      a half of the cuboid-edge length with the Schematic-Origin as starting point.
     */
    public void removeTempBlock(Vector locationVec, org.bukkit.World world, Material replaceMaterial, int replaceRadius) {
        int startX = locationVec.getBlockX() - replaceRadius;
        int endX = locationVec.getBlockX() + replaceRadius;
        int startY = locationVec.getBlockY() - replaceRadius;
        int endY = locationVec.getBlockY() + replaceRadius;
        int startZ = locationVec.getBlockZ() - replaceRadius;
        int endZ = locationVec.getBlockZ() + replaceRadius;
    
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == replaceMaterial) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    public static BlockVector3 getBlockVector(org.bukkit.util.Vector locationVec) {
        return BlockVector3.at(locationVec.getX(), locationVec.getY(), locationVec.getZ());
    }
}
