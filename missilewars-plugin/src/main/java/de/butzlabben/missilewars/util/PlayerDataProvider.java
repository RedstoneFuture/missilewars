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

package de.butzlabben.missilewars.util;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class PlayerDataProvider {

    private static final PlayerDataProvider instance = new PlayerDataProvider();
    private final HashMap<UUID, PlayerData> data = new HashMap<>();
    private final File playerDataDirectory;

    private PlayerDataProvider() {
        playerDataDirectory = new File(MissileWars.getInstance().getDataFolder(), "data");
        playerDataDirectory.mkdirs();
    }

    public static PlayerDataProvider getInstance() {
        return instance;
    }

    public void storeInventory(Player player) {
        UUID uuid = player.getUniqueId();
        if (data.containsKey(uuid)) return;

        File file = getPathFromUUID(uuid);
        if (file.exists() && file.isFile()) return;

        PlayerData playerData = new PlayerData(player);
        data.put(uuid, playerData);
        playerData.saveToFile(file.getPath());
    }

    public void loadInventory(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = null;
        File file = getPathFromUUID(player.getUniqueId());

        // getting data
        if (data.containsKey(uuid)) {
            playerData = data.get(uuid);
        } else if (file.exists()) {
            playerData = PlayerData.loadFromFile(file);
        }

        // applying data
        if (playerData != null) {
            playerData.apply(player);
        } else {
            applyDefaultValues(player);
            Logger.WARN.log("Could not find inventory for " + uuid);
            return;
        }

        // deleting old data
        if (data.containsKey(uuid)) data.remove(uuid);
        if (file.exists()) file.delete();
    }

    public File getPathFromUUID(UUID uuid) {
        return new File(playerDataDirectory, uuid.toString() + ".yml");
    }

    public static void applyDefaultValues(Player player) {
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
    }
}
