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

import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
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

        event.setRespawnLocation(getGame().getArena().getSpectatorSpawn());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!isInGameWorld(event.getEntity().getLocation())) return;

        Player player = event.getEntity();

        event.setDeathMessage(null);
        if (getGame().getArena().isAutoRespawn()) getGame().autoRespawnPlayer(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!isInGameWorld(player.getLocation())) return;

        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerArenaJoin(PlayerArenaJoinEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        if (getGame().isSpectatorsMax()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageConfig.getMessage("not_enter_arena"));
            return;
        }

        Player player = event.getPlayer();
        getGame().playerJoinInGame(player, true);
    }

    @EventHandler
    public void onPlayerArenaLeave(PlayerArenaLeaveEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        MWPlayer mwPlayer = event.getGame().getPlayer(player);

        if (mwPlayer != null) getGame().playerLeaveFromGame(mwPlayer);
    }
}
