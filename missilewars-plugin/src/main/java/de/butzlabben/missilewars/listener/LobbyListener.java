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
import de.butzlabben.missilewars.inventory.VoteInventory;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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

        if (VersionUtil.isStainedGlassPane(event.getItem().getType())) {
            // team change:
            if (!player.hasPermission("mw.change")) return;

            String displayName = event.getItem().getItemMeta().getDisplayName();

            // same team:
            if (displayName.equals(getGame().getPlayer(player).getTeam().getFullname())) return;

            // too late for team change:
            if (getGame().getTimer().getSeconds() < 10) {
                player.sendMessage(MessageConfig.getMessage("change_team_not_now"));
                return;
            }

            if (displayName.equals(getGame().getTeam1().getFullname())) {
                player.performCommand("mw change 1");
            } else {
                player.performCommand("mw change 2");
            }
            getGame().getScoreboardManager().updateScoreboard();

        } else if (event.getItem().getType() == Material.NETHER_STAR) {
            // vote inventory:
            VoteInventory inventory = new VoteInventory(getGame().getLobby().getArenas());
            player.openInventory(inventory.getInventory(player));
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!isInLobbyArea(player.getLocation())) return;

        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerArenaJoin(PlayerArenaJoinEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;

        if (getGame().isPlayersMax()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageConfig.getMessage("not_enter_arena"));
            return;
        }

        Player player = event.getPlayer();
        getGame().playerJoinInGame(player, false);
    }

    @EventHandler
    public void onPlayerArenaLeave(PlayerArenaLeaveEvent event) {
        if (!isInLobbyArea(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        MWPlayer mwPlayer = event.getGame().getPlayer(player);

        if (mwPlayer != null) getGame().playerLeaveFromGame(mwPlayer);
    }
}
