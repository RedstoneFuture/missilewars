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

package de.butzlabben.missilewars.game.equipment;

import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.schematics.objects.SchematicObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */

@Getter
public class SchematicGameEquipment {

    private final Game game;

    private List<SchematicObject> schematicEquipmentList = new ArrayList<>();


    public SchematicGameEquipment(Game game, SchematicObject.schematicType schematicType) {
        this.game = game;

        createSchematicEquipmentList(schematicType);
    }

    /**
     * This method goes through all configured schematic and adds them
     * to the list. The higher the defined spawn-occurrence of a schematic
     * being set, the more often it will be added to the list.
     */
    private void createSchematicEquipmentList(SchematicObject.schematicType schematicType) {

        if (schematicType == SchematicObject.schematicType.MISSILE) {
            for (SchematicObject object : game.getArena().getMissileConfiguration().getSchematics()) {
                for (int i = object.getOccurrence(); i > 0; i--) {
                    schematicEquipmentList.add(object);
                }
            }
        } else if (schematicType == SchematicObject.schematicType.SHIELD) {
            for (SchematicObject object : game.getArena().getShieldConfiguration().getSchematics()) {
                for (int i = object.getOccurrence(); i > 0; i--) {
                    schematicEquipmentList.add(object);
                }
            }
        }
        
    }

}
