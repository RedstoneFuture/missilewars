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

package de.butzlabben.missilewars.configuration.arena;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.game.schematics.SchematicConfiguration;
import de.butzlabben.missilewars.game.schematics.SchematicFacing;
import de.butzlabben.missilewars.game.schematics.objects.Missile;
import de.butzlabben.missilewars.game.schematics.objects.SchematicObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;


@Getter
@ToString
@RequiredArgsConstructor
public class MissileConfig extends SchematicConfiguration {

    // TODO pretty names
    private boolean onlyBlockPlaceable = false;
    private boolean onlyBetweenSpawnPlaceable = false;
    private boolean northFacing = true;
    private boolean eastFacing = true;
    private boolean southFacing = true;
    private boolean westFacing = true;

    private List<Missile> schematics = new ArrayList<>() {{
        add(new Missile("Tomahawk.schem", "&a%schematic_name_compact%", 3, EntityType.CREEPER, 0, 2));
        add(new Missile("Cruiser.schem", "&e%schematic_name_compact%", 2, EntityType.BLAZE, 0, 2));
        add(new Missile("Sword.schem", "&7%schematic_name_compact%", 2, EntityType.SKELETON, 0, 2));
        add(new Missile("Juggernaut.schem", "&4%schematic_name_compact%", 2, EntityType.MUSHROOM_COW, 0, 2));
        add(new Missile("Piranha.schem", "&3%schematic_name_compact%", 3, EntityType.HORSE, 0, 2));
        add(new Missile("Tunnelbore.schem", "&0%schematic_name_compact%", 1, EntityType.ENDERMAN, 0, 2));
    }};


    @Override
    public String getObjectNameSingular() {
        return "Missile";
    }

    @Override
    public String getObjectNamePlural() {
        return "Missiles";
    }
    
    @Override
    public List<String> getSchematicNames() {
        List<String> schematicNames = new ArrayList<>();

        for (Missile missile : getSchematics()) {
            schematicNames.add(missile.getSchematicName(true));
        }

        return schematicNames;
    }

    @Override
    public SchematicObject getSchematicFromFileName(String fileName) {

        for (Missile missile : getSchematics()) {
            if (missile.getSchematicName(true).equals(fileName)) return missile;
        }
        Logger.WARN.log(getObjectNameSingular() + " not found: '" + fileName.replaceAll("§", "").replaceAll("&", "") + "'");
        return null;
    }

    @Override
    public SchematicObject getSchematicFromDisplayName(String displayName) {

        for (Missile missile : getSchematics()) {
            if (missile.getDisplayName().equals(displayName)) return missile;
        }
        Logger.WARN.log(getObjectNameSingular() + " not found: '" + displayName.replaceAll("§", "").replaceAll("&", "") + "'");
        return null;
    }

    @Override
    public void check() {
        if (getSchematics().isEmpty()) throw new IllegalStateException("The game cannot be started, when 0 " + getObjectNamePlural() + " are configured");

        Set<SchematicObject> toRemove = new HashSet<>();
        
        for (Missile missile : getSchematics()) {
            File schematic = missile.getSchematic();

            if (schematic.exists()) continue;

            Logger.WARN.log(missile.getDisplayName() + " §7has no " + getObjectNameSingular() + ". Removing this schematic");
            toRemove.add(missile);
        }
        getSchematics().removeAll(toRemove);
    }
    
    public List<SchematicFacing> getEnabledFacings() {
        List<SchematicFacing> enabledDirections = new ArrayList<>();

        if (northFacing) enabledDirections.add(SchematicFacing.NORTH);
        if (eastFacing) enabledDirections.add(SchematicFacing.EAST);
        if (southFacing) enabledDirections.add(SchematicFacing.SOUTH);
        if (westFacing) enabledDirections.add(SchematicFacing.WEST);

        if (enabledDirections.isEmpty()) {
            Logger.WARN.log("All facings were disabled for an arena. Please correct this issue");
            enabledDirections.addAll(Arrays.asList(SchematicFacing.values()));
        }

        return enabledDirections;
    }
    
}
