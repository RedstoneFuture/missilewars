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

import de.butzlabben.missilewars.MissileWars;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * @author Butzlabben
 * @since 10.06.2018
 */
public class OrcListener implements Listener {

    private static OrcListener instance;

    private final HashMap<UUID, OrcInventory> invs = new HashMap<>();

    private OrcListener() {
        Bukkit.getPluginManager().registerEvents(this, MissileWars.getInstance());
    }

    public static synchronized OrcListener getInstance() {
        if (instance == null)
            instance = new OrcListener();
        return instance;
    }

    @EventHandler
    public void on(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && invs.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
            OrcItem item = invs.get(e.getWhoClicked().getUniqueId()).items.get(e.getSlot());
            if (item != null)
                item.onClick((Player) e.getWhoClicked(), invs.get(e.getWhoClicked().getUniqueId()));
        }
    }

    public void register(UUID uuid, OrcInventory inv) {
        invs.put(uuid, inv);
    }

    @EventHandler
    public void on(InventoryCloseEvent e) {
        if (e.getInventory() != null) {
            invs.remove(e.getPlayer().getUniqueId());
        }
    }
}
