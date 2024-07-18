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

package de.butzlabben.missilewars.game.schematics.paste;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.missile.paste.v1_20.fawe.FAWE_Paster;
import de.butzlabben.missilewars.util.version.ColorConverter;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.File;

/**
 * @author Butzlabben
 * @since 23.09.2018
 */
public class FawePasteProvider implements Paster {

    FAWE_Paster paster = new FAWE_Paster();

    @Override
    public void pasteMissile(File schematic, Vector position, int rotation, World world, Team team, boolean blockUpdate) {
        paster.pasteMissile(schematic, position, rotation, world, ColorConverter.getGlassFromColorCode(team.getColorCode()),
                Config.getUpdateRadius(), Config.getTempBlockMaterial(), Config.getUpdateDelay(), MissileWars.getInstance(), blockUpdate);
    }

    @Override
    public void pasteSchematic(File schematic, Vector position, int rotation, World world) {
        paster.pasteSchematic(schematic, position, rotation, world, MissileWars.getInstance());
    }
}
