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

package de.butzlabben.missilewars.player;

import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.equipment.PlayerEquipmentRandomizer;
import de.butzlabben.missilewars.menus.hotbar.GameJoinMenu;
import de.butzlabben.missilewars.menus.inventory.MapVoteMenu;
import de.butzlabben.missilewars.menus.inventory.TeamSelectionMenu;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
@EqualsAndHashCode(of = {"uuid", "id"})
@Getter
public class MWPlayer implements Runnable {

    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    final long id = NEXT_ID.getAndIncrement();
    private final UUID uuid;
    private final Game game;
    @Setter
    private Team team;
    private PlayerEquipmentRandomizer playerEquipmentRandomizer;
    @Setter
    private boolean playerInteractEventCancel = false;
    private GameJoinMenu gameJoinMenu;
    private MapVoteMenu mapVoteMenu;
    private TeamSelectionMenu teamSelectionMenu;
    private long lastTeamChangeTime;

    public MWPlayer(Player player, Game game) {
        this.uuid = player.getUniqueId();
        this.game = game;
        
        this.gameJoinMenu = new GameJoinMenu(this);
        this.mapVoteMenu = new MapVoteMenu(this);
        this.teamSelectionMenu = new TeamSelectionMenu(this);
        
        setLastTeamChangeTime();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    public void iniPlayerEquipmentRandomizer() {
        this.playerEquipmentRandomizer = new PlayerEquipmentRandomizer(this, game);
    }
    
    @Override
    public void run() {
        playerEquipmentRandomizer.tick();
    }

    @Override
    public String toString() {
        return "MWPlayer(uuid=" + uuid + ", id=" + id + ", teamName=" + getTeam().getName() + ")";
    }

    public void setLastTeamChangeTime() {
        this.lastTeamChangeTime = System.currentTimeMillis();
    }

    public long getWaitTimeForTeamChange() {
        // anti-spam intervall in seconds
        int antiSpamTime = Config.getTeamChangeCmdIntervall();
        
        long currentTime = System.currentTimeMillis();
        
        // Output is in seconds.
        return (antiSpamTime - ((currentTime - lastTeamChangeTime) / 1000));
    }
}