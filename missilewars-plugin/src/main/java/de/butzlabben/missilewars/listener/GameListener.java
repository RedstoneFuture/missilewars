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
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.arena.FallProtectionConfiguration;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaLeaveEvent;
import de.butzlabben.missilewars.wrapper.game.RespawnGoldBlock;
import de.butzlabben.missilewars.wrapper.game.Shield;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.geometry.Plane;
import de.butzlabben.missilewars.wrapper.missile.Missile;
import de.butzlabben.missilewars.wrapper.missile.MissileFacing;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Objects;

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
    public void onMove(PlayerMoveEvent e) {
        if (!isInGameWorld(e.getTo())) return;

        Player p = e.getPlayer();
        if ((e.getTo().getBlockY() >= getGame().getArena().getMaxHeight()) && (p.getGameMode() == GameMode.SURVIVAL)) {
            p.teleport(e.getFrom());
            p.sendMessage(MessageConfig.getMessage("not_higher"));
        } else if ((e.getTo().getBlockY() <= getGame().getArena().getDeathHeight()) && (p.getGameMode() == GameMode.SURVIVAL)) {
            p.setLastDamageCause(new EntityDamageEvent(p, EntityDamageEvent.DamageCause.FALL, 20));
            p.damage(20.0D);
        }
        if (!getGame().isInGameArea(e.getTo())) {
            e.setCancelled(true);
            Vector addTo = e.getFrom().toVector().subtract(e.getTo().toVector()).multiply(3);
            addTo.setY(0);
            p.teleport(e.getFrom().add(addTo));
            p.sendMessage(MessageConfig.getMessage("arena_leave"));
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!isInGameWorld(e.getLocation())) return;

        Game game = getGame();
        if (e.getEntity().getType() == EntityType.FIREBALL && !game.getArena().getFireballConfiguration().isDestroysPortal())
            e.blockList().removeIf(b -> b.getType() == VersionUtil.getPortal());
    }

    @EventHandler
    public void on(BlockPhysicsEvent event) {
        Location location = event.getBlock().getLocation();
        if (!isInGameWorld(location)) return;
        if (event.getChangedType() != VersionUtil.getPortal()) return;
        Game game = getGame();
        if (game.getArena().getPlane1().distance(location.toVector()) > game.getArena().getPlane2().distance(location.toVector())) {
            game.getTeam1().win();
            game.getTeam2().lose();
        } else {
            game.getTeam2().win();
            game.getTeam1().lose();
        }
        game.setDraw(false);
        game.stopGame();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!isInGameWorld(e.getPlayer().getLocation())) return;
        if (e.getItem() == null) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
        Game game = getGame();

        // missile spawn with using of a missile spawn egg
        if (VersionUtil.isMonsterEgg(itemStack.getType())) {
            e.setCancelled(true);

            // Can missiles only be spawned if the item interaction was performed on a block (no air)?
            boolean isOnlyBlockPlaceable = game.getArena().getMissileConfiguration().isOnlyBlockPlaceable();
            if (isOnlyBlockPlaceable) {
                if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            }

            // Are missiles only allowed to spawn inside the arena, between the two arena spawn points?
            boolean isOnlyBetweenSpawnPlaceable = game.getArena().getMissileConfiguration().isOnlyBetweenSpawnPlaceable();
            if (isOnlyBetweenSpawnPlaceable) {
                if (!isInBetween(player.getLocation().toVector(), getGame().getArena().getPlane1(), getGame().getArena().getPlane2())) {
                    player.sendMessage(MessageConfig.getMessage("missile_place_deny"));
                    return;
                }
            }

            Missile missile = game.getArena().getMissileConfiguration().getMissileFromName(itemStack.getItemMeta().getDisplayName());
            if (missile == null) {
                player.sendMessage(MessageConfig.getMessage("invalid_missile"));
                return;
            }
            itemStack.setAmount(itemStack.getAmount() - 1);
            player.setItemInHand(itemStack);
            missile.paste(player, MissileFacing.getFacingPlayer(player, game.getArena().getMissileConfiguration()), getGame());

            return;
        }

        // shield spawn with using of a missile spawn egg
        if (itemStack.getType() == VersionUtil.getFireball()) {
            int amount = e.getItem().getAmount();
            e.getItem().setAmount(amount - 1);

            if (amount == 1 && VersionUtil.getVersion() == 8) {
                player.getInventory().remove(VersionUtil.getFireball());
            }

            Fireball fb = player.launchProjectile(Fireball.class);
            fb.setVelocity(player.getLocation().getDirection().multiply(2.5D));
            VersionUtil.playFireball(player, fb.getLocation());
            fb.setYield(3F);
            fb.setIsIncendiary(true);
            fb.setBounce(false);

            return;
        }
    }

    @EventHandler
    public void onJoin(PlayerArenaJoinEvent e) {
        Game game = e.getGame();
        if (game != getGame()) return;

        Player p = e.getPlayer();
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
            to.updateIntervals(game.getArena().getInterval(to.getMembers().size()));
            game.startForPlayer(p);
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        if (!isInGameWorld(e.getEntity().getLocation())) return;

        Game game = getGame();
        if (e.getEntity() instanceof Snowball) {
            Snowball ball = (Snowball) e.getEntity();
            if (ball.getShooter() instanceof Player) {
                Shield shield = new Shield((Player) ball.getShooter(), game.getArena().getShieldConfiguration());
                shield.onThrow(e);
            }
        }
    }

    @EventHandler
    public void onDmg(EntityDamageByEntityEvent e) {
        if (!isInGameWorld(e.getEntity().getLocation())) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player p = (Player) e.getEntity();
        if (e.getDamager() instanceof Projectile) {
            Projectile pj = (Projectile) e.getDamager();
            Player shooter = (Player) pj.getShooter();
            if (Objects.requireNonNull(getGame().getPlayer(shooter)).getTeam() == Objects.requireNonNull(getGame().getPlayer(p)).getTeam()) {
                shooter.sendMessage(MessageConfig.getMessage("hurt_teammates"));
                e.setCancelled(true);
            }
            return;
        }
        if (e.getDamager() instanceof Player) {
            Player d = (Player) e.getDamager();
            if (Objects.requireNonNull(getGame().getPlayer(d)).getTeam() == Objects.requireNonNull(getGame().getPlayer(p)).getTeam()) {
                d.sendMessage(MessageConfig.getMessage("hurt_teammates"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent e) {
        if (!isInGameWorld(e.getPlayer().getLocation())) return;

        Game game = getGame();
        Player player = e.getPlayer();

        Team t = Objects.requireNonNull(getGame().getPlayer(player)).getTeam();
        if (t != null) {
            e.setRespawnLocation(t.getSpawn());
            FallProtectionConfiguration fallProtection = getGame().getArena().getFallProtection();
            if (fallProtection.isEnabled())
                new RespawnGoldBlock(player, fallProtection.getDuration(), fallProtection.isMessageOnlyOnStart(), getGame());
        } else {
            e.setRespawnLocation(getGame().getArena().getSpectatorSpawn());
        }

        game.sendGameItems(player, true);
        game.setPlayerAttributes(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        // check if the player is in a game world
        if (!isInGameWorld(e.getEntity().getLocation())) return;

        Game game = getGame();
        Player p = e.getEntity();

        e.setDeathMessage(null);
        // delete vanilla death message
        String deathBroadcastMessage = null;

        MWPlayer player = getGame().getPlayer(p);
        assert player != null;

        if (game.getArena().isAutoRespawn()) Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> p.spigot().respawn(), 20L);

        // spectator respawn for people there are not in a team
        if (player.getTeam() == null) {
            p.setHealth(p.getMaxHealth());
            p.teleport(getGame().getArena().getSpectatorSpawn());
            return;
        }

        // check the death cause for choice the death message
        if (p.getLastDamageCause() != null) {

            EntityDamageEvent.DamageCause damageCause = p.getLastDamageCause().getCause();

            if (damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                deathBroadcastMessage = MessageConfig.getNativeMessage("died_explosion").replace("%player%", p.getDisplayName());
            } else {
                deathBroadcastMessage = MessageConfig.getNativeMessage("died").replace("%player%", p.getDisplayName());
            }
        }

        getGame().broadcast(deathBroadcastMessage);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeave(PlayerArenaLeaveEvent e) {
        Game game = e.getGame();
        if (game != getGame()) return;

        MWPlayer player = getGame().getPlayer(e.getPlayer());
        if (player == null) return;
        BukkitTask task = game.getPlayerTasks().get(player.getUUID());
        if (task != null) task.cancel();

        Team team = player.getTeam();
        if (team != null) {
            getGame().broadcast(
                    MessageConfig.getMessage("player_left").replace("%player%", e.getPlayer().getDisplayName()));
            team.removeMember(getGame().getPlayer(e.getPlayer()));

            int teamSize = team.getMembers().size();
            if (teamSize == 0) {
                Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> {
                    getGame().draw(false);
                    team.lose();
                    team.getEnemyTeam().win();
                    getGame().stopGame();
                });
                getGame().broadcast(MessageConfig.getMessage("team_offline").replace("%team%", team.getFullname()));
            } else {
                team.updateIntervals(game.getArena().getInterval(team.getMembers().size()));
            }
        }
    }

    // TODO - Analyse, Ingame Check and Crop Cancel Analytic
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player p = (Player) event.getWhoClicked();
        if (!isInGameWorld(p.getLocation())) return;

        if (p.getGameMode() == GameMode.CREATIVE || p.isOp()) return;
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        event.setCancelled(true);
    }

    private boolean isInBetween(Vector point, Plane plane1, Plane plane2) {
        double distanceBetween = plane1.distanceSquared(plane2.getSupport());
        double distance1 = plane1.distanceSquared(point);
        double distance2 = plane2.distanceSquared(point);
        return distanceBetween > distance1 + distance2;
    }
}
