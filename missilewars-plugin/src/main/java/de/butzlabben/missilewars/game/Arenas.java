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

package de.butzlabben.missilewars.game;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.arena.ArenaConfig;
import de.butzlabben.missilewars.initialization.FileManager;
import de.butzlabben.missilewars.util.serialization.Serializer;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Arenas {

    @Getter
    private static final Map<String, ArenaConfig> ARENAS = new HashMap<>();

    public static void load() {
        ARENAS.clear();

        File folder = new File(Config.getArenasFolder());

        // Creates the folder "/arena", if not existing
        folder.mkdirs();

        // Get all arena files or create a new one
        File[] arenaFiles = folder.listFiles();
        if (arenaFiles.length == 0) {
            File defaultArena = new File(folder, "arena0.yml");
            try {
                defaultArena.createNewFile();
                Serializer.serialize(defaultArena, new ArenaConfig());
            } catch (IOException exception) {
                Logger.ERROR.log("Could not create default arena config");
                Logger.ERROR.log("As there are no arenas present, the plugin is shutting down");
                exception.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(MissileWars.getInstance());
                return;
            }
            arenaFiles = new File[] {defaultArena};
        }

        for (File config : arenaFiles) {
            if (!config.getName().endsWith(".yml") && !config.getName().endsWith(".yaml")) continue;
            try {
                ArenaConfig arenaConfig = Serializer.deserialize(config, ArenaConfig.class);
                arenaConfig.setFile(config);
                if (existsArena(arenaConfig.getName())) {
                    Logger.WARN.log("There are several arenas configured with the name \"" + arenaConfig.getName() + "\". Arenas must have a unique name");
                    continue;
                }
                FileManager.saveDefaultResource(Config.getArenasFolder() + File.separator + "default_map", 
                        "MissileWars-Arena.zip", MissileWars.getInstance());
                arenaConfig.updateConfig();
                ARENAS.put(arenaConfig.getName(), arenaConfig);
            } catch (IOException exception) {
                Logger.ERROR.log("Could not load config for arena " + config.getName());
                exception.printStackTrace();
            }
        }
    }

    public static ArenaConfig getFromName(String arenaName) {
        if (ARENAS.containsKey(arenaName)) return ARENAS.get(arenaName);
        return null;
    }

    public static boolean existsArena(String arenaName) {
        return ARENAS.containsKey(arenaName);
    }
}
