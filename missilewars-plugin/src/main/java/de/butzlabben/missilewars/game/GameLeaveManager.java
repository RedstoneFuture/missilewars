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

package de.butzlabben.missilewars.game;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.PlayerDataProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
public class GameLeaveManager {
    
    private final Game game;
    @SuppressWarnings("unused")
    private final TeamManager teamManager;
    
    private final HashMap<UUID, Team> leftPlayerCache = new HashMap<>();
    
    public GameLeaveManager(Game game) {
        this.game = game;
        this.teamManager = game.getTeamManager();
    }
    
    /**
     * This method handles the removal of the player from the game.
     *
     * @param mwPlayer the target MissileWars player
     */
    public void playerLeaveFromGame(MWPlayer mwPlayer) {
        Player player = mwPlayer.getPlayer();
        Team team = mwPlayer.getTeam();
        
        playerLeaveFromTeam(mwPlayer);
        game.removePlayer(mwPlayer);
        
        PlayerDataProvider.getInstance().loadInventory(player);

        String message = null;
        if (game.getState() == GameState.LOBBY) {
            message = PluginMessages.getMessage(true, PluginMessages.MessageEnum.LOBBY_PLAYER_LEFT);
        } else if (game.getState() == GameState.INGAME) {
            message = PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_PLAYER_LEFT);
        }

        if (message != null) {
            game.broadcast(message.replace("%max_players%", Integer.toString(game.getGameConfig().getMaxPlayers()))
                    .replace("%players%", Integer.toString(game.getPlayerAmount()))
                    .replace("%player%", player.getName())
                    .replace("%team%", team.getFullname()));
        }

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        
        if (game.getState() == GameState.LOBBY) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.LOBBY_LEFT)
                    .replace("%game_name%", game.getGameConfig().getDisplayName()));
        } else if (game.getState() == GameState.INGAME) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_LEFT)
                    .replace("%arena_name%", game.getArenaConfig().getDisplayName()));
        }

    }
    
    public void playerLeaveFromTeam(MWPlayer mwPlayer) {
        Team oldTeam = mwPlayer.getTeam();

        leftPlayerCache.put(mwPlayer.getUuid(), oldTeam);
        
        if (game.getState() == GameState.INGAME) {
            BukkitTask task = game.getPlayerTasks().get(mwPlayer.getUuid());
            if (task != null) task.cancel();
        }
        
        oldTeam.removeMember(mwPlayer);
        if (game.getState() == GameState.INGAME) checkTeamSize(oldTeam);
    }
    
    private void checkTeamSize(Team team) {
        if (team.getTeamType() == TeamType.SPECTATOR) return;
        
        int teamSize = team.getMembers().size();
        if (teamSize == 0) {
            Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> {
                team.getEnemyTeam().setGameResult(GameResult.WIN);
                team.setGameResult(GameResult.LOSE);
                game.sendGameResult();
                game.stopGame();
            });
            game.broadcast(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_ALL_TEAMMATES_OFFLINE)
                    .replace("%team%", team.getFullname()));
        }
    }
    
    /**
     * This method checks whether the specified player has already played in this game 
     * before leaving it based of the player-cache.
     * 
     * @param uuid (UUID) the target player UUID
     * @return 'true' if the target player already played in this game
     */
    public boolean isKnownPlayer(UUID uuid) {
        return leftPlayerCache.containsKey(uuid);
    }
    
    /**
     * This method returns the last team the player was in before leaving the game.
     * 
     * @param uuid (UUID) the target player UUID
     * @return team (Team) the last team of the player (player team or spectator team)
     */
    public Team getLastTeamOfKnownPlayer(UUID uuid) {
        return leftPlayerCache.get(uuid);
    }
    
}
