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

import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.configuration.arena.FallProtectionConfiguration;
import de.butzlabben.missilewars.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.misc.RespawnGoldBlock;
import de.butzlabben.missilewars.game.misc.Shield;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.geometry.Geometry;
import de.butzlabben.missilewars.util.version.VersionUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Butzlabben
 * @since 12.01.2018
 */
public class GameListener extends GameBoundListener {

    public GameListener(Game game) {
        super(game);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!isInGameWorld(event.getLocation())) return;

        if (event.getEntity().getType() != EntityType.FIREBALL) return;
        if (getGame().getArena().getFireballConfiguration().isDestroysPortal()) return;

        event.blockList().removeIf(b -> b.getType() == VersionUtil.getPortal());
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!isInGameWorld(event.getBlock().getLocation())) return;

        if (event.getChangedType() != VersionUtil.getPortal()) return;

        Location location = event.getBlock().getLocation();
        
        Team team1 = getGame().getTeam1();
        Team team2 = getGame().getTeam2();
        
        if (Geometry.isCloser(location, team1.getSpawn(), team2.getSpawn())) {
            team1.setGameResult(GameResult.LOSE);
            team2.setGameResult(GameResult.WIN);
        } else {
            team1.setGameResult(GameResult.WIN);
            team2.setGameResult(GameResult.LOSE);
        }
        
        getGame().sendGameResult();
        getGame().stopGame();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        if (event.getItem() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        // missile spawn with using of a missile spawn egg
        if (VersionUtil.isMonsterEgg(itemStack.getType())) {
            event.setCancelled(true);

            // Can missiles only be spawned if the item interaction was performed on a block (no air)?
            boolean isOnlyBlockPlaceable = getGame().getArena().getMissileConfiguration().isOnlyBlockPlaceable();
            if (isOnlyBlockPlaceable) {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            }

            getGame().spawnMissile(player, itemStack);
            return;
        }

        // shield spawn with using of a shield spawn egg
        if (itemStack.getType() == VersionUtil.getFireball()) {

            getGame().spawnFireball(player, itemStack);
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (!isInGameWorld(event.getEntity().getLocation())) return;

        if (!(event.getEntity() instanceof Snowball)) return;

        Snowball snowball = (Snowball) event.getEntity();
        if (!(snowball.getShooter() instanceof Player)) return;

        Player shooter = (Player) snowball.getShooter();
        Shield shield = new Shield(shooter, getGame().getArena().getShieldConfiguration());
        shield.onThrow(event);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!isInGameWorld(player.getLocation())) return;

        Player shooter;
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (!(projectile.getShooter() instanceof Player)) return;

            shooter = (Player) projectile.getShooter();

        } else if (event.getDamager() instanceof Player) {

            shooter = (Player) event.getDamager();

        } else {
            return;
        }

        Team team = getGame().getPlayer(shooter).getTeam();
        if (team == null) return;

        // same player
        if (shooter == player) return;

        // same team
        if (team == getGame().getPlayer(player).getTeam()) {
            shooter.sendMessage(Messages.getMessage("hurt_teammates"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        Team team = getGame().getPlayer(player).getTeam();

        if (team != null) {
            event.setRespawnLocation(team.getSpawn());
            getGame().sendGameItems(player, true);
            getGame().setPlayerAttributes(player);
            getGame().getPlayer(player).getRandomGameEquipment().resetPlayerInterval();

            FallProtectionConfiguration fallProtection = getGame().getArena().getFallProtection();
            if (fallProtection.isEnabled()) {
                new RespawnGoldBlock(player, fallProtection.getDuration(), fallProtection.isMessageOnlyOnStart(), getGame());
            }
        } else {
            event.setRespawnLocation(getGame().getArena().getSpectatorSpawn());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!isInGameWorld(event.getEntity().getLocation())) return;

        Player player = event.getEntity();
        Team team = getGame().getPlayer(player).getTeam();

        // check the death cause for choice the death message
        if (team != null) {

            if (player.getLastDamageCause() == null) return;

            String deathBroadcast;
            EntityDamageEvent.DamageCause damageCause = player.getLastDamageCause().getCause();

            if (damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                deathBroadcast = Messages.getNativeMessage("died_explosion").replace("%player%", player.getDisplayName());
            } else {
                deathBroadcast = Messages.getNativeMessage("died").replace("%player%", player.getDisplayName());
            }

            getGame().broadcast(deathBroadcast);
        }

        event.setDeathMessage(null);
        if (getGame().getArena().isAutoRespawn()) getGame().autoRespawnPlayer(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!isInGameWorld(player.getLocation())) return;

        if (player.getGameMode() == GameMode.CREATIVE) return;

        if (player.getGameMode() == GameMode.SPECTATOR) event.setCancelled(true);

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null) {
            if (clickedInventory.getType() != InventoryType.PLAYER) event.setCancelled(true);
        }

        if ((event.getSlotType() != InventoryType.SlotType.CONTAINER) &&
                (event.getSlotType() != InventoryType.SlotType.QUICKBAR)) event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isInGameWorld(event.getTo())) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        int toY = event.getTo().getBlockY();
        if (toY > getGame().getArena().getMaxHeight()) {
            player.teleport(event.getFrom());
            player.sendMessage(Messages.getMessage("not_higher"));
        } else if (toY < getGame().getArena().getDeathHeight()) {
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, 20));
            player.damage(20.0D);
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (!getGame().isInGameArea(event.getTo())) {
            if (to != null) Game.knockbackEffect(player, from, to);
            player.sendMessage(Messages.getMessage("arena_leave"));
        }
    }

    @EventHandler
    public void onPlayerArenaJoin(PlayerArenaJoinEvent event) {
        if (!getGame().isIn(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();

        if ((!getGame().getLobby().isJoinOngoingGame()) || (getGame().isPlayersMax())) {
            if (getGame().isSpectatorsMax()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.getMessage("not_enter_arena"));
                return;
            }
            getGame().playerJoinInGame(player, true);
            return;
        }

        getGame().playerJoinInGame(player, false);
    }

    @EventHandler
    public void onPlayerArenaLeave(PlayerArenaLeaveEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        MWPlayer mwPlayer = event.getGame().getPlayer(player);

        if (mwPlayer != null) getGame().playerLeaveFromGame(mwPlayer);
    }
}
