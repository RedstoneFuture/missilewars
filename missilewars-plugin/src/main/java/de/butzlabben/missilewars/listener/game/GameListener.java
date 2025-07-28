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
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.configuration.arena.modules.FallProtectionConfig;
import de.butzlabben.missilewars.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.JoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.RejoinIngameBehavior;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.game.misc.RespawnGoldBlock;
import de.butzlabben.missilewars.game.misc.TeamSpawnProtection;
import de.butzlabben.missilewars.game.schematics.objects.Missile;
import de.butzlabben.missilewars.listener.ShieldListener;
import de.butzlabben.missilewars.menus.inventory.TeamSelectionMenu;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.version.MaterialHelper;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
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
        if (getGame().getArenaConfig().getFireballConfig().isDestroysPortal()) return;

        event.blockList().removeIf(b -> b.getType() == Material.NETHER_PORTAL);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;
        
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        
        // Interaction Cancelling for some objects:
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (MaterialHelper.isSignMaterial(event.getClickedBlock().getType())) {
                event.setCancelled(true);
                Logger.DEBUG.log("Cancelling of interaction with '#ALL_SIGNS' (Gamemode: " + player.getGameMode().name() + ").");
                return;
            }
        }
        
        // Game-Item handling:
        
        if (event.getItem() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack itemStack = event.getItem();

        // missile spawn with using of a missile spawn egg
        if (Missile.isSpawnEgg(itemStack.getType())) {
            event.setCancelled(true);
            
            // prevent spam with the event handling
            if (isInteractDelay(player)) return;
            setInteractDelay(player);

            // Can missiles only be spawned if the item interaction was performed on a block (no air)?
            boolean isOnlyBlockPlaceable = getGame().getArenaConfig().getMissileConfig().isOnlyBlockPlaceable();
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
        ShieldListener shieldListener = new ShieldListener(shooter, getGame(), snowball);
        Bukkit.getPluginManager().registerEvents(shieldListener, MissileWars.getInstance());

        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> {
            HandlerList.unregisterAll(shieldListener);
            
            // Is the snowball-entity dead because of an invalid 'fly_time' of the shield-configuration 
            // or a projectile hit before.
            if (!snowball.isDead()) getGame().spawnShield(shooter, snowball);
            
        }, getGame().getArenaConfig().getShieldConfig().getFlyTime());
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
            shooter.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_HURT_TEAMMATES));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!isInGameWorld(event.getPlayer().getLocation())) return;

        Player player = event.getPlayer();
        Team team = getGame().getPlayer(player).getTeam();

        if (team.getTeamType() == TeamType.PLAYER) {
            TeamSpawnProtection.regenerateSpawn(team);
            
            event.setRespawnLocation(team.getSpawn());
            getGame().getEquipmentManager().sendGameItems(player, true);
            getGame().setPlayerAttributes(player);
            getGame().getPlayer(player).getPlayerEquipmentRandomizer().resetPlayerInterval();

            FallProtectionConfig fallProtection = getGame().getArenaConfig().getFallProtection();
            if (fallProtection.isEnabled()) {
                new RespawnGoldBlock(player, fallProtection.getDuration(), fallProtection.isMessageOnlyOnStart(), getGame());
            }
        } else {
            event.setRespawnLocation(getGame().getArenaConfig().getSpectatorSpawn());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!isInGameWorld(event.getEntity().getLocation())) return;

        Player player = event.getEntity();
        MWPlayer mwPlayer = getGame().getPlayer(player);
        Team team = mwPlayer.getTeam();

        // check the death cause for choice the death message
        if (team.getTeamType() == TeamType.PLAYER) {

            if (player.getLastDamageCause() == null) return;

            String deathBroadcast;
            EntityDamageEvent.DamageCause damageCause = player.getLastDamageCause().getCause();

            if (damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                deathBroadcast = PluginMessages.getMessage(true, PluginMessages.MessageEnum.DIED_EXPLOSION).replace("%player%", player.getDisplayName());
            } else {
                deathBroadcast = PluginMessages.getMessage(true, PluginMessages.MessageEnum.DIED_NORMAL).replace("%player%", player.getDisplayName());
            }

            getGame().broadcast(deathBroadcast);
        }

        event.setDeathMessage(null);
        if (getGame().getArenaConfig().isAutoRespawn()) getGame().autoRespawnPlayer(mwPlayer);
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
        if (toY > getGame().getArenaConfig().getMaxMoveHeight()) {
            player.teleport(event.getFrom());
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.ARENA_NOT_HIGHER));
        } else if (toY < getGame().getArenaConfig().getDeathHeight()) {
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, 20));
            player.damage(20.0D);
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (!getGame().isInGameArea(event.getTo())) {
            if (to != null) Game.knockbackEffect(player, from, to);
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.ARENA_REACHED_BORDER));
        }
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
        
        // B: game join in a player-team:
        if ((!isKnownPlayer && joinBehavior == JoinIngameBehavior.PLAYER) || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.PLAYER) 
                || (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.PLAYER)) {
            
            if (!getGame().areTooManyPlayers()) {
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.PLAYER);
                
            } else if (isKnownPlayer && rejoinBehavior == RejoinIngameBehavior.LAST_TEAM && lastTeam.getTeamType() == TeamType.PLAYER 
                    && !getGame().areTooManySpectators()) {
                event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_PLAYER_TEAM_MAX_REACHED));
                getGame().getGameJoinManager().runPlayerJoin(player, TeamType.SPECTATOR);
                
            } else {
                event.getPlayer().sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_MAX_REACHED));
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
