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
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Daniel Nägele
 */
public class R1_16Paster {

    public void pasteMissile(File schematic, Vector locationVec, int rotation, org.bukkit.World world,
                             Material glassBlockReplace, int replaceRadius, Material replaceMaterial, int replaceTicks, JavaPlugin plugin) {
        
        pasteSchematic(schematic, locationVec, world, rotation, plugin);
        
        // Remove "Replacer-Block" after a short time to update the pasted schematic structure via (normal) WorldEdit:
        new BukkitRunnable() {
            @Override
            public void run() {
                removeTempBlock(locationVec, world, replaceRadius, replaceMaterial);
            }
        }.runTaskLater(plugin, replaceTicks);
    }

    /**
     * This method executes the paste command via FAWE.
     * 
     * @param schematic (File) the target WorldEdit schematic file
     * @param locationVec (Vector) 
     * @param world (World) the target world for the WorldEdit action
     * @param rotation (int) the target schematic rotation
     * @param plugin (JavaPlugin) the basis plugin
     */
    public void pasteSchematic(File schematic, Vector locationVec, org.bukkit.World world, int rotation, JavaPlugin plugin) {
        World weWorld = new BukkitWorld(world);
        BlockVector3 blockVec = fromBukkitVector(locationVec);

        try (Clipboard clipboard = ClipboardFormats.findByFile(schematic).load(schematic); 
             var session = WorldEdit.getInstance().newEditSession(weWorld)) {
            
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
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
     * This method removes the temporary "Replacer-Block", so that the (asynchronously) 
     * paste structure via FAWE gets an update.
     * 
     * @param locationVec (Vector) 
     * @param world (World) the target world for the WorldEdit action
     * @param radius (int) the configured update-radius
     * @param replaceMaterial (Material) the target material for the replacement
     */
    private void removeTempBlock(Vector locationVec, org.bukkit.World world, int radius, Material replaceMaterial) {
        World weWorld = new BukkitWorld(world);
        BlockVector3 blockVec = fromBukkitVector(locationVec);
        
        var radiusVec = BlockVector3.at(radius, radius, radius);
        
        weWorld.replaceBlocks(new CuboidRegion(blockVec.subtract(radiusVec), blockVec.add(radiusVec)), 
                Set.of(BukkitAdapter.adapt(replaceMaterial.createBlockData()).toBaseBlock()), 
                BukkitAdapter.adapt(Material.AIR.createBlockData()));
    }
    
    private BlockVector3 fromBukkitVector(org.bukkit.util.Vector pos) {
        return BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
    }
}
