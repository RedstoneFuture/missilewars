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
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */

@Getter
public class EquipmentManager {

    private final Game game;

    private final MissileGameEquipment missileEquipment;
    private final SpecialGameEquipment specialEquipment;
    private ItemStack customBow;
    private ItemStack customPickaxe;


    public EquipmentManager(Game game) {
        this.game = game;

        missileEquipment = new MissileGameEquipment(game);
        specialEquipment = new SpecialGameEquipment(game);
    }

    /**
     * This method is used to create the game items for the player kit.
     */
    public void createGameItems() {

        // Will it be used ?
        if (game.getArena().getSpawn().isSendBow() || game.getArena().getRespawn().isSendBow()) {

            ItemStack bow = new ItemStack(Material.BOW);
            bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
            bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
            bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
            ItemMeta bowMeta = bow.getItemMeta();
            bowMeta.setUnbreakable(true);
            bowMeta.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
            bow.setItemMeta(bowMeta);
            this.customBow = bow;
        }

        // Will it be used ?
        if (game.getArena().getSpawn().isSendPickaxe() || game.getArena().getRespawn().isSendPickaxe()) {

            ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
            ItemMeta pickaxeMeta = pickaxe.getItemMeta();
            pickaxeMeta.setUnbreakable(true);
            pickaxe.setItemMeta(pickaxeMeta);
            this.customPickaxe = pickaxe;
        }

    }

    /**
     * This method gives the player the starter item set, based on the config.yml
     * configuration for spawn and respawn.
     *
     * @param player    the target player
     * @param isRespawn true, if the player should receive it after a respawn
     */
    public void sendGameItems(Player player, boolean isRespawn) {

        if (isRespawn) {
            if (game.getArena().isKeepInventory()) return;

        } else {
            // clear inventory
            player.getInventory().clear();
        }

        // send armor
        ItemStack[] armor = game.getPlayer(player).getTeam().getTeamArmor();
        player.getInventory().setArmorContents(armor);

        // send kit items
        if (isRespawn) {

            if (game.getArena().getRespawn().isSendBow()) player.getInventory().addItem(this.customBow);
            if (game.getArena().getRespawn().isSendPickaxe()) player.getInventory().addItem(this.customPickaxe);

        } else {

            if (game.getArena().getSpawn().isSendBow()) player.getInventory().addItem(this.customBow);
            if (game.getArena().getSpawn().isSendPickaxe()) player.getInventory().addItem(this.customPickaxe);

        }
    }
}
