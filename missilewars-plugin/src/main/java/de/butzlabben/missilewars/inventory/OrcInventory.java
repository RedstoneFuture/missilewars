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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@Getter
public abstract class OrcInventory {

    protected String title;
    protected Map<Integer, OrcItem> items = new HashMap<>();
    private int rows;
    private InventoryType type;
    private boolean fill = false;

    public OrcInventory(String title) {
        Objects.requireNonNull(title, "title cannot be null");
        this.title = title;
    }

    public OrcInventory(String title, int rows) {
        this(title);
        if (rows <= 0 || rows > 6)
            throw new IllegalArgumentException("rows cannot be smaller than 1 or bigger than 6");
        this.rows = rows;
    }

    public OrcInventory(String title, int rows, boolean fill) {
        this(title, rows);
        this.fill = fill;
        if (this.fill) {
            for (int i = 0; i < rows * 9; i++) {
                items.put(i, OrcItem.fill);
            }
        }
    }

    public OrcInventory(String title, InventoryType type) {
        this(title);
        if (type == null || type == InventoryType.CHEST) {
            this.type = null;
            rows = 3;
        } else {
            this.type = type;
        }
    }

    public void addItem(int slot, OrcItem item) {
        if (item == null) {
            removeItem(slot);
        } else {
            items.put(slot, item);
        }
    }

    public void addItem(int row, int col, OrcItem item) {
        addItem(row * 9 + col, item);
    }

    public void removeItem(int slot) {
        items.remove(slot);
    }

    public void removeItem(int row, int col) {
        removeItem(row * 9 + col);
    }

    public Inventory getInventory(Player p) {
        return getInventory(p, title);
    }

    public void redraw(Player p) {
        p.closeInventory();
        p.openInventory(getInventory(p));
    }

    public Inventory getInventory(Player p, String title) {
        Inventory inv;
        int size;
        if (type == null) {
            inv = Bukkit.createInventory(null, rows * 9, title);
            size = rows * 9;
        } else {
            inv = Bukkit.createInventory(null, type, title);
            size = type.getDefaultSize();
        }

        for (Entry<Integer, OrcItem> entry : items.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < size) {
                inv.setItem(entry.getKey(), entry.getValue().getItemStack(p));
            } else {
                Logger.ERROR.log("There is a problem with a configured Item!");
            }
        }

        OrcListener.getInstance().register(p.getUniqueId(), this);

        return inv;
    }

    public Inventory getInventory() {
        Inventory inv;
        int size;
        if (type == null) {
            inv = Bukkit.createInventory(null, rows * 9, title);
            size = rows * 9;
        } else {
            inv = Bukkit.createInventory(null, type, title);
            size = type.getDefaultSize();
        }

        for (Entry<Integer, OrcItem> entry : items.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < size) {
                inv.setItem(entry.getKey(), entry.getValue().getItemStack());
            } else {
                Logger.ERROR.log("There is a problem with a configured Item!");
            }
        }
        return inv;
    }

    public void prettyFill() {
        for (int i = 0; i < 9; i++) {
            prettyFill(i);
        }
        for (int i = rows * 9 - 9; i < rows * 9; i++) {
            prettyFill(i);
        }
    }

    private void prettyFill(int slot) {
        if (items.containsKey(slot))
            return;
        items.put(slot, OrcItem.fill);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
