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
import de.butzlabben.missilewars.configuration.game.GameConfig;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.player.MWPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MissileWarsPlaceholder extends PlaceholderExpansion {

    private final MissileWars plugin;
    private final String noInformation = "&7?";

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
        return "0.0.2";
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
            if (!offlinePlayer.isOnline()) return noInformation;

            Player player = offlinePlayer.getPlayer();
            Game playerGame = GameManager.getInstance().getGame(player.getLocation());

            if (playerGame == null) {

                // %missilewars_lobby_gamestate_<lobby name or 'this'>%
                if (params.startsWith("lobby_gamestate_")) {
                    return GameState.ERROR.getGameStateMsg();
                }

                // if (params.startsWith("lobby_")) return "§c§oThis is not a lobby area!";
                if (params.startsWith("lobby_")) return noInformation;
                // if (params.startsWith("arena_")) return "§c§oThis is not a game arena!";
                if (params.startsWith("arena_")) return noInformation;
                // if (params.startsWith("player_")) return "§c§oPlayer is not in a game!";
                if (params.startsWith("player_")) return noInformation;
            }
            
            if (playerGame.getArena() == null) {
                // if (params.startsWith("arena_")) return "§c§oThis is not a game arena!";
                if (params.startsWith("arena_")) return noInformation;
            }

            if (params.startsWith("lobby_")) params = params.replace("this", playerGame.getGameConfig().getName());
            if (params.startsWith("arena_")) params = params.replace("this", playerGame.getArena().getName());
            
        }
        
        for (Game game : GameManager.getInstance().getGames().values()) {
            GameConfig gameConfig = game.getGameConfig();
            
            // %missilewars_lobby_gamestate_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_gamestate_" + gameConfig.getName())) {
                return game.getState().getGameStateMsg();
            }
            
            // %missilewars_lobby_mapvote_state_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_mapvote_state_" + gameConfig.getName())) {
                return game.getMapVoting().getState().toString();
            }

            // %missilewars_lobby_displayname_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_displayname_" + gameConfig.getName())) {
                return gameConfig.getDisplayName();
            }
            
            // %missilewars_lobby_team1_name_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team1_name_" + gameConfig.getName())) {
                return gameConfig.getTeam1Config().getName();
            }
            
            // %missilewars_lobby_team1_color_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team1_color_" + gameConfig.getName())) {
                return gameConfig.getTeam1Config().getColor();
            }
            
            // %missilewars_lobby_team2_name_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team2_name_" + gameConfig.getName())) {
                return gameConfig.getTeam2Config().getName();
            }
            
            // %missilewars_lobby_team2_color_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_team2_color_" + gameConfig.getName())) {
                return gameConfig.getTeam2Config().getColor();
            }
            
            // %missilewars_lobby_mapchooseprocedure_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_mapchooseprocedure_" + gameConfig.getName())) {
                return gameConfig.getMapChooseProcedure().toString();
            }
            
            // %missilewars_lobby_gameduration_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_gameduration_" + gameConfig.getName())) {
                return Integer.toString(game.getGameDuration());
            }
            
            // %missilewars_lobby_arenasize_X_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_arenasize_X_" + gameConfig.getName())) {
                if (game.getGameArea() != null) {
                    return Integer.toString(game.getGameArea().getXSize());
                } else {
                    return noInformation;
                }
            }
            
            // %missilewars_lobby_arenasize_Y_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_arenasize_Y_" + gameConfig.getName())) {
                if (game.getGameArea() != null) {
                    return Integer.toString(game.getGameArea().getYSize());
                } else {
                    return noInformation;
                }
            }
            
            // %missilewars_lobby_arenasize_Z_<lobby name or 'this'>%
            if (params.equalsIgnoreCase("lobby_arenasize_Z_" + gameConfig.getName())) {
                if (game.getGameArea() != null) {
                    return Integer.toString(game.getGameArea().getZSize());
                } else {
                    return noInformation;
                }
            }
            
            for (Arena arena : gameConfig.getArenas()) {

                // %missilewars_arena_displayname_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_displayname_" + arena.getName())) {
                    return arena.getDisplayName();
                }

                // %missilewars_arena_missileamount_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_missileamount_" + arena.getName())) {
                    return Integer.toString(arena.getMissileConfig().getSchematics().size());
                }
                
                // %missilewars_arena_gameduration_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_gameduration_" + arena.getName())) {
                    return Integer.toString(arena.getGameDuration());
                }
                
            }
            
            if (game.getPlayers().get(offlinePlayer.getUniqueId()) != null) {
                MWPlayer mwPlayer = game.getPlayers().get(offlinePlayer.getUniqueId());
                
                // %missilewars_player_lobby_displayname%
                if (params.equalsIgnoreCase("player_lobby_displayname")) {
                    return mwPlayer.getGame().getGameConfig().getDisplayName();
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
