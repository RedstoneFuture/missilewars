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

package de.butzlabben.missilewars.listener;

import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.util.MotdManager;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (Config.motdEnabled()) {
            event.setMotd(MotdManager.getInstance().getMotd());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Game game = getGame(event.getEntity().getLocation());
        if (game == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Game game = getGame(event.getPlayer().getLocation());
        if (game == null) return;

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setBuild(false);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Game game = getGame(event.getPlayer().getLocation());
        if (game == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Game game = getGame(event.getPlayer().getLocation());
        if (game == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Game game = getGame(event.getPlayer().getLocation());
        if (game == null) return;

        Player player = event.getPlayer();
        game.teleportToFallbackSpawn(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Game game = getGame(event.getPlayer().getLocation());
        if (game == null) return;

        Player player = event.getPlayer();

        // old game handling:
        registerPlayerArenaLeaveEvent(player, game);

        game.teleportToFallbackSpawn(player);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Game gameFrom = getGame(from);
        Game gameTo = getGame(to);

        // same game:
        if (gameFrom == gameTo) return;

        Player player = event.getPlayer();

        // old game handling:
        if (gameFrom != null) registerPlayerArenaLeaveEvent(player, gameFrom);

        // teleport after a delay between the arena leave and the next area join
        new BukkitRunnable() {
            public void run() {
                // new game handling:
                if (gameTo != null) {
                    PlayerArenaJoinEvent joinEvent = registerPlayerArenaJoinEvent(player, gameTo);
                    if (joinEvent.isCancelled()) gameTo.teleportToFallbackSpawn(player);
                }
            }
        }.runTaskLater(MissileWars.getInstance(), 2);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Game gameFrom = getGame(from);
        Game gameTo = getGame(to);

        // same game:
        if (gameFrom == gameTo) return;

        Player player = event.getPlayer();

        // old game handling:
        if (gameFrom != null) registerPlayerArenaLeaveEvent(player, gameFrom);

        // new game handling:
        if (gameTo != null) {
            PlayerArenaJoinEvent joinEvent = registerPlayerArenaJoinEvent(player, gameTo);
            if (!(joinEvent.isCancelled())) return;
            if (to != null) Game.knockbackEffect(player, from, to);
        }
    }

    private PlayerArenaJoinEvent registerPlayerArenaJoinEvent(Player player, Game game) {
        PlayerArenaJoinEvent onJoinGame = new PlayerArenaJoinEvent(player, game);
        Bukkit.getPluginManager().callEvent(onJoinGame);

        if (!onJoinGame.isCancelled()) {
            game.updateGameInfo();
            sendEventDebugMessage(player, game);
            Logger.NORMAL.log(player.getName() + " joint the MW game " + game.getLobby().getName());
        } else {
            Logger.DEBUG.log("Canceling game join for " + player.getName());
        }

        return onJoinGame;
    }

    private PlayerArenaLeaveEvent registerPlayerArenaLeaveEvent(Player player, Game game) {
        PlayerArenaLeaveEvent onLeaveGame = new PlayerArenaLeaveEvent(player, game);
        Bukkit.getPluginManager().callEvent(onLeaveGame);

        if (!onLeaveGame.isCancelled()) {
            game.updateGameInfo();
            sendEventDebugMessage(player, game);
            Logger.NORMAL.log(player.getName() + " left the MW game " + game.getLobby().getName());
        }

        return onLeaveGame;
    }

    /**
     * This method gets the game based of the location. It's either inside
     * the game lobby (representing as an area) or inside the game arena
     * (representing as a world).
     *
     * @param location (Location) of the player
     * @return the Game Object if existing for the location
     */
    private Game getGame(Location location) {
        if (GameManager.getInstance() == null) return null;

        return GameManager.getInstance().getGame(location);
    }

    private void sendEventDebugMessage(Player player, Game game) {

        Logger.DEBUG.log("Location: " + player.getLocation());
        Logger.DEBUG.log("Current game amount: " + GameManager.getInstance().getGameAmount());
        Logger.DEBUG.log("Lobby: " + game.getLobby().getDisplayName());
        Logger.DEBUG.log("Arena: " + game.getArena().getDisplayName());
        Logger.DEBUG.log("Team 1: " + game.getTeam1());
        Logger.DEBUG.log("Team 2: " + game.getTeam2());

    }
}
