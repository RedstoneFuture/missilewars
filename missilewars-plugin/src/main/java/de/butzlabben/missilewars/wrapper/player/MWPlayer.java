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
import de.butzlabben.missilewars.util.Randomizer;
import de.butzlabben.missilewars.wrapper.game.Team;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
    int i = -1;
    private Team t;
    private Randomizer r;
    private int period;

    public MWPlayer(Player player, Game game) {
        this.uuid = player.getUniqueId();
        this.game = game;
    }

    public Team getTeam() {
        return t;
    }

    public void setTeam(Team t) {
        this.t = t;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public void run() {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null || !p.isOnline())
            return;
        if (i == -1) {
            i = period - 10;
            if (i >= period || i < 0) i = 0;
        }
        i++;
        if (i >= period) {
            if (r == null)
                r = new Randomizer(game);
            p.getInventory().addItem(r.createItem());
            i = 0;
        }
        p.setLevel(period - i);
    }

    @Override
    public String toString() {
        return "MWPlayer(uuid=" + uuid + ", id=" + id + ", teamName=" + getTeam().getName() + ")";
    }
}