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

package de.butzlabben.missilewars.inventory.pages;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.inventory.OrcInventory;
import de.butzlabben.missilewars.inventory.OrcItem;
import de.butzlabben.missilewars.util.version.VersionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * @author Butzlabben
 * @since 20.05.2018
 */
public class InventoryPage extends OrcInventory {

    InventoryPage next, before = null;
    private int i = 0;

    public InventoryPage(String title, int page, int pages) {
        super(title, 6);

        OrcItem oi = new OrcItem(VersionUtil.getSunFlower(), "§aPage §e" + page + " §aof§e " + pages);
        addItem(5, 4, oi);

        oi = new OrcItem(Material.PAPER, "§ePrevious page");
        oi.setOnClick((p, inv, item) -> {
            p.closeInventory();
            p.openInventory(this.before.getInventory(p));
        });
        addItem(5, 0, oi);

        oi = new OrcItem(Material.PAPER, "§eNext page");
        oi.setOnClick((p, inv, item) -> {
            p.closeInventory();
            p.openInventory(this.next.getInventory(p));
        });
        addItem(5, 8, oi);
    }

    @Override
    public Inventory getInventory(Player p) {
        return super.getInventory(p);
    }

    public void addItem(OrcItem item) {
        if (i > 36) {
            Logger.ERROR.log("More items than allowed in page view");
            return;
        }
        addItem(i, item);
        i++;
    }
}
