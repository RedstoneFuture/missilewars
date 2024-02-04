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

import de.butzlabben.missilewars.game.Team;
import org.bukkit.util.Vector;

import java.io.File;

/**
 * @author Butzlabben
 * @since 23.09.2018
 */
public interface Paster {

    void pasteSchematic(File schematic, Vector position, org.bukkit.World world);

    void pasteMissile(File schematic, Vector position, int rotation, org.bukkit.World world, Team team);
}
