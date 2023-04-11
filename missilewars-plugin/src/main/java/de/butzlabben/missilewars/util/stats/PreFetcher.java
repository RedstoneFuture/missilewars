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
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.game.stats.StatsFetcher;
import de.butzlabben.missilewars.inventory.OrcItem;
import de.butzlabben.missilewars.inventory.pages.InventoryPage;
import de.butzlabben.missilewars.inventory.pages.PageGUICreator;
import de.butzlabben.missilewars.util.version.VersionUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PreFetcher {

    @Getter
    private static PrePlayerFetchRunnable runnable;

    private PreFetcher() {
    }

    public static synchronized PrePlayerFetchRunnable preFetchPlayers(StatsFetcher fetcher) {
        if (runnable != null) return runnable;
        runnable = new PrePlayerFetchRunnable(fetcher);
        Thread thread = new Thread(runnable);
        thread.start();
        return runnable;
    }

    @RequiredArgsConstructor
    public static class PrePlayerFetchRunnable implements Runnable {

        private static final int MAX_SKIN_FETCHES = 10;
        private final StatsFetcher fetcher;
        private boolean shouldStop = false;

        @Override
        public void run() {
            if (!Config.isFightStatsEnabled())
                return;
            if (!Config.isContactAuth())
                return;
            List<UUID> uuids = fetcher.getPlayers();
            Map<UUID, String> names = new HashMap<>();
            Collections.reverse(uuids);
            Logger.DEBUG.log("Prefetching " + uuids.size() + " player names");
            for (UUID uuid : uuids) {
                if (shouldStop)
                    break;
                try {
                    GameProfile profile = GameProfileBuilder.fetch(uuid);
                    names.put(uuid, profile.getName());
                } catch (Exception e) {
                    Logger.WARN.log("Could not prefetch player " + uuid.toString() + ". Aborting. Reason: " + e.getMessage());
                    return;
                }
            }

            if (!Config.isShowRealSkins()) return;
            try {
                Thread.sleep(6 * 10 * 1000);
            } catch (InterruptedException ignored) {
            }

            if (shouldStop) return;
            Logger.DEBUG.log("Prefetching " + uuids.size() + " player skins");
            PageGUICreator<String> creator = getPreFetchCreator(names.values());
            creator.show(null);
            if (creator.getInvPages() == null) return;
            int i = 0;
            for (InventoryPage page : creator.getInvPages()) {
                if (shouldStop) return;
                try {
                    Thread.sleep(20 * 1000);
                } catch (InterruptedException ignored) {
                }
                if (shouldStop) return;
                Logger.DEBUG.log("Prefetching page " + i);
                try {
                    page.getInventory();
                } catch (Exception ignored) {
                }
                i++;
            }
            Logger.DEBUG.log("Players fully loaded");
        }

        public void stop() {
            shouldStop = true;
        }

        private PageGUICreator<String> getPreFetchCreator(Collection<String> names) {
            return new PageGUICreator<>("§ePlayer statistics", names, (item) -> {
                ItemStack itemStack = new ItemStack(VersionUtil.getPlayerSkullMaterial());
                SkullMeta sm = (SkullMeta) itemStack.getItemMeta();
                //noinspection deprecation
                sm.setOwner(item);
                sm.setDisplayName(item);
                itemStack.setItemMeta(sm);
                return new OrcItem(itemStack);
            });
        }
    }
}
