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

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Lobby;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.player.MWPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MissileWarsPlaceholder extends PlaceholderExpansion {

    private final MissileWars plugin;

    public MissileWarsPlaceholder(MissileWars plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Daniel Nägele";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "missilewars";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "0.0.1";
    }

    // This is required or else PlaceholderAPI will unregister the expansion on reload
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {

        if (params.endsWith("_this") || params.startsWith("player_")) {
            // if (!offlinePlayer.isOnline()) return "§c§oPlayer is not online!";
            if (!offlinePlayer.isOnline()) return "";

            Player player = offlinePlayer.getPlayer();
            Game playerGame = GameManager.getInstance().getGame(player.getLocation());

            if (playerGame == null) {

                // %missilewars_lobby_gamestate_<lobby name or 'this'>%
                if (params.startsWith("lobby_gamestate_")) {
                    return GameManager.getInstance().getGameStateMessage(null);
                }

                // if (params.startsWith("lobby_")) return "§c§oThis is not a lobby area!";
                if (params.startsWith("lobby_")) return "";
                // if (params.startsWith("arena_")) return "§c§oThis is not a game arena!";
                if (params.startsWith("arena_")) return "";
                // if (params.startsWith("player_")) return "§c§oPlayer is not in a game!";
                if (params.startsWith("player_")) return "";
            }

            if (params.startsWith("lobby_")) params = params.replace("this", playerGame.getLobby().getName());
            if (params.startsWith("arena_")) params = params.replace("this", playerGame.getArena().getName());
            
        }
        
        for (Game game : GameManager.getInstance().getGames().values()) {
            Lobby lobby = game.getLobby();
            
            // %missilewars_lobby_gamestate_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_gamestate_" + lobby.getName())) {
                return GameManager.getInstance().getGameStateMessage(game);
            }

            // %missilewars_lobby_displayname_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_displayname_" + lobby.getName())) {
                return lobby.getDisplayName();
            }
            
            // %missilewars_lobby_team1_name_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team1_name_" + lobby.getName())) {
                return lobby.getTeam1Name();
            }
            
            // %missilewars_lobby_team1_color_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team1_color_" + lobby.getName())) {
                return lobby.getTeam1Color();
            }
            
            // %missilewars_lobby_team2_name_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team2_name_" + lobby.getName())) {
                return lobby.getTeam2Name();
            }
            
            // %missilewars_lobby_team2_color_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team2_color_" + lobby.getName())) {
                return lobby.getTeam2Color();
            }
            
            for (Arena arena : lobby.getArenas()) {

                // %missilewars_arena_displayname_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_displayname_" + arena.getName())) {
                    return arena.getDisplayName();
                }

                // %missilewars_arena_missileamount_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_missileamount_" + arena.getName())) {
                    return Integer.toString(arena.getMissileConfiguration().getSchematics().size());
                }

            }
            
            if (game.getPlayers().get(offlinePlayer.getUniqueId()) != null) {
                MWPlayer mwPlayer = game.getPlayers().get(offlinePlayer.getUniqueId());
                
                // %missilewars_player_lobby_displayname%
                if (params.equalsIgnoreCase("player_lobby_displayname")) {
                    return game.getLobby().getDisplayName();
                }
                
                // %missilewars_player_team_name%
                if (params.equalsIgnoreCase("player_team_name")) {
                    return mwPlayer.getTeam().getName();
                }
                
                // %missilewars_player_team_color%
                if (params.equalsIgnoreCase("player_team_color")) {
                    return mwPlayer.getTeam().getColor();
                }
                
            }
            
        }
        
        // Placeholder is unknown by the expansion
        return null;
    }

}
