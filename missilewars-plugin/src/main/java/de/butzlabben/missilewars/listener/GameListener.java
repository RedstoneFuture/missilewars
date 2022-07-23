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
import de.butzlabben.missilewars.game.GameResult;
import de.butzlabben.missilewars.util.PlayerDataProvider;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.arena.FallProtectionConfiguration;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.wrapper.game.RespawnGoldBlock;
import de.butzlabben.missilewars.wrapper.game.Shield;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * @author Butzlabben
 * @since 12.01.2018
 */
@SuppressWarnings("deprecation")
public class GameListener extends GameBoundListener {

    public GameListener(Game game) {
        super(game);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isInGameWorld(event.getTo())) return;

        Player p = event.getPlayer();
        if ((event.getTo().getBlockY() >= getGame().getArena().getMaxHeight()) && (p.getGameMode() == GameMode.SURVIVAL)) {
            p.teleport(event.getFrom());
            p.sendMessage(MessageConfig.getMessage("not_higher"));
        } else if ((event.getTo().getBlockY() <= getGame().getArena().getDeathHeight()) && (p.getGameMode() == GameMode.SURVIVAL)) {
            p.setLastDamageCause(new EntityDamageEvent(p, EntityDamageEvent.DamageCause.FALL, 20));
            p.damage(20.0D);
        }
        if (!getGame().isInGameArea(event.getTo())) {
            event.setCancelled(true);
            Vector addTo = event.getFrom().toVector().subtract(event.getTo().toVector()).multiply(3);
            addTo.setY(0);
            p.teleport(event.getFrom().add(addTo));
            p.sendMessage(MessageConfig.getMessage("arena_leave"));
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!isInGameWorld(event.getLocation())) return;

        if (event.getEntity().getType() != EntityType.FIREBALL) return;
        if (!getGame().getArena().getFireballConfiguration().isDestroysPortal()) return;

        event.blockList().removeIf(b -> b.getType() == VersionUtil.getPortal());
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!isInGameWorld(event.getBlock().getLocation())) return;

        if (event.getChangedType() != VersionUtil.getPortal()) return;

        Location location = event.getBlock().getLocation();

        if (getGame().getArena().getPlane1().distance(location.toVector()) > getGame().getArena().getPlane2().distance(location.toVector())) {
            getGame().getTeam1().setGameResult(GameResult.WIN);
            getGame().getTeam2().setGameResult(GameResult.LOSE);
        } else {
            getGame().getTeam1().setGameResult(GameResult.LOSE);
            getGame().getTeam2().setGameResult(GameResult.WIN);
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
            return;
        }
    }

    @EventHandler
    public void onJoin(PlayerArenaJoinEvent event) {
        Game game = event.getGame();
        if (game != getGame()) return;

        Player p = event.getPlayer();
        MWPlayer mwPlayer = game.addPlayer(p);
        PlayerDataProvider.getInstance().storeInventory(p);
        p.getInventory().clear();

        if (!game.getLobby().isJoinOngoingGame() || game.getPlayers().size() >= game.getLobby().getMaxSize()) {
            p.sendMessage(MessageConfig.getMessage("spectator"));
            Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.teleport(game.getArena().getSpectatorSpawn()), 2);
            Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.setGameMode(GameMode.SPECTATOR), 35);;
            p.setDisplayName("§7" + p.getName() + "§r");
            p.setScoreboard(game.getScoreboard());
        } else {
            Team to;
            int size1 = game.getTeam1().getMembers().size();
            int size2 = game.getTeam2().getMembers().size();
            if (size2 < size1)
                to = getGame().getTeam2();
            else
                to = getGame().getTeam1();

            // Adds the player to the new team.
            to.addMember(mwPlayer);

            p.sendMessage(MessageConfig.getMessage("team_assigned").replace("%team%", to.getFullname()));
            game.startForPlayer(p);
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (!isInGameWorld(event.getEntity().getLocation())) return;

        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();

            if (!(snowball.getShooter() instanceof Player)) return;

            Player shooter = (Player) snowball.getShooter();
            Shield shield = new Shield(shooter, getGame().getArena().getShieldConfiguration());
            shield.onThrow(event);
        }
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

        } else return;

        Team team = getGame().getPlayer(shooter).getTeam();
        if (team == null) return;

        // same player
        if (shooter == player) return;

        // same team
        if (team == getGame().getPlayer(player).getTeam()) {
            shooter.sendMessage(MessageConfig.getMessage("hurt_teammates"));
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

            String deathBroadcastMessage;
            EntityDamageEvent.DamageCause damageCause = player.getLastDamageCause().getCause();

            if (damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                deathBroadcastMessage = MessageConfig.getNativeMessage("died_explosion").replace("%player%", player.getDisplayName());
            } else {
                deathBroadcastMessage = MessageConfig.getNativeMessage("died").replace("%player%", player.getDisplayName());
            }

            getGame().broadcast(deathBroadcastMessage);
        }

        event.setDeathMessage(null);
        if (getGame().getArena().isAutoRespawn()) getGame().autoRespawnPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeave(PlayerArenaLeaveEvent event) {
        Game game = event.getGame();
        if (game != getGame()) return;

        MWPlayer player = getGame().getPlayer(event.getPlayer());
        if (player == null) return;
        BukkitTask task = game.getPlayerTasks().get(player.getUuid());
        if (task != null) task.cancel();

        Team team = player.getTeam();
        if (team != null) {
            getGame().broadcast(
                    MessageConfig.getMessage("player_left").replace("%player%", event.getPlayer().getDisplayName()));
            team.removeMember(getGame().getPlayer(event.getPlayer()));

            int teamSize = team.getMembers().size();
            if (teamSize == 0) {
                Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> {
                    team.getEnemyTeam().setGameResult(GameResult.WIN);
                    team.setGameResult(GameResult.LOSE);
                    game.sendGameResult();
                    getGame().stopGame();
                });
                getGame().broadcast(MessageConfig.getMessage("team_offline").replace("%team%", team.getFullname()));
            }
        }
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
}
