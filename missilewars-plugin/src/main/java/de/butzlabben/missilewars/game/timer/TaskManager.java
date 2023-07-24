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

package de.butzlabben.missilewars.game.timer;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Butzlabben
 * @since 14.01.2018
 */

@Getter
public class TaskManager {

    private final Game game;

    @Setter private Timer timer;
    private BukkitTask bukkitTask;

    public TaskManager(Game game) {
        this.game = game;
    }

    public void runTimer(long delay, long period) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), timer, delay, period);
    }

    public void stopTimer() {
        if (bukkitTask != null)
            bukkitTask.cancel();
    }

}
