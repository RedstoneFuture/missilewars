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
import de.butzlabben.missilewars.inventory.OrcItem;
import de.butzlabben.missilewars.inventory.VoteInventory;
import de.butzlabben.missilewars.util.PlayerDataProvider;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.MapChooseProcedure;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import de.butzlabben.missilewars.wrapper.signs.MWSign;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    public void onClick(PlayerInteractEvent e) {
        if (!isInLobbyArea(e.getPlayer().getLocation())) return;

        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
        if (e.getItem() == null) return;

        if (VersionUtil.isStainedGlassPane(e.getItem().getType())) {

            if (!p.hasPermission("mw.change")) return;

            if (getGame().getTimer().getSeconds() < 10) {
                p.sendMessage(MessageConfig.getMessage("change_team_not_now"));
                return;
            }

            String displayName = e.getItem().getItemMeta().getDisplayName();
            if (displayName.equals(getGame().getTeam1().getFullname())) {
                p.performCommand("mw change 1");
                getGame().getScoreboardManager().updateScoreboard();
            } else {
                p.performCommand("mw change 2");
                getGame().getScoreboardManager().updateScoreboard();
            }

        } else if (e.getItem().getType() == Material.NETHER_STAR) {
            VoteInventory inventory = new VoteInventory(getGame().getLobby().getArenas());
            p.openInventory(inventory.getInventory(p));
        }

    }

    @EventHandler
    public void onJoin(PlayerArenaJoinEvent e) {
        Game game = e.getGame();
        if (game != getGame()) return;

        Player p = e.getPlayer();
        MWPlayer mw = game.addPlayer(p);

        PlayerDataProvider.getInstance().storeInventory(p);

        p.getInventory().clear();
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());

        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.setGameMode(GameMode.ADVENTURE), 10);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.teleport(game.getLobby().getSpawnPoint()), 2);

        Team to;

        int size1 = getGame().getTeam1().getMembers().size();
        int size2 = getGame().getTeam2().getMembers().size();

        if (size2 < size1) {
            to = getGame().getTeam2();
        } else {
            to = getGame().getTeam1();
        }

        // Premium version
        if (p.hasPermission("mw.change")) {
            p.getInventory().setItem(0, VersionUtil.getGlassPlane(getGame().getTeam1()));
            p.getInventory().setItem(8, VersionUtil.getGlassPlane(getGame().getTeam2()));
        }

        if (game.getLobby().getMapChooseProcedure() == MapChooseProcedure.MAPVOTING && game.getArena() == null) {
            p.getInventory().setItem(4, new OrcItem(Material.NETHER_STAR, "§3Vote Map").getItemStack());
        }

        // Adds the player to the new team.
        to.addMember(mw);

        p.sendMessage(MessageConfig.getMessage("team_assigned").replace("%team%", to.getFullname()));

        String name = p.getName();
        String players = "" + game.getPlayers().values().size();
        String maxPlayers = "" + game.getLobby().getMaxSize();
        String message = MessageConfig.getMessage("lobby_joined").replace("%max_players%", maxPlayers).replace("%players%", players).replace("%player%", name);
        game.broadcast(message);

        MissileWars.getInstance().getSignRepository().getSigns(game).forEach(MWSign::update);
    }

    @EventHandler
    public void on(EntityDamageEvent e) {
        if (isInLobbyArea(e.getEntity().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerRespawnEvent e) {
        if (isInLobbyArea(e.getPlayer().getLocation())) {
            e.setRespawnLocation(getGame().getLobby().getSpawnPoint());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        if (isInLobbyArea(p.getLocation()))
            if (p.getGameMode() != GameMode.CREATIVE && !p.isOp())
                e.setCancelled(true);
    }
}
