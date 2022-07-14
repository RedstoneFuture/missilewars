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

package de.butzlabben.missilewars.wrapper.player;

import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.PlayerEquipmentRandomizer;
import de.butzlabben.missilewars.wrapper.game.Team;
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
    @Setter private Team team;
    private PlayerEquipmentRandomizer randomGameEquipment;

    public MWPlayer(Player player, Game game) {
        this.uuid = player.getUniqueId();
        this.game = game;
        this.randomGameEquipment = new PlayerEquipmentRandomizer(this, game);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public void run() {

        Player p = Bukkit.getPlayer(uuid);
        if (p == null || !p.isOnline()) return;

        randomGameEquipment.tick();
    }

    @Override
    public String toString() {
        return "MWPlayer(uuid=" + uuid + ", id=" + id + ", teamName=" + getTeam().getName() + ")";
    }
}