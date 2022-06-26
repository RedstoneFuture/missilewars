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
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import de.butzlabben.missilewars.wrapper.missile.Missile;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */

public class PlayerEquipmentRandomizer {

    private final MWPlayer mwPlayer;
    private final Game game;
    private final Arena arena;

    private final int maxGameDuration;
    private final int defaultIntervalByTeamAmount = 20;
    private final int defaultFactorByGameTime = 1;

    int playerInterval;
    int sendEquipmentCounter = 0;


    public PlayerEquipmentRandomizer(MWPlayer mwPlayer, Game game) {
        this.mwPlayer = mwPlayer;
        this.game = game;
        this.arena = game.getArena();
        maxGameDuration = game.getArena().getGameDuration() * 60;

        setPlayerInterval(getStartInterval() + 1);
    }

    public void nextPeriod() {

        playerInterval--;
        mwPlayer.getPlayer().setLevel(playerInterval);

        if (playerInterval <= 0) {

            sendRandomGameEquipment();
            setPlayerInterval((int) Math.ceil(getIntervalByTeamAmount() * getFactorByGameTime()));
        }

    }

    private void setPlayerInterval(Integer playerInterval) {
        this.playerInterval = playerInterval;
        mwPlayer.getPlayer().setLevel(playerInterval);
    }

    /**
     * This method randomly shuffles the game equipment list and gives
     * the player the first item of that list. The ratio is 2:1 between
     * missiles and special items.
     */
    private void sendRandomGameEquipment() {

        ItemStack item;

        // switch between type of "items":
        // after 2 missile items, you get one special item
        if (sendEquipmentCounter >= 2) {

            Collections.shuffle(game.getSpecialEquipment().getSpecialEquipmentList());
            item = game.getSpecialEquipment().getSpecialEquipmentList().get(0);

            sendEquipmentCounter = 0;

        } else {

            Collections.shuffle(game.getMissileEquipment().getMissileEquipmentList());
            Missile missile = game.getMissileEquipment().getMissileEquipmentList().get(0);
            item = missile.getItem();

        }

        if (item == null) return;

        mwPlayer.getPlayer().getInventory().addItem(item);
        sendEquipmentCounter++;
    }

    /**
     * This method returns the interval after the player receives a new
     * item at game start.
     *
     * @return (int) the interval in seconds
     */
    private int getStartInterval() {
        return arena.getInterval().getStart();
    }

    /**
     * This method returns the interval after the player receives a new
     * item during the game. It depends on the current team size and the
     * same or next lower key value in the config.
     *
     * @return (int) the interval in seconds
     */
    private int getIntervalByTeamAmount() {

        if (arena.getInterval().getIntervalsByTeamAmount().isEmpty()) {
            Logger.WARN.log("The given interval mapping in \"" + arena.getName() + "\" is empty. Choosing default value " + defaultIntervalByTeamAmount + ".");
            return defaultIntervalByTeamAmount;
        }

        int teamSize = mwPlayer.getTeam().getMembers().size();
        for (int i = teamSize; i > 0; i--) {
            if (arena.getInterval().getIntervalsByTeamAmount().containsKey(Integer.toString(i))) {
                return arena.getInterval().getIntervalsByTeamAmount().get(Integer.toString(i));
            }
        }

        Logger.DEBUG.log("No interval value for map \"" + arena.getName() + "\" could be detected based on the team amount of " + teamSize + ". Please define at least one a interval value for a minimal team amount of 1.");
        return defaultIntervalByTeamAmount;
    }

    /**
     * This method returns the interval factor after the player receives a new
     * item during the game. It depends on the current game time and the
     * same or next higher key value in the config.
     *
     * @return (int) the interval factor in seconds
     */
    private double getFactorByGameTime() {

        if (arena.getInterval().getIntervalFactorByGameTime().isEmpty()) {
            Logger.WARN.log("The given interval factor mapping in \"" + arena.getName() + "\" is empty. Choosing default value " + defaultFactorByGameTime + ".");
            return defaultFactorByGameTime;
        }

        int seconds = game.getTimer().getSeconds();
        for (int i = seconds; i <= maxGameDuration; i++) {
            if (arena.getInterval().getIntervalFactorByGameTime().containsKey(Integer.toString(i))) {
                return arena.getInterval().getIntervalFactorByGameTime().get(Integer.toString(i));
            }
        }

        Logger.DEBUG.log("No interval factor value for map \"" + arena.getName() + "\" could be detected based on the game time of " + seconds + ". Please define at least one a interval value for a minimal team amount of " + maxGameDuration + ".");
        return defaultFactorByGameTime;
    }

}
