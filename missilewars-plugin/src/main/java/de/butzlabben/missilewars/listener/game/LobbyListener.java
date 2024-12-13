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
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.menus.MenuItem;
import de.butzlabben.missilewars.menus.hotbar.GameJoinMenu;
import de.butzlabben.missilewars.menus.inventory.MapVoteMenu;
import de.butzlabben.missilewars.menus.inventory.TeamSelectionMenu;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * @author Butzlabben
 * @since 11.01.2018
 */
public class LobbyListener extends GameBoundListener {

    public LobbyListener(Game game) {
        super(game);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        event.setCancelled(true);

        // prevent spam with the event handling
        if (isInteractDelay(player)) return;
        setInteractDelay(player);

        if (event.getItem() == null) return;
        
        // execution commands from the hotbar menu items
        int slotId = player.getInventory().getHeldItemSlot();
        if (!GameJoinMenu.menuItems.containsKey(slotId)) return;
        
        if ((event.getAction().equals(Action.LEFT_CLICK_AIR)) || (event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            
            MWPlayer mwPlayer = getGame().getPlayer(player);
            if (!mwPlayer.getGameJoinMenu().finalMenuItems.containsKey(slotId)) return;
            
            MenuItem menuItem = mwPlayer.getGameJoinMenu().finalMenuItems.get(slotId);
            menuItem.getLeftClickActions().runActions(player, getGame());
        }
        
        if ((event.getAction().equals(Action.RIGHT_CLICK_AIR)) || (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            
            MWPlayer mwPlayer = getGame().getPlayer(player);
            if (!mwPlayer.getGameJoinMenu().finalMenuItems.containsKey(slotId)) return;
            
            MenuItem menuItem = mwPlayer.getGameJoinMenu().finalMenuItems.get(slotId);
            menuItem.getRightClickActions().runActions(player, getGame());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!isInLobbyArea(player.getLocation())) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;

        event.setRespawnLocation(getGame().getLobby().getSpawnPoint());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!isInLobbyArea(player.getLocation())) return;
        
        // handling of MW inventories:
        if (event.getView().getTitle().equals(TeamSelectionMenu.getTitle()) || 
                event.getView().getTitle().equals(MapVoteMenu.getTitle())) return;
        
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
        if (!isInLobbyArea(player.getLocation())) return;
        
        // handling of MW inventories:
        if (event.getView().getTitle().equals(TeamSelectionMenu.getTitle()) || 
                event.getView().getTitle().equals(MapVoteMenu.getTitle())) {
            if (event.getSlotType() == InventoryType.SlotType.CONTAINER) return;
        }
        
        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
        Logger.DEBUG.log("Cancelled 'InventoryClickEvent' event of " + player.getName());
    }
    
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;
        
        Player player = event.getPlayer();
        
        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
        Logger.DEBUG.log("Cancelled 'PlayerSwapHandItemsEvent' event of " + player.getName());
    }
    
    @EventHandler
    public void onPlayerArenaJoin(PlayerArenaJoinEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;
        
        Player player = event.getPlayer();
        
        // A: game join in a player-team:
        if (!getGame().areTooManyPlayers()) {
            getGame().getGameJoinManager().runPlayerJoin(player, TeamType.PLAYER);
            
        } else if (!getGame().areTooManySpectators()) {
            event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_PLAYER_TEAM_MAX_REACHED));
            getGame().getGameJoinManager().runPlayerJoin(player, TeamType.SPECTATOR);
            
        } else {
            event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_MAX_REACHED));
            event.setCancelled(true);
            
        }
        
    }

    @EventHandler
    public void onPlayerArenaLeave(PlayerArenaLeaveEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        MWPlayer mwPlayer = event.getGame().getPlayer(player);

        if (mwPlayer != null) getGame().getGameLeaveManager().playerLeaveFromGame(mwPlayer);
    }
}
