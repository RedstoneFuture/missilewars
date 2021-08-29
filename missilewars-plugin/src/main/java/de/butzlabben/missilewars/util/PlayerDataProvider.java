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
import de.butzlabben.missilewars.wrapper.player.PlayerData;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PlayerDataProvider {

    private static final PlayerDataProvider ourInstance = new PlayerDataProvider();

    private final String path = "data";
    private final HashMap<UUID, PlayerData> data = new HashMap<>();

    private PlayerDataProvider() {
    }

    public static PlayerDataProvider getInstance() {
        return ourInstance;
    }

    public void storeInventory(Player player) {
        if (hasData(player.getUniqueId()))
            return;
        PlayerData playerData = new PlayerData(player);
        data.put(player.getUniqueId(), playerData);
        playerData.saveToFile(getPathFromUUID(player.getUniqueId()).getPath());
    }

    public void loadInventory(Player player) {
        PlayerData data;
        File file = getPathFromUUID(player.getUniqueId());
        if (this.data.containsKey(player.getUniqueId())) {
            data = this.data.remove(player.getUniqueId());
        } else {
            if (file.exists()) {
                data = PlayerData.loadFromFile(file);
            } else {
                player.getInventory().clear();
                player.setLevel(0);
                Logger.WARN.log("Could not find inventory for " + player.getUniqueId());
                return;
            }
        }
        if (file.exists())
            file.delete();
        if (data != null) {
            data.apply(player);
        }
    }

    public File getPathFromUUID(UUID uuid) {
        File file = new File(MissileWars.getInstance().getDataFolder(), path);
        file.mkdirs();
        return new File(file, uuid.toString() + ".yml");
    }

    public boolean hasData(UUID uuid) {
        if (data.containsKey(uuid))
            return true;
        File file = getPathFromUUID(uuid);
        return file.exists() && file.isFile();

    }
}
