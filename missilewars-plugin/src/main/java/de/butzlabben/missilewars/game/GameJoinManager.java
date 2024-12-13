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

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.menus.hotbar.GameJoinMenu;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.PlayerDataProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public class GameJoinManager {
    
    private final Game game;
    private final TeamManager teamManager;
    
    public GameJoinManager(Game game) {
        this.game = game;
        this.teamManager = game.getTeamManager();
        
        GameJoinMenu.setMenuItems(Config.getGameJoinMenuItems());
    }
    
    /**
     * This method adds the player to the game.
     *
     * @param player (Player) the target Player
     * @param targetTeamType (TeamType) Should the player join in a "player-team" (Team1, Team2) or in the "spectator-team" (Spectator)?
     */
    public void runPlayerJoin(Player player, TeamType targetTeamType) {
        PlayerDataProvider.getInstance().storeInventory(player);
        setDefaultPlayerData(player);

        MWPlayer mwPlayer = addPlayer(player);
        
        // Teleport to Lobby and change the gamemode
        if (game.getState() == GameState.LOBBY) {
            game.teleportToLobbySpawn(player);
            player.setGameMode(GameMode.ADVENTURE);
        }
        
        Team team;
        
        // Default behavior for new players of this game session:
        if (targetTeamType == TeamType.SPECTATOR) {
            team = teamManager.getTeamSpec();
        } else {
            team = teamManager.getNextPlayerTeam();
        }
        
        // Was this player already in this game before he left it?
        if ((game.getState() == GameState.INGAME) || (game.getState() == GameState.END)) {
            boolean isKnownPlayer = game.getGameLeaveManager().isKnownPlayer(player.getUniqueId());
            Team lastTeam = game.getGameLeaveManager().getLastTeamOfKnownPlayer(player.getUniqueId());
            
            if (isKnownPlayer) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_REJOINED)
                        .replace("%last-team%", lastTeam.getFullname()));
                
                if (lastTeam.getTeamType() == targetTeamType) team = lastTeam;
            }
        }
        
        team.addMember(mwPlayer);
        
        sendJoinBroadcastMsg(mwPlayer);
        sendJoinPrivateMsg(mwPlayer, false);
        
        player.setScoreboard(game.getScoreboardManager().getBoard());
        
        if (game.getState() == GameState.LOBBY) {
            getGameJoinMenu(mwPlayer);
            
        } else if (game.getState() == GameState.INGAME) {
            if (team.getTeamType() == TeamType.PLAYER) startForPlayerAfterCountdown(player, true);
            if (team.getTeamType() == TeamType.SPECTATOR) startForPlayer(player, true);

        } else {
            if (team.getTeamType() == TeamType.PLAYER) Logger.ERROR.log("The game-join in the END-phase should not be as player. (Player: " 
                    + player.getName() + ")");
            if (team.getTeamType() == TeamType.SPECTATOR) startForPlayer(player, true);
            
        }
    }
    
    public void runPlayerTeamSwitch(MWPlayer mwPlayer, Team targetTeam) {
        Player player = mwPlayer.getPlayer();
        Team oldTeam = mwPlayer.getTeam();
        
        setDefaultPlayerData(player);
        
        // Remove the player from the old team and add him to the new team
        game.getGameLeaveManager().playerLeaveFromTeam(mwPlayer);
        targetTeam.addMember(mwPlayer);
        
        sendTeamSwitchBroadcastMsg(mwPlayer, oldTeam);
        // Sending the private info message is skipped here.
        
        // Manual update of the scoreboard because the event listener was not addressed.
        game.getScoreboardManager().updateScoreboard();
        
        if (game.getState() == GameState.LOBBY) {
            getGameJoinMenu(mwPlayer);
            
        } else if (game.getState() == GameState.INGAME) {
            if (targetTeam.getTeamType() == TeamType.PLAYER) startForPlayerAfterCountdown(player, false);
            if (targetTeam.getTeamType() == TeamType.SPECTATOR) startForPlayer(player, false);

        } else {
            if (targetTeam.getTeamType() == TeamType.PLAYER) Logger.ERROR.log("The game-join in the END-phase should not be as player. (Player: " 
                    + player.getName() + ")");
            if (targetTeam.getTeamType() == TeamType.SPECTATOR) startForPlayer(player, false);
            
        }
    }
    
    private void setDefaultPlayerData(Player player) {
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
    }
    
    public void startForPlayerAfterCountdown(Player player, boolean isGameJoin) {
        MWPlayer mwPlayer = game.getPlayer(player);
        if (mwPlayer == null) {
            Logger.ERROR.log("Error starting game at player " + player.getName());
            return;
        }
        
        mwPlayer.getTeam().teleportToTeamSpawn(player);
        player.setGameMode(GameMode.SPECTATOR);
        
        runCountdownIntervall(player, "§e5");
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> runCountdownIntervall(player, "§e4"), 20);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> runCountdownIntervall(player, "§e3"), 40);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> runCountdownIntervall(player, "§a2"), 60);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> runCountdownIntervall(player, "§21"), 80);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> startForPlayer(player, isGameJoin), 100);
    }
    
    private void runCountdownIntervall(Player player, String titel) {
        player.sendTitle(titel, "", 10, 20, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100, 3);
    }
    
    public void startForPlayer(Player player, boolean isGameJoin) {
        MWPlayer mwPlayer = game.getPlayer(player);
        if (mwPlayer == null) {
            Logger.ERROR.log("Error starting game at player " + player.getName());
            return;
        }
        
        mwPlayer.getTeam().teleportToTeamSpawn(player);
        if (isGameJoin) {
            mwPlayer.getPlayer().playSound(mwPlayer.getPlayer().getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.6f, 1.4f);
        } else {
            mwPlayer.getPlayer().playSound(mwPlayer.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100, 3);
        }
        
        if (mwPlayer.getTeam().getTeamType() == TeamType.PLAYER) {
            // normal team-player join:
            game.setPlayerAttributes(player);
            game.getEquipmentManager().sendGameItems(player, false);
            mwPlayer.iniPlayerEquipmentRandomizer();
            game.getPlayerTasks().put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), mwPlayer, 40, 20));
            
        } else {
            // spectator join:
            player.setGameMode(GameMode.SPECTATOR);
            
            if ((isGameJoin) && (game.getState() == GameState.INGAME)) {
                if (!player.hasPermission("mw.teammenu")) return;
                Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> {
                    openTeamSelectionMenu(mwPlayer);
                }, 20);
            }
            
        }

    }

    private void sendJoinBroadcastMsg(MWPlayer mwPlayer) {
        Player player = mwPlayer.getPlayer();
        
        String broadcastMsg;
        if (game.getState() == GameState.LOBBY) {
            broadcastMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.LOBBY_PLAYER_JOINED);
        } else {
            broadcastMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_PLAYER_JOINED);
        }
        
        game.broadcast(broadcastMsg.replace("%max_players%", Integer.toString(game.getGameConfig().getMaxPlayers()))
                .replace("%players%", Integer.toString(game.getPlayerAmount()))
                .replace("%player%", player.getName())
                .replace("%team%", (mwPlayer.getTeam() != null) ? mwPlayer.getTeam().getFullname() : "?"));
    }
    
    private void sendTeamSwitchBroadcastMsg(MWPlayer mwPlayer, Team oldTeam) {
        Player player = mwPlayer.getPlayer();
        
        String broadcastMsg;
        if (game.getState() == GameState.LOBBY) {
            broadcastMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.LOBBY_PLAYER_SWITCHED);
        } else {
            broadcastMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_PLAYER_SWITCHED);
        }
        
        game.broadcast(broadcastMsg.replace("%max_players%", Integer.toString(game.getGameConfig().getMaxPlayers()))
                .replace("%players%", Integer.toString(game.getPlayerAmount()))
                .replace("%player%", player.getName())
                .replace("%from%", oldTeam.getFullname())
                .replace("%to%", mwPlayer.getTeam().getFullname()));
    }
    
    public void sendJoinPrivateMsg(MWPlayer mwPlayer, boolean isTeamSwitch) {
        Player player = mwPlayer.getPlayer();
        
        String privateMsg;
        if (mwPlayer.getTeam() == teamManager.getTeamSpec()) {
            if (isTeamSwitch) {
                privateMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_SPECTATOR_TEAM_SWITCH);
            } else {
                privateMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_SPECTATOR_TEAM_ASSIGNED);
            }
            
        } else {
            if (isTeamSwitch) {
                privateMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_PLAYER_TEAM_SWITCH);
            } else {
                privateMsg = PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_PLAYER_TEAM_ASSIGNED);
            }
        }
        
        player.sendMessage(privateMsg.replace("%max_players%", Integer.toString(game.getGameConfig().getMaxPlayers()))
                .replace("%players%", Integer.toString(game.getPlayerAmount()))
                .replace("%player%", player.getName())
                .replace("%team%", (mwPlayer.getTeam() != null) ? mwPlayer.getTeam().getFullname() : "?"));
    }

    private void getGameJoinMenu(MWPlayer mwPlayer) {
        mwPlayer.getGameJoinMenu().getMenu();
    }
    
    private void openTeamSelectionMenu(MWPlayer mwPlayer) {
        mwPlayer.getTeamSelectionMenu().openMenu();
    }
    
    private MWPlayer addPlayer(Player player) {
        if (game.getPlayers().containsKey(player.getUniqueId())) return game.getPlayers().get(player.getUniqueId());
        MWPlayer mwPlayer = new MWPlayer(player, game);
        game.getPlayers().put(player.getUniqueId(), mwPlayer);
        return mwPlayer;
    }
    
    /**
     * This method executes the PlayerTeleportEvent to run the basic game join process
     * after the game is restarted.
     *
     * @param player target player
     */
    public void runTeleportEventForPlayer(Player player) {
        Bukkit.getPluginManager().callEvent(new PlayerTeleportEvent(player,
                Config.getFallbackSpawn(), game.getGameConfig().getSpawnPoint()));
    }
    
}
