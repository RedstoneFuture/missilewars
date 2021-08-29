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


import de.butzlabben.missilewars.inventory.OrcItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * @author Butzlabben
 * @since 21.05.2018
 */
public class PageGUICreator<T> {

    private final int elementsPerPage;
    private final String title;
    @Getter
    private final List<T> elements;
    private final ItemConverter<T> converter;
    private final Map<Integer, OrcItem> specialItems;
    @Getter
    private List<InventoryPage> invPages;

    public PageGUICreator(String title, Collection<T> elements, ItemConverter<T> converter) {
        this(title, elements, converter, Collections.emptyMap(), 4 * 9);
    }

    public PageGUICreator(String title, Collection<T> elements, ItemConverter<T> converter, Map<Integer, OrcItem> specialItems) {
        this(title, elements, converter, specialItems, 4 * 9);
    }

    public PageGUICreator(String title, Collection<T> elements, ItemConverter<T> converter, Map<Integer, OrcItem> specialItems, int elementsPerPage) {
        this.title = title;
        this.elements = new ArrayList<>(elements);
        this.converter = converter;
        this.elementsPerPage = elementsPerPage;
        this.specialItems = specialItems;
    }

    public void show(Player p) {
        List<OrcItem> items = elements.stream().map(converter::convert).collect(Collectors.toList());
        if (items.size() == 0)
            return;

        int pages = (int) (Math.ceil((items.size() / (double) elementsPerPage) < 1 ? 1 : Math.ceil((double) items.size() / (double) elementsPerPage)));

        invPages = new ArrayList<>(pages);

        for (int i = 1; i < pages + 1; i++) {
            int start = i == 1 ? 0 : elementsPerPage * (i - 1);
            int end = Math.min(items.size(), elementsPerPage * i);
            List<OrcItem> page = items.subList(start, end);
            InventoryPage invPage = new InventoryPage(title, i, pages);

            page.forEach(invPage::addItem);
            specialItems.forEach(invPage::addItem);

            invPages.add(invPage);
        }

        for (int i = 0; i < invPages.size(); i++) {

            int beforeIndex = i == 0 ? invPages.size() - 1 : i - 1;
            int nextIndex = i == invPages.size() - 1 ? 0 : i + 1;

            invPages.get(i).before = invPages.get(beforeIndex);
            invPages.get(i).next = invPages.get(nextIndex);
        }

        if (p != null && p.isOnline())
            p.openInventory(invPages.get(0).getInventory(p));
    }

    public void reopen(Player player) {
        player.closeInventory();
        show(player);
    }
}
