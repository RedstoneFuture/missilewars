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

import com.google.gson.annotations.SerializedName;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.game.schematics.SchematicConfiguration;
import de.butzlabben.missilewars.game.schematics.objects.SchematicObject;
import de.butzlabben.missilewars.game.schematics.objects.Shield;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@ToString
@RequiredArgsConstructor
public class ShieldConfiguration extends SchematicConfiguration {

    @SerializedName("fly_time") private int flyTime = 20;

    private List<Shield> schematics = new ArrayList<>() {{
        add(new Shield("shield.schematic", "&cShield", 1));
    }};


    @Override
    public String getObjectNameSingular() {
        return "Shield";
    }

    @Override
    public String getObjectNamePlural() {
        return "Shields";
    }

    @Override
    public List<String> getSchematicNames() {
        List<String> schematicNames = new ArrayList<>();

        for (Shield shield : getSchematics()) {
            schematicNames.add(shield.getSchematicName(true));
        }

        return schematicNames;
    }

    @Override
    public SchematicObject getSchematicFromFileName(String fileName) {

        for (Shield shield : getSchematics()) {
            if (shield.getSchematicName(true).equals(fileName)) return shield;
        }
        Logger.WARN.log(getObjectNameSingular() + " not found: '" + fileName.replaceAll("§", "").replaceAll("&", "") + "'");
        return null;
    }

    @Override
    public SchematicObject getSchematicFromDisplayName(String displayName) {

        for (Shield shield : getSchematics()) {
            if (shield.getDisplayName().equals(displayName)) return shield;
        }
        Logger.WARN.log(getObjectNameSingular() + " not found: '" + displayName.replaceAll("§", "").replaceAll("&", "") + "'");
        return null;
    }

    @Override
    public void check() {
        if (getSchematics().isEmpty()) throw new IllegalStateException("The game cannot be started, when 0 " + getObjectNamePlural() + " are configured");

        Set<SchematicObject> toRemove = new HashSet<>();

        for (Shield shield : getSchematics()) {
            File schematic = shield.getSchematic();

            if (schematic.exists()) continue;

            Logger.WARN.log(shield.getDisplayName() + " §7has no " + getObjectNameSingular() + ". Removing this schematic");
            toRemove.add(shield);
        }
        getSchematics().removeAll(toRemove);
    }
    
}
