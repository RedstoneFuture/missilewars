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

import java.util.Comparator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerStatsComparator implements Comparator<PlayerStats> {

    private final PlayerStatsCompareCriteria compareCriteria;
    private final boolean descending;

    public PlayerStatsComparator() {
        this(PlayerStatsCompareCriteria.NAME, false);
    }

    @Override
    public int compare(PlayerStats o1, PlayerStats o2) {
        int sort = 0;
        switch (compareCriteria) {
            case NAME:
                sort = o1.compareTo(o2);
                break;
            case GAMES:
                sort = Integer.compare(o1.getGamesPlayed(), o2.getGamesPlayed());
                break;
            case WL:
                sort = Double.compare(o1.getWinToLoseRatio(), o2.getGamesPlayed());
        }
        if (descending) {
            sort = -sort;
        }
        return sort;
    }

    public enum PlayerStatsCompareCriteria {
        NAME, WL, GAMES
    }
}
