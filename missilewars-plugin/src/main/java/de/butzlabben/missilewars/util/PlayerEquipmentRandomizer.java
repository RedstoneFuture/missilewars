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

import java.util.Random;

/**
 * @author Butzlabben
 * @since 19.01.2018
 */

public class PlayerEquipmentRandomizer {

    private final MWPlayer mwPlayer;
    private final Game game;
    private final Arena arena;

    private final int maxGameDuration;
    private final Random randomizer;
    private static final int DEFAULT_INTERVAL_BY_TEAM_AMOUNT = 20;
    private static final int DEFAULT_FACTOR_BY_GAME_TIME = 1;

    int playerInterval;
    int sendEquipmentCounter = 0;


    public PlayerEquipmentRandomizer(MWPlayer mwPlayer, Game game) {
        this.mwPlayer = mwPlayer;
        this.game = game;
        this.arena = game.getArena();
        randomizer = new Random();
        maxGameDuration = game.getArena().getGameDuration() * 60;

        resetPlayerInterval();
    }

    public void tick() {

        setPlayerInterval(playerInterval - 1);

        if (playerInterval <= 0) {

            sendRandomGameEquipment();
            setPlayerInterval((int) Math.ceil(getIntervalByTeamAmount() * getFactorByGameTime()));
        }
    }

    /**
     * This method resets the countdown for the player equipment
     * randomizer.
     */
    public void resetPlayerInterval() {
        setPlayerInterval(getStartInterval() + 1);
    }

    /**
     * This method sets the countdown for the player equipment
     * randomizer to a specified value.
     *
     * @param playerInterval (Integer) the target interval status
     */
    private void setPlayerInterval(Integer playerInterval) {
        this.playerInterval = playerInterval;
        mwPlayer.getPlayer().setLevel(playerInterval);
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
     * This method gives the player a random item of one of the two
     * game equipment lists. The two lists alternate: after two
     * missiles from the MissileEquipmentList, the player gets a
     * special item from the SpecialEquipmentList.
     */
    private void sendRandomGameEquipment() {

        ItemStack item;
        int randomID;

        // switch between type of "items":
        // after 2 missile items, you get one special item
        if (sendEquipmentCounter >= 2) {

            randomID = randomizer.nextInt(game.getSpecialEquipment().getSpecialEquipmentList().size());
            item = game.getSpecialEquipment().getSpecialEquipmentList().get(randomID);

            sendEquipmentCounter = 0;

        } else {

            randomID = randomizer.nextInt(game.getMissileEquipment().getMissileEquipmentList().size());
            Missile missile = game.getMissileEquipment().getMissileEquipmentList().get(randomID);
            item = missile.getItem();

        }

        if (item == null) return;

        mwPlayer.getPlayer().getInventory().addItem(item);
        sendEquipmentCounter++;
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
            Logger.WARN.log("The given interval mapping in \"" + arena.getName() + "\" is empty. Choosing default value " + DEFAULT_INTERVAL_BY_TEAM_AMOUNT + ".");
            return DEFAULT_INTERVAL_BY_TEAM_AMOUNT;
        }

        int teamSize = mwPlayer.getTeam().getMembers().size();
        for (int i = teamSize; i > 0; i--) {
            if (arena.getInterval().getIntervalsByTeamAmount().containsKey(Integer.toString(i))) {
                return arena.getInterval().getIntervalsByTeamAmount().get(Integer.toString(i));
            }
        }

        Logger.DEBUG.log("No interval value for map \"" + arena.getName() + "\" could be detected based on the team amount of " + teamSize + ". Please define at least one a interval value for a minimal team amount of 1.");
        return DEFAULT_INTERVAL_BY_TEAM_AMOUNT;
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
            Logger.WARN.log("The given interval factor mapping in \"" + arena.getName() + "\" is empty. Choosing default value " + DEFAULT_FACTOR_BY_GAME_TIME + ".");
            return DEFAULT_FACTOR_BY_GAME_TIME;
        }

        int seconds = game.getTimer().getSeconds();
        for (int i = seconds; i <= maxGameDuration; i++) {
            if (arena.getInterval().getIntervalFactorByGameTime().containsKey(Integer.toString(i))) {
                return arena.getInterval().getIntervalFactorByGameTime().get(Integer.toString(i));
            }
        }

        Logger.DEBUG.log("No interval factor value for map \"" + arena.getName() + "\" could be detected based on the game time of " + seconds + " seconds. Please define at least one a interval value for a minimal team amount of " + maxGameDuration + " seconds.");
        return DEFAULT_FACTOR_BY_GAME_TIME;
    }

}
