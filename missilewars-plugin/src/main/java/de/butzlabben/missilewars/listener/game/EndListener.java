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

package de.butzlabben.missilewars.listener.game;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.JoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.RejoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.menus.inventory.TeamSelectionMenu;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * @author Butzlabben
 * @since 15.01.2018
 */
public class EndListener extends GameBoundListener {

    public EndListener(Game game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        event.setRespawnLocation(getGame().getArenaConfig().getSpectatorSpawn());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!isInGameWorld(event.getEntity().getLocation())) return;
        
        Player player = event.getEntity();
        MWPlayer mwPlayer = getGame().getPlayer(player);

        event.setDeathMessage(null);
        if (getGame().getArenaConfig().isAutoRespawn()) getGame().autoRespawnPlayer(mwPlayer);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!isInGameWorld(player.getLocation())) return;

        // handling of MW inventories:
        if (event.getView().getTitle().equals(TeamSelectionMenu.getTitle())) return;
        
        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickAsSpectator(InventoryClickEvent event) {
        
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() != GameMode.SPECTATOR) return;
        
        // In Vanilla, the click actions are completely ignored. However, CraftBukkit 
        // will continue to call the events, but it will be canceled by default.
        event.setCancelled(false);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!isInGameWorld(player.getLocation())) return;

        // handling of MW inventories:
        if (event.getView().getTitle().equals(TeamSelectionMenu.getTitle())) {
            if (event.getSlotType() == InventoryType.SlotType.CONTAINER) return;
        }
        
        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
        Logger.DEBUG.log("Cancelled 'InventoryClickEvent' event of " + player.getName());
    }

    @EventHandler
    public void onPlayerArenaJoin(PlayerArenaJoinEvent event) {
        if (!getGame().isIn(event.getPlayer().getLocation())) return;
        
        Player player = event.getPlayer();

        JoinIngameBehavior joinBehavior = getGame().getGameConfig().getJoinIngameBehavior();
        RejoinIngameBehavior rejoinBehavior = getGame().getGameConfig().getRejoinIngameBehavior();
        boolean isKnownPlayer = getGame().getGameLeaveManager().isKnownPlayer(player.getUniqueId());
        Team lastTeam = getGame().getGameLeaveManager().getLastTeamOfKnownPlayer(player.getUniqueId());
        
        // A: Forbidden the game join:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.FORBIDDEN) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.FORBIDDEN)) {
            event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_ENTER_ARENA));
            event.setCancelled(true);
            return;
        }
        
        // B: game join in a player-team --> Forcing the join as a spectator because of the ENDGAME phase:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.PLAYER) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.PLAYER) 
                || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.PLAYER)) {
            
            if (!getGame().areTooManySpectators()) {
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.SPECTATOR);
                
            } else {
                event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_SPECTATOR_TEAM_MAX_REACHED));
                event.setCancelled(true);
                
            }
            return;
        }
        
        // C: game join in a spectator-team:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.SPECTATOR) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.SPECTATOR) 
                || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.SPECTATOR)) {
            
            if (!getGame().areTooManySpectators()) {
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.SPECTATOR);
                
            } else {
                event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_SPECTATOR_TEAM_MAX_REACHED));
                event.setCancelled(true);
                
            }
        }
    }

    @EventHandler
    public void onPlayerArenaLeave(PlayerArenaLeaveEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        MWPlayer mwPlayer = event.getGame().getPlayer(player);

        if (mwPlayer != null) getGame().getGameLeaveManager().playerLeaveFromGame(mwPlayer);
    }
}
