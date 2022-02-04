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

package de.butzlabben.missilewars.wrapper.game;

import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameState;
import de.butzlabben.missilewars.util.version.BlockSetterProvider;
import de.butzlabben.missilewars.util.version.VersionUtil;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * @author Butzlabben
 * @since 14.01.2018
 */
public class RespawnGoldBlock implements Listener {

    private final Player player;
    private final Game game;
    private final Map<Location, Map.Entry<Material, ?>> map = new HashMap<>();
    private int duration;
    private final boolean messageOnlyOnStart;
    private int task;

    public RespawnGoldBlock(Player p, int duration, boolean messageOnlyOnStart, Game game) {
        this.player = p;
        this.duration = duration;
        this.messageOnlyOnStart = messageOnlyOnStart;
        this.game = game;

        Bukkit.getPluginManager().registerEvents(this, MissileWars.getInstance());
        activate();
    }

    private void activate() {

        if (messageOnlyOnStart) {
            sendFallProtectionMessage();
        }

        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(MissileWars.getInstance(), () -> {
            if (duration == 0) {
                stop();
                return;
            }

            if (!messageOnlyOnStart) {
                sendFallProtectionMessage();
            }

            if (player.getGameMode() != GameMode.SURVIVAL) {
                stop();
                return;
            }
            if (game.getState() != GameState.INGAME) {
                stop();
                return;
            }
            for (Location loc : map.keySet()) {
                loc.getBlock().setType(map.get(loc).getKey());
                BlockSetterProvider.getBlockDataSetter().setData(loc.getBlock(), map.get(loc).getValue());
            }
            map.clear();
            Location curr = player.getLocation().clone();
            curr.setY(curr.getY() - 1.0D);
            Block b = curr.getBlock();
            setBlock(b);
            curr = curr.clone().add(-1.0D, 0.0D, 0.0D);
            b = curr.getBlock();
            setBlock(b);
            curr = curr.clone().add(2.0D, 0.0D, 0.0D);
            b = curr.getBlock();
            setBlock(b);
            curr = curr.clone().add(-1.0D, 0.0D, -1.0D);
            b = curr.getBlock();
            setBlock(b);
            curr = curr.clone().add(0.0D, 0.0D, 2.0D);
            b = curr.getBlock();
            setBlock(b);
            --duration;
        }, 0L, 1L);
    }

    private void setBlock(Block b) {
        if ((b.getType() != Material.GOLD_BLOCK) && (b.getType() == Material.AIR)) {
            Object data = b.getData();
            if (VersionUtil.getVersion() >= 13)
                data = b.getBlockData();
            map.put(b.getLocation(), new AbstractMap.SimpleEntry<>(b.getType(), data));
            b.setType(Material.GOLD_BLOCK);
        }
    }

    public void stop() {
        for (Location loc : map.keySet()) {
            loc.getBlock().setType(map.get(loc).getKey());
            BlockSetterProvider.getBlockDataSetter().setData(loc.getBlock(), map.get(loc).getValue());
        }
        map.clear();
        player.sendMessage(MessageConfig.getMessage("fall_protection_inactive"));
        Bukkit.getScheduler().cancelTask(task);
        HandlerList.unregisterAll(this);
    }

    /**
     * This methode send the fall protection message to the player.
     * The message include the remaining time until the fall protection is ending.
     */
    private void sendFallProtectionMessage() {
        double seconds = (double) duration / 20;
        if ((seconds == Math.floor(seconds)) && !Double.isInfinite(seconds)) {
            player.sendMessage(MessageConfig.getMessage("fall_protection").replace("%seconds%", "" + (int) seconds));
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (p == player && (map.size() != 0) && (p.isSneaking())) {
            for (Location loc : map.keySet()) {
                loc.getBlock().setType(map.get(loc).getKey());
                BlockSetterProvider.getBlockDataSetter().setData(loc.getBlock(), map.get(loc).getValue());
            }
            map.clear();
            Bukkit.getScheduler().cancelTask(task);
            HandlerList.unregisterAll(this);
            p.sendMessage(MessageConfig.getMessage("fall_protection_deactivated"));
        }
    }
}
