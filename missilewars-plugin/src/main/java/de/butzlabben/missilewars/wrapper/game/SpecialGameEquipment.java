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

package de.butzlabben.missilewars.wrapper.game;

import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.version.VersionUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */

@Getter
public class SpecialGameEquipment {

    private final Game game;

    private ItemStack shield;
    private ItemStack arrow;
    private ItemStack fireball;

    private List<ItemStack> specialEquipmentList = new ArrayList<>();


    public SpecialGameEquipment(Game game) {
        this.game = game;

        createShield();
        createArrow();
        createFireball();

        createSpecialEquipmentList();
    }

    /**
     * This method goes through all configured special equipment items
     * and adds them to the list. The higher the defined spawn-occurrence
     * of an item type being set, the more often it will be added to the list.
     */
    private void createSpecialEquipmentList() {

        int shieldOccurrence = game.getArena().getShieldConfiguration().getOccurrence();
        int arrowOccurrence = game.getArena().getArrowOccurrence();
        int fireballOccurrence = game.getArena().getFireballConfiguration().getOccurrence();

        for (int i = shieldOccurrence; i > 0; i--) {
            specialEquipmentList.add(shield);
        }

        for (int i = arrowOccurrence; i > 0; i--) {
            specialEquipmentList.add(arrow);
        }

        for (int i = fireballOccurrence; i > 0; i--) {
            specialEquipmentList.add(fireball);
        }

    }

    /**
     * This method creates the shield item stack.
     */
    private void createShield() {
        shield = new ItemStack(VersionUtil.getSnowball());
        ItemMeta shieldMeta = shield.getItemMeta();
        shieldMeta.setDisplayName(game.getArena().getShieldConfiguration().getName());
        shield.setItemMeta(shieldMeta);
    }

    /**
     * This method creates the arrow item stack.
     */
    private void createArrow() {
        arrow = new ItemStack(Material.ARROW, 3);
    }

    /**
     * This method creates the fireball item stack.
     */
    private void createFireball() {
        fireball = new ItemStack(VersionUtil.getFireball());
        ItemMeta fireballMeta = fireball.getItemMeta();
        fireballMeta.setDisplayName(game.getArena().getFireballConfiguration().getName());
        fireball.setItemMeta(fireballMeta);
    }


}
