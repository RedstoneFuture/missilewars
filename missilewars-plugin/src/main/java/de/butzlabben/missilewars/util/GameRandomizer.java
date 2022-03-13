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

package de.butzlabben.missilewars.util;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.arena.MissileConfiguration;
import de.butzlabben.missilewars.wrapper.missile.Missile;
import de.butzlabben.missilewars.wrapper.player.Interval;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Random;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */
public class GameRandomizer {

    private final MissileConfiguration missileConfiguration;
    private final Game game;
    private final HashMap<Interval, String> defensive = new HashMap<>();
    private final HashMap<Interval, String> missiles = new HashMap<>();

    private int totalOccurrenceMissiles = 0;
    private int totalOccurrenceDefensive = 0;
    private int randomizeCounter = 1;

    private ItemStack shield;
    private ItemStack arrow;
    private ItemStack fireball;


    public GameRandomizer(Game game) {
        this.game = game;
        missileConfiguration = game.getArena().getMissileConfiguration();

        createDefenseItems();

        // get missiles
        for (Missile missile : missileConfiguration.getMissiles()) {

            Interval interval = new Interval(totalOccurrenceMissiles, totalOccurrenceMissiles + missile.occurrence() - 1);
            missiles.put(interval, missile.getName());
            totalOccurrenceMissiles += missile.occurrence();
        }

        // get shield
        int shieldOccurrence = game.getArena().getShieldConfiguration().getOccurrence();
        Interval shield = new Interval(totalOccurrenceDefensive, totalOccurrenceDefensive + shieldOccurrence - 1);
        totalOccurrenceDefensive += shieldOccurrence;
        defensive.put(shield, "s");

        // get arrow
        int arrowOccurrence = game.getArena().getArrowOccurrence();
        Interval arrow = new Interval(totalOccurrenceDefensive, totalOccurrenceDefensive + arrowOccurrence - 1);
        totalOccurrenceDefensive += arrowOccurrence;
        defensive.put(arrow, "a");

        // get fireball
        Interval fireball = new Interval(totalOccurrenceDefensive, totalOccurrenceDefensive + arrowOccurrence - 1);
        totalOccurrenceDefensive += arrowOccurrence;
        defensive.put(fireball, "f");
    }

    public ItemStack getRandomItem() {
        Random randomizer = new Random();
        int random;

        ItemStack itemStack = null;

        // switch between type of "items":
        // after 2 occurrence items, you get one defensive item
        if (randomizeCounter == 2) {
            randomizeCounter = 0;
            random = randomizer.nextInt(totalOccurrenceDefensive);
            
            for (Interval interval : defensive.keySet()) {
                if (interval.isIn(random)) {

                    switch (defensive.get(interval)) {
                        case "s": return shield;
                        case "a": return arrow;
                        case "f": return fireball;
                        default: return null;
                    }

                }
            }
        } else {
            random = randomizer.nextInt(totalOccurrenceMissiles);
            
            for (Interval interval : missiles.keySet()) {
                if (interval.isIn(random)) {
                    String randomItem = missiles.get(interval);
                    Missile missile = missileConfiguration.getMissileFromName(randomItem);

                    if (missile != null) {
                        itemStack = missile.getItem();
                    } else
                        Logger.DEBUG.log("There wasn't a missile found, when giving out items");
                    randomizeCounter++;
                }
            }
        }
        return itemStack;
    }

    /**
     * This method creates the item stacks for the defense random-items.
     */
    private void createDefenseItems() {

        // create MW shield
        shield = new ItemStack(VersionUtil.getSnowball());
        ItemMeta shieldMeta = shield.getItemMeta();
        shieldMeta.setDisplayName(game.getArena().getShieldConfiguration().getName());
        shield.setItemMeta(shieldMeta);

        // create MW arrow
        arrow = new ItemStack(Material.ARROW, 3);

        // create MW fireball
        fireball = new ItemStack(VersionUtil.getFireball());
        ItemMeta fireballMeta = fireball.getItemMeta();
        fireballMeta.setDisplayName(game.getArena().getFireballConfiguration().getName());
        fireball.setItemMeta(fireballMeta);

    }

}
