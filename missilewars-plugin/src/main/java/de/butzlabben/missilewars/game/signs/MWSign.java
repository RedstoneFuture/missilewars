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
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.util.version.VersionUtil;
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
        boolean blockIsSign = VersionUtil.isWallSignMaterial(location.getBlock().getType());
        return worldExists && lobbyValid && blockIsSign;
    }

    public boolean isLocation(Location location) {
        return this.location.equals(location);
    }

    public void update() {
        if (!isValid()) {
            Logger.WARN.log("The specified configuration options for the sign at " + location + " for the lobby " + lobby + " are not valid.");
            return;
        }
        Game game = GameManager.getInstance().getGame(getLobby());
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            lines.add(replace(Messages.getNativeMessage("sign." + i), game));
        }
        if (game == null) {
            Logger.WARN.log("Could not find specifed arena \"" + getLobby() + "\" for sign at: " + getLocation().toString());
        }
        // Run sync
        Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> editSign(getLocation(), lines));
    }

    public void editSign(Location location, List<String> lines) {
        Block block = location.getBlock();
        if (!VersionUtil.isWallSignMaterial(block.getType())) {
            Logger.WARN.log("Configured sign at: " + location + " is not a wall sign");
            return;
        }
        Sign sign = (Sign) block.getState();
        for (int i = 0; i < lines.size(); i++) {
            sign.setLine(i, lines.get(i));
        }
        sign.update(true);
    }

    private String replace(String line, Game game) {
        String state = Messages.getNativeMessage("sign.state.error");
        String name = "No game";
        if (game != null) {
            switch (game.getState()) {
                case LOBBY:
                    state = Messages.getNativeMessage("sign.state.lobby");
                    name = game.getLobby().getDisplayName();
                    break;
                case INGAME:
                    state = Messages.getNativeMessage("sign.state.ingame");
                    name = game.getArena().getDisplayName();
                    break;
                case END:
                    state = Messages.getNativeMessage("sign.state.ended");
                    name = game.getArena().getDisplayName();
                    break;
            }
        }
        String replaced = line.replace("%state%", state).replace("%arena%", name);
        int maxPlayers = game == null ? 0 : game.getLobby().getMaxSize();
        int players = game == null ? 0 : game.getPlayers().size();
        replaced = replaced.replace("%max_players%", Integer.toString(maxPlayers)).replace("%players%", Integer.toString(players));
        return replaced;
    }
}
