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
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "location")
public class MWSign {

    private Location location;
    private String lobby;

    public boolean isValid() {
        boolean worldExists = location.getWorld() != null;
        boolean lobbyValid = GameManager.getInstance().getGames().containsKey(lobby);
        boolean blockIsSign = isSign(location.getBlock().getBlockData());

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
        lines.add(replace(Messages.getMessage(false, Messages.MessageEnum.SIGN_0), game));
        lines.add(replace(Messages.getMessage(false, Messages.MessageEnum.SIGN_1), game));
        lines.add(replace(Messages.getMessage(false, Messages.MessageEnum.SIGN_2), game));
        lines.add(replace(Messages.getMessage(false, Messages.MessageEnum.SIGN_3), game));

        if (game == null) {
            Logger.WARN.log("Could not find specifed arena \"" + getLobby() + "\" for sign at: " + getLocation().toString());
        }
        // Run sync
        Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> editSign(getLocation(), lines));
    }

    public void editSign(Location location, List<String> lines) {
        Block block = location.getBlock();
        if (!(MWSign.isSign(block.getBlockData()))) {
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
        String state = Messages.getMessage(false, Messages.MessageEnum.SIGN_STATE_ERROR);
        String name = "No game";
        if (game != null) {
            switch (game.getState()) {
                case LOBBY:
                    state = Messages.getMessage(false, Messages.MessageEnum.SIGN_STATE_LOBBY);
                    name = game.getLobby().getDisplayName();
                    break;
                case INGAME:
                    state = Messages.getMessage(false, Messages.MessageEnum.SIGN_STATE_INGAME);
                    name = game.getArena().getDisplayName();
                    break;
                case END:
                    state = Messages.getMessage(false, Messages.MessageEnum.SIGN_STATE_ENDED);
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
    
    public static boolean isSign(BlockData blockData) {
        return ((blockData instanceof org.bukkit.block.data.type.Sign) || (blockData instanceof WallSign));
    }
}
