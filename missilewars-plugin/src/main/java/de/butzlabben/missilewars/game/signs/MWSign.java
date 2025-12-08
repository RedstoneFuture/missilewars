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

package de.butzlabben.missilewars.game.signs;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.util.version.MaterialHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "location")
public class MWSign {

    private Location location;
    private String lobby;

    public boolean isValid() {
        boolean worldExists = location.getWorld() != null;
        boolean lobbyValid = GameManager.getInstance().getGames().containsKey(lobby);
        boolean blockIsSign = MaterialHelper.isSignMaterial(location.getBlock().getType());

        return worldExists && lobbyValid && blockIsSign;
    }

    public boolean isLocation(Location location) {
        return this.location.equals(location);
    }

    public void update() {
        if (!isValid()) return;
        
        Game game = GameManager.getInstance().getGame(getLobby());
        List<String> lines = new ArrayList<>();
        lines.add(replace(PluginMessages.getMessage(false, PluginMessages.MessageEnum.SIGN_0), game));
        lines.add(replace(PluginMessages.getMessage(false, PluginMessages.MessageEnum.SIGN_1), game));
        lines.add(replace(PluginMessages.getMessage(false, PluginMessages.MessageEnum.SIGN_2), game));
        lines.add(replace(PluginMessages.getMessage(false, PluginMessages.MessageEnum.SIGN_3), game));

        // Run sync
        Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> editSign(getLocation(), lines));
    }

    private void editSign(Location location, List<String> lines) {
        Block block = location.getBlock();
        if (!(MaterialHelper.isSignMaterial(block.getType()))) {
            Logger.WARN.log("Configured sign at: " + location + " is not a standing or wall sign");
            return;
        }
        Sign sign = (Sign) block.getState();
        for (int i = 0; i < lines.size(); i++) {
            sign.setLine(i, lines.get(i));
        }
        sign.update(true);
    }

    private String replace(String line, Game game) {

        String gameStateMsg = GameState.ERROR.getGameStateMsg();
        String name = "-";
        
        if (game != null) {
            gameStateMsg = game.getState().getGameStateMsg();
            
            switch (game.getState()) {
                case LOBBY:
                    name = game.getGameConfig().getDisplayName();
                    break;
                case INGAME:
                case END:
                    name = game.getArenaConfig().getDisplayName();
                    break;
            }
        }

        int maxPlayers = (game == null ? 0 : game.getGameConfig().getMaxPlayers());
        int players = (game == null ? 0 : game.getPlayerAmount());

        return line.replace("%state%", gameStateMsg)
                .replace("%arena%", name)
                .replace("%max_players%", Integer.toString(maxPlayers))
                .replace("%players%", Integer.toString(players));
    }
}
