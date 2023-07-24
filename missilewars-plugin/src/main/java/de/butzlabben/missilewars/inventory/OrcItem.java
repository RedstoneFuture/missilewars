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

import de.butzlabben.missilewars.util.version.ColorConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OrcItem {

    public static OrcItem enabled, disabled, coming_soon, back, fill, error = new OrcItem(Material.BARRIER, null,
            "§cERROR: Item is wrong configured!", "§cPath in properties: see Displayname");

    static {
        fill = new OrcItem(new ItemStack(ColorConverter.getGlassPaneFromColorCode("§8")));
    }

    private ItemStack is;
    private OrcClickListener listener;
    private DependListener depend;
    private Runnable callback;

    public OrcItem(Material mat, String display, String... lore) {
        setItemStack(mat, display, lore);
    }

    public OrcItem(ItemStack is) {
        setItemStack(is);
    }

    public OrcItem(Material mat, String display, List<String> lore) {
        setItemStack(mat, (byte) 0, display, lore);
    }

    public OrcItem(Material mat) {
        this(new ItemStack(mat));
    }

    public OrcItem(Material material, byte data, String display, ArrayList<String> lore) {
        setItemStack(material, data, display, lore);
    }

    public void setCallback(Runnable r) {
        callback = r;
    }

    @SuppressWarnings("deprecation")
    public OrcItem setItemStack(Material mat, byte data, String display, List<String> lore) {
        is = new ItemStack(mat, 1, data);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(display);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(meta);
        return this;
    }

    public ItemStack getItemStack(Player p) {
        if (p != null && depend != null) {
            ItemStack is = depend.getItemStack(p);
            if (is != null)
                return is;
        }
        return is;
    }

    public ItemStack getItemStack() {
        return is;
    }

    public OrcItem setItemStack(ItemStack is) {
        Objects.requireNonNull(is, "ItemStack cannot be null");
        this.is = is;
        ItemMeta meta = is.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(meta);
        return this;
    }

    public OrcItem setOnClick(OrcClickListener listener) {
        this.listener = listener;
        return this;
    }

    public OrcItem onClick(Player p, OrcInventory inv) {
        if (listener != null) {
            listener.onClick(p, inv, this);
        }
        if (callback != null)
            callback.run();
        return this;
    }

    public OrcItem setDisplay(String display) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(display);
        is.setItemMeta(meta);
        return this;
    }

    public OrcItem setLore(String... lore) {
        ItemMeta meta = is.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return this;
    }

    public OrcItem removeLore() {
        ItemMeta meta = is.getItemMeta();
        meta.setLore(null);
        is.setItemMeta(meta);
        return this;
    }

    public OrcItem setItemStack(Material mat, String display, String... lore) {
        return setItemStack(mat, (byte) 0, display, Arrays.asList(lore));
    }

    public OrcItem setDepend(DependListener listener) {
        depend = listener;
        return this;
    }

    public OrcItem clone() {
        return new OrcItem(is);
    }
}
