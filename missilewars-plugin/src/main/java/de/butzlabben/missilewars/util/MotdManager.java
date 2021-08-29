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

import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameState;
import org.bukkit.ChatColor;

public class MotdManager {

    private static final MotdManager instance = new MotdManager();
    private String motd = "&cError in getting Motd";

    public static MotdManager getInstance() {
        return instance;
    }

    public String getMotd() {
        return ChatColor.translateAlternateColorCodes('&', motd);
    }

    public void updateMOTD(Game game) {
        GameState state = game.getState();

        if (Config.motdEnabled()) {
            String motd = "&cError in getting Motd";
            switch (state) {
                case LOBBY:
                    motd = Config.motdLobby();
                    break;
                case END:
                    motd = Config.motdEnded();
                    break;
                case INGAME:
                    motd = Config.motdGame();
                    break;
            }

            String players = "" + game.getPlayers().values().size();
            String maxPlayers = "" + game.getLobby().getMaxSize();
            this.motd = ChatColor.translateAlternateColorCodes('&', motd).replace("%max_players%", maxPlayers).replace("%players%", players);
        }
    }
}
