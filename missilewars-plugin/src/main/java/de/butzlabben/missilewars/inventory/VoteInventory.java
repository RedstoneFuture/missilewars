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

package de.butzlabben.missilewars.inventory;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public class VoteInventory extends OrcInventory {

    public VoteInventory(List<Arena> arenas) {
        super(MessageConfig.getNativeMessage("vote.gui"), (int) Math.ceil(arenas.size() / 9D));

        Map<Integer, int[]> map = new HashMap<>();
        map.put(1, new int[] {4});
        map.put(2, new int[] {0, 8});
        map.put(3, new int[] {0, 4, 8});
        map.put(4, new int[] {0, 3, 5, 8});
        map.put(5, new int[] {0, 2, 4, 6, 8});
        map.put(6, new int[] {0, 1, 3, 5, 7, 8});
        map.put(7, new int[] {0, 1, 3, 4, 5, 7, 8});
        map.put(8, new int[] {0, 1, 2, 3, 5, 6, 7, 8});
        map.put(0, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8});

        final int rowCount = (int) Math.ceil(arenas.size() / 9D);
        int currentRow = 0;
        int inRowIndex = 0;
        for (Arena arena : arenas) {
            Material material;
            try {
                material = Material.valueOf(arena.getDisplayMaterial());
            } catch (IllegalArgumentException ignored) {
                Logger.WARN.log("Could not find a material with the name: " + arena.getDisplayMaterial());
                material = Material.BARRIER;
            }
            OrcItem orcItem = new OrcItem(material, arena.getDisplayName());
            orcItem.setOnClick((p, inv, item) -> {
                p.performCommand("mw vote " + arena.getName());
                p.closeInventory();
            });
            int index;
            if (currentRow >= rowCount - 1) {
                index = map.get(arenas.size() % 9)[inRowIndex];
            } else {
                index = map.get(0)[inRowIndex];
            }
            index += currentRow * 9;
            addItem(index, orcItem);
            inRowIndex++;
            if (inRowIndex == 9) {
                inRowIndex = 0;
                currentRow++;
            }
        }
    }
}

