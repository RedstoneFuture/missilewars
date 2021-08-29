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

package de.butzlabben.missilewars.wrapper.abstracts.arena;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.wrapper.missile.Missile;
import de.butzlabben.missilewars.wrapper.missile.MissileFacing;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.entity.EntityType;


@Getter
@RequiredArgsConstructor
@ToString
public class MissileConfiguration {

    // TODO pretty names

    private final boolean onlyBlockPlaceable = false;
    private final boolean onlyBetweenSpawnPlaceable = false;
    private final boolean northFacing = true;
    private final boolean eastFacing = true;
    private final boolean southFacing = true;
    private final boolean westFacing = true;
    private final List<Missile> missiles = new ArrayList<Missile>() {{
        add(new Missile("Tomahawk.schematic", "&aTomahawk", EntityType.CREEPER, 2, 2, 3));
        add(new Missile("Cruiser.schematic", "&eCruiser", EntityType.BLAZE, 2, 2, 2));
        add(new Missile("Sword.schematic", "&7Sword", EntityType.SKELETON, 2, 2, 2));
        add(new Missile("Juggernaut.schematic", "&4Juggernaut", EntityType.MUSHROOM_COW, 2, 2, 1));
        add(new Missile("Piranha.schematic", "&3Piranha", EntityType.HORSE, 2, 2, 3));
        add(new Missile("Tunnelbore.schematic", "&0Tunnelbore", EntityType.ENDERMAN, 2, 2, 1));
    }};

    public List<MissileFacing> getEnabledFacings() {
        List<MissileFacing> enabledDirections = new ArrayList<>();
        if (northFacing) enabledDirections.add(MissileFacing.NORTH);
        if (eastFacing) enabledDirections.add(MissileFacing.EAST);
        if (southFacing) enabledDirections.add(MissileFacing.SOUTH);
        if (westFacing) enabledDirections.add(MissileFacing.WEST);
        if (enabledDirections.isEmpty()) {
            Logger.WARN.log("All facings were disabled for an arena. Please correct this issue");
            enabledDirections.addAll(Arrays.asList(MissileFacing.values()));
        }
        return enabledDirections;
    }

    public Missile getMissileFromName(String name) {
        for (Missile m : missiles) {
            if (m.getName().equalsIgnoreCase(name) || m.getName().replaceAll("§.", "").equalsIgnoreCase(name))
                return m;
        }
        return null;
    }

    public Missile getMissileFromType(EntityType type) {
        for (Missile m : missiles) {
            if (m.getType() == type)
                return m;
        }
        return null;
    }

    public Missile getMissileFromID(int i) {
        return missiles.get(i);
    }

    public void check() {
        Set<Missile> toRemove = new HashSet<>();
        for (Missile missile : missiles) {
            File schematic = missile.getSchematic();
            if (!schematic.exists()) {
                Logger.WARN.log(missile.getName() + " §7has no schematic. Removing this missile");
                toRemove.add(missile);
            }
        }
        missiles.removeAll(toRemove);
    }
}
