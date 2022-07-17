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
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.PlayerDataProvider;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import org.bukkit.Bukkit;
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

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onJoin(PlayerArenaJoinEvent event) {
        Game game = event.getGame();
        if (game != getGame()) return;

        Player p = event.getPlayer();
        PlayerDataProvider.getInstance().storeInventory(p);
        p.sendMessage(MessageConfig.getMessage("spectator"));

        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.teleport(game.getArena().getSpectatorSpawn()), 2);

        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.setGameMode(GameMode.SPECTATOR), 35);
        p.setDisplayName("§7" + p.getName() + "§r");
        game.addPlayer(p);
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
        if (getGame().getArena().isAutoRespawn()) getGame().autoRespawnPlayer(player);

        event.setDeathMessage(null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player p = (Player) event.getWhoClicked();
        if (isInGameWorld(p.getLocation()))
            if (p.getGameMode() != GameMode.CREATIVE && !p.isOp())
                event.setCancelled(true);
    }
}
