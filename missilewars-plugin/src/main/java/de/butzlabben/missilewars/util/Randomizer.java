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
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */
public class Randomizer {

    private final MissileConfiguration missileConfiguration;
    private final Game game;
    private final HashMap<Interval, String> defensive = new HashMap<>();
    private final HashMap<Interval, String> missiles = new HashMap<>();

    private int allMissiles = 0;
    private int allDefensives = 0;
    private int count = 1;

    public Randomizer(Game game) {
        this.game = game;
        missileConfiguration = game.getArena().getMissileConfiguration();
        for (Missile missile : missileConfiguration.getMissiles()) {
            Interval i = new Interval(allMissiles, allMissiles + missile.occurrence() - 1);
            missiles.put(i, missile.getName());
            allMissiles += missile.occurrence();
        }

        int shieldOccurrence = game.getArena().getShieldConfiguration().getOccurrence();
        Interval shield = new Interval(allDefensives, allDefensives + shieldOccurrence - 1);
        allDefensives += shieldOccurrence;
        defensive.put(shield, "s");

        int arrowOccurrence = game.getArena().getArrowOccurrence();
        Interval arrow = new Interval(allDefensives, allDefensives + arrowOccurrence - 1);
        allDefensives += arrowOccurrence;
        defensive.put(arrow, "a");

        Interval fireball = new Interval(allDefensives, allDefensives + arrowOccurrence - 1);
        allDefensives += arrowOccurrence;
        defensive.put(fireball, "f");
    }

    public ItemStack createItem() {
        ItemStack is = null;
        ItemMeta im;
        Random r = new Random();
        if (count == 2) {
            count = 0;
            int random = r.nextInt(allDefensives);
            for (Interval i : defensive.keySet()) {
                if (i.isIn(random)) {
                    String to = defensive.get(i);
                    if (to.equals("s")) {
                        is = new ItemStack(VersionUtil.getSnowball());
                        im = is.getItemMeta();
                        im.setDisplayName(game.getArena().getShieldConfiguration().getName());
                    } else if (to.equals("a")) {
                        is = new ItemStack(Material.ARROW, 3);
                        im = is.getItemMeta();
                    } else {
                        is = new ItemStack(VersionUtil.getFireball());
                        im = is.getItemMeta();
                        im.setDisplayName(game.getArena().getFireballConfiguration().getName());
                    }
                    is.setItemMeta(im);
                    return is;
                }
            }
        } else {
            int random = r.nextInt(allMissiles);
            for (Interval i : missiles.keySet()) {
                if (i.isIn(random)) {
                    String to = missiles.get(i);
                    Missile m = missileConfiguration.getMissileFromName(to);
                    if (m != null) {
                        is = m.getItem();
                    } else
                        Logger.DEBUG.log("There wasn't a missile found, when giving out items");
                    ++count;
                }
            }
        }
        return is;
    }
}
