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
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.configuration.arena.FallProtectionConfiguration;
import de.butzlabben.missilewars.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.JoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.RejoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.game.misc.RespawnGoldBlock;
import de.butzlabben.missilewars.game.schematics.objects.Missile;
import de.butzlabben.missilewars.listener.ShieldListener;
import de.butzlabben.missilewars.menus.inventory.TeamSelectionMenu;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.geometry.Geometry;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.inventory.InventoryOpenEvent;
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

        event.blockList().removeIf(b -> b.getType() == Material.NETHER_PORTAL);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!isInGameWorld(event.getBlock().getLocation())) return;

        if (event.getChangedType() != Material.NETHER_PORTAL) return;

        Location location = event.getBlock().getLocation();

        Team team1 = getGame().getTeamManager().getTeam1();
        Team team2 = getGame().getTeamManager().getTeam2();

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
        if (Missile.isSpawnEgg(itemStack.getType())) {
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
        if (itemStack.getType() == Material.FIRE_CHARGE) {

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
        ShieldListener shieldListener = new ShieldListener(shooter, getGame());
        shieldListener.onThrow(event);
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
            shooter.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_HURT_TEAMMATES));
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
            getGame().getEquipmentManager().sendGameItems(player, true);
            getGame().setPlayerAttributes(player);
            getGame().getPlayer(player).getPlayerEquipmentRandomizer().resetPlayerInterval();

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
                deathBroadcast = Messages.getMessage(true, Messages.MessageEnum.DIED_EXPLOSION).replace("%player%", player.getDisplayName());
            } else {
                deathBroadcast = Messages.getMessage(true, Messages.MessageEnum.DIED_NORMAL).replace("%player%", player.getDisplayName());
            }

            getGame().broadcast(deathBroadcast);
        }

        event.setDeathMessage(null);
        if (getGame().getArena().isAutoRespawn()) getGame().autoRespawnPlayer(player);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!isInGameWorld(player.getLocation())) return;

        // handling of MW inventories:
        if (event.getView().getTitle().equals(TeamSelectionMenu.getTitle())) return;
        
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.getGameMode() == GameMode.SPECTATOR) event.setCancelled(true);

        Inventory clickedInventory = event.getInventory();
        if (clickedInventory.getType() != InventoryType.PLAYER) event.setCancelled(true);
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

        // Putting the items inside is not perfectly locked. But it is a second protection.
        // More checks are possible: https://www.spigotmc.org/threads/531737

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!isInGameWorld(player.getLocation())) return;

        // handling of MW inventories:
        if (event.getView().getTitle().equals(TeamSelectionMenu.getTitle())) {
            if (event.getSlotType() == InventoryType.SlotType.CONTAINER) return;
        }
        
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
            Logger.DEBUG.log("Cancelled 'InventoryClickEvent' event of " + player.getName());
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null) {
            if (clickedInventory.getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                Logger.DEBUG.log("Cancelled 'InventoryClickEvent' event of " + player.getName());
            }
        }

        if ((event.getSlotType() != InventoryType.SlotType.CONTAINER) &&
                (event.getSlotType() != InventoryType.SlotType.QUICKBAR)) {
            event.setCancelled(true);
            Logger.DEBUG.log("Cancelled 'InventoryClickEvent' event of " + player.getName());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isInGameWorld(event.getTo())) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        int toY = event.getTo().getBlockY();
        if (toY > getGame().getArena().getMaxMoveHeight()) {
            player.teleport(event.getFrom());
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.ARENA_NOT_HIGHER));
        } else if (toY < getGame().getArena().getDeathHeight()) {
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, 20));
            player.damage(20.0D);
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (!getGame().isInGameArea(event.getTo())) {
            if (to != null) Game.knockbackEffect(player, from, to);
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.ARENA_ARENA_LEAVE));
        }
    }

    @EventHandler
    public void onPlayerArenaJoin(PlayerArenaJoinEvent event) {
        if (!getGame().isIn(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();

        JoinIngameBehavior joinBehavior = getGame().getLobby().getJoinIngameBehavior();
        RejoinIngameBehavior rejoinBehavior = getGame().getLobby().getRejoinIngameBehavior();
        boolean isKnownPlayer = getGame().getGameLeaveManager().isKnownPlayer(player.getUniqueId());
        Team lastTeam = getGame().getGameLeaveManager().getLastTeamOfKnownPlayer(player.getUniqueId());
        
        // A: Forbidden the game join:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.FORBIDDEN) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.FORBIDDEN)) {
            event.getPlayer().sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_NOT_ENTER_ARENA));
            event.setCancelled(true);
            return;
        }
        
        // B: game join in a player-team:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.PLAYER) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.PLAYER) 
                || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.PLAYER)) {
            
            if (!getGame().areTooManyPlayers()) {
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.PLAYER);
                
            } else if (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.PLAYER 
                    && !getGame().areTooManySpectators()) {
                event.getPlayer().sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_PLAYER_TEAM_MAX_REACHED));
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.SPECTATOR);
                
            } else {
                event.getPlayer().sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_MAX_REACHED));
                event.setCancelled(true);
                
            }
            return;
        }
        
        // C: game join in a spectator-team:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.SPECTATOR) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.SPECTATOR) 
                || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.SPECTATOR)) {
            
            if (!getGame().areTooManySpectators()) {
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.SPECTATOR);

            } else if (!getGame().areTooManyPlayers()) {
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.PLAYER);
                
            } else {
                event.getPlayer().sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_SPECTATOR_TEAM_MAX_REACHED));
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
