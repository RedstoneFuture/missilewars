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

package de.butzlabben.missilewars.util.stats;

import com.mojang.authlib.GameProfile;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.stats.PlayerStats;
import de.butzlabben.missilewars.game.stats.PlayerStatsComparator;
import de.butzlabben.missilewars.inventory.OrcItem;
import de.butzlabben.missilewars.inventory.pages.PageGUICreator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Getter
public class PlayerGuiFactory {

    private final static int MAX_FETCHES = 9;
    private final static int FETCH_EVERY_ROUND = 3;

    private final List<PlayerStats> stats;
    private final Map<UUID, String> names = new ConcurrentHashMap<>();
    private final int fetchRound = 0;

    public PlayerGuiFactory(List<PlayerStats> stats) {
        this.stats = stats;
        for (PlayerStats stat : stats) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(stat.getUuid());
            if (offlinePlayer.getName() != null) {
                names.put(stat.getUuid(), offlinePlayer.getName());
                stat.setName(offlinePlayer.getName());
            }
            if (GameProfileBuilder.getCache().containsKey(stat.getUuid())) {
                String name = GameProfileBuilder.getCache().get(stat.getUuid()).getProfile().getName();
                names.put(stat.getUuid(), name);
                stat.setName(name);
            }
        }
    }

    public void openWhenReady(Player player) {
        int realSize = stats.size();
        int currentSize = names.size();
        if (realSize > currentSize) {
            if (Config.isContactAuth()) {
                player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.STATS_FETCHING_PLAYERS)
                        .replace("current_size", Integer.toString(currentSize))
                        .replace("real_size", Integer.toString(realSize)));
                ForkJoinPool.commonPool().execute(() -> {
                    List<UUID> missing = getMissingUUIDs();
                    int maxFetches = Math.min(missing.size(), MAX_FETCHES);
                    for (int i = 0; i < maxFetches; i++) {
                        UUID uuid = missing.get(i);
                        try {
                            GameProfile profile = GameProfileBuilder.fetch(uuid);
                            names.put(uuid, profile.getName());
                        } catch (IOException e) {
                            names.put(uuid, "Error getting name");
                            if (!e.getMessage().contains("Could not connect to mojang servers for unknown player"))
                                Logger.WARN.log("Could not fetch name for " + uuid.toString() + ". Reason: " + e.getMessage());
                        }
                    }

                    openWhenReady(player);
                });
                return;
            }
        }
        for (PlayerStats stat : stats) {
            stat.setName(names.get(stat.getUuid()));
            if (stat.getName() == null || stat.getName().equals("")) {
                Logger.WARN.log("Could not find name for: " + stat.getUuid());
            }
        }

        Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> open(player));
    }

    public List<UUID> getMissingUUIDs() {
        return stats.stream().map(PlayerStats::getUuid).filter(uuid -> !names.containsKey(uuid)).collect(Collectors.toList());
    }

    private void open(Player player) {
        List<PlayerStats> stats = new ArrayList<>(this.stats);
        stats.sort(new PlayerStatsComparator());

        PageGUICreator<PlayerStats> creator = new PageGUICreator<>("§ePlayer statistics", stats, (item) -> {
            String name = item.getName();
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) itemStack.getItemMeta();
            if (Config.isShowRealSkins()) {
                //noinspection deprecation
                sm.setOwner(name);
            } else {
                sm.setOwningPlayer(Bukkit.getOfflinePlayer(item.getUuid()));
            }
            List<String> lore = Arrays.asList("§7Games played: §e" + item.getGamesPlayed(),
                    "§7W/L: §e" + StatsUtil.formatDouble(item.getWinToLoseRatio()),
                    "§7Favourite team: §e" + StatsUtil.formatDouble(item.getTeamRatio()));
            sm.setLore(lore);
            sm.setDisplayName("§7" + name);
            itemStack.setItemMeta(sm);
            return new OrcItem(itemStack);
        });

        Map<Integer, OrcItem> extraButtons = new HashMap<>();

        creator.show(player);
    }
}
