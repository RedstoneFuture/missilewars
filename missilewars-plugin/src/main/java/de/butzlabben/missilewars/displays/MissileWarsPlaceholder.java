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

package de.butzlabben.missilewars.displays;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.game.GameConfig;
import de.butzlabben.missilewars.configuration.arena.ArenaConfig;
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
        return "RedstoneFuture";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "missilewars";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "0.0.3";
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

                // %missilewars_game_gamestate_<game name or 'this'>%
                if (params.startsWith("game_gamestate_")) {
                    return GameState.ERROR.getGameStateMsg();
                }

                // if (params.startsWith("game_")) return "§c§oThis is not a game area!";
                if (params.startsWith("game_")) return noInformation;
                // if (params.startsWith("arena_")) return "§c§oThis is not a game arena!";
                if (params.startsWith("arena_")) return noInformation;
                // if (params.startsWith("player_")) return "§c§oPlayer is not in a game!";
                if (params.startsWith("player_")) return noInformation;
            }
            
            if (playerGame.getArenaConfig() == null) {
                // if (params.startsWith("arena_")) return "§c§oThis is not a game arena!";
                if (params.startsWith("arena_")) return noInformation;
            }

            if (params.startsWith("game_")) params = params.replace("this", playerGame.getGameConfig().getName());
            if (params.startsWith("arena_")) params = params.replace("this", playerGame.getArenaConfig().getName());
            
        }
        
        for (Game game : GameManager.getInstance().getGames().values()) {
            GameConfig gameConfig = game.getGameConfig();
            
            // %missilewars_game_gamestate_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_gamestate_" + gameConfig.getName())) {
                return game.getState().getGameStateMsg();
            }
            
            // %missilewars_game_mapvote_state_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_mapvote_state_" + gameConfig.getName())) {
                return game.getMapVoting().getState().toString();
            }

            // %missilewars_game_displayname_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_displayname_" + gameConfig.getName())) {
                return gameConfig.getDisplayName();
            }
            
            // %missilewars_game_team1_name_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_team1_name_" + gameConfig.getName())) {
                return gameConfig.getTeam1Config().getName();
            }
            
            // %missilewars_game_team1_color_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_team1_color_" + gameConfig.getName())) {
                return gameConfig.getTeam1Config().getColor();
            }
            
            // %missilewars_game_team2_name_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_team2_name_" + gameConfig.getName())) {
                return gameConfig.getTeam2Config().getName();
            }
            
            // %missilewars_game_team2_color_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_team2_color_" + gameConfig.getName())) {
                return gameConfig.getTeam2Config().getColor();
            }
            
            // %missilewars_game_mapchooseprocedure_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_mapchooseprocedure_" + gameConfig.getName())) {
                return gameConfig.getMapChooseProcedure().toString();
            }
            
            // %missilewars_game_gameduration_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_gameduration_" + gameConfig.getName())) {
                return Integer.toString(game.getGameDuration());
            }
            
            // %missilewars_game_arenasize_X_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_arenasize_X_" + gameConfig.getName())) {
                if (game.getGameArea() != null) {
                    return Integer.toString(game.getGameArea().getXSize());
                } else {
                    return noInformation;
                }
            }
            
            // %missilewars_game_arenasize_Y_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_arenasize_Y_" + gameConfig.getName())) {
                if (game.getGameArea() != null) {
                    return Integer.toString(game.getGameArea().getYSize());
                } else {
                    return noInformation;
                }
            }
            
            // %missilewars_game_arenasize_Z_<game name or 'this'>%
            if (params.equalsIgnoreCase("game_arenasize_Z_" + gameConfig.getName())) {
                if (game.getGameArea() != null) {
                    return Integer.toString(game.getGameArea().getZSize());
                } else {
                    return noInformation;
                }
            }
            
            for (ArenaConfig arenaConfig : gameConfig.getArenas()) {

                // %missilewars_arena_displayname_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_displayname_" + arenaConfig.getName())) {
                    return arenaConfig.getDisplayName();
                }

                // %missilewars_arena_missileamount_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_missileamount_" + arenaConfig.getName())) {
                    return Integer.toString(arenaConfig.getMissileConfig().getSchematics().size());
                }
                
                // %missilewars_arena_gameduration_<arena name or 'this'>%
                if (params.equalsIgnoreCase("arena_gameduration_" + arenaConfig.getName())) {
                    return Integer.toString(arenaConfig.getGameDuration());
                }
                
            }
            
            if (game.getPlayers().get(offlinePlayer.getUniqueId()) != null) {
                MWPlayer mwPlayer = game.getPlayers().get(offlinePlayer.getUniqueId());
                
                // %missilewars_player_game_displayname%
                if (params.equalsIgnoreCase("player_game_displayname")) {
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
