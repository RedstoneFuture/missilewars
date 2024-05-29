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

package de.butzlabben.missilewars.game.misc;

import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.enums.GameState;
import lombok.Getter;
import org.bukkit.ChatColor;

public class MotdManager {

    @Getter
    private static final MotdManager instance = new MotdManager();
    private String motd = "&cError in getting Motd";

    public String getMotd() {
        return ChatColor.translateAlternateColorCodes('&', motd);
    }

    public void updateMOTD(Game game) {
        GameState state = game.getState();
        String newMotd = "&cError in getting Motd";

        if (Config.motdEnabled()) {
            switch (state) {
                case LOBBY:
                    newMotd = Config.motdLobby();
                    break;
                case END:
                    newMotd = Config.motdEnded();
                    break;
                case INGAME:
                    newMotd = Config.motdGame();
                    break;
            }
            
            motd = ChatColor.translateAlternateColorCodes('&', newMotd)
                    .replace("%max_players%", Integer.toString(game.getLobby().getMaxPlayers()))
                    .replace("%players%", Integer.toString(game.getPlayerAmount()))
                    .replace("%prefix%", Messages.getPrefix());
        }
    }
}