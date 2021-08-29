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

package de.butzlabben.missilewars.wrapper.stats;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString(of = {"name", "uuid"})
public class PlayerStats implements Comparable<PlayerStats> {

    private final UUID uuid;
    private final int wins, loses;
    private final int gamesPlayed;
    private final int team1, team2;
    @Setter
    private String name;

    public double getWinToLoseRatio() {
        double loses = this.loses == 0 ? 1 : this.loses;
        return wins / loses;
    }

    public double getTeamRatio() {
        double team2 = this.team2 == 0 ? 1 : this.team2;
        double ratio = team1 / team2;
        if (ratio > 2) return 2;
        else if (ratio < 1) return 1;
        else return ratio;
    }

    @Override
    public int compareTo(PlayerStats o) {
        if (o.getName() == null) return 0;
        if (name == null) return 0;
        return name.compareTo(o.getName());
    }
}
