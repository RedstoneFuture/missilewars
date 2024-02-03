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
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.util.SetupUtil;
import de.butzlabben.missilewars.util.serialization.Serializer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;

public class Arenas {

    @Getter
    private static final Map<String, Arena> ARENAS = new HashMap<>();

    public static void load() {
        ARENAS.clear();

        File folder = new File(Config.getArenasFolder());

        // Creates the folder "/arena", if not existing
        folder.mkdirs();

        // Get all arena files or create a new one
        File[] files = folder.listFiles();
        if (files.length == 0) {
            File defaultArena = new File(folder, "arena0.yml");
            try {
                defaultArena.createNewFile();
                Serializer.serialize(defaultArena, new Arena());
            } catch (IOException exception) {
                Logger.ERROR.log("Could not create default arena config");
                Logger.ERROR.log("As there are no arenas present, the plugin is shutting down");
                exception.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(MissileWars.getInstance());
                return;
            }

            // Also unpack additional arenas
            try {
                SetupUtil.copyAndUnzip("Dam-Arena.zip", folder.getAbsolutePath());
            } catch (IOException e) {
                Logger.ERROR.log("Could not extract Dam Arena");
                e.printStackTrace();
            }
        }

        files = folder.listFiles();
        for (File config : files) {
            if (!config.getName().endsWith(".yml") && !config.getName().endsWith(".yaml")) continue;
            try {
                Arena arena = Serializer.deserialize(config, Arena.class);
                arena.setFile(config);
                if (existsArena(arena.getName())) {
                    Logger.WARN.log("There are several arenas configured with the name \"" + arena.getName() + "\". Arenas must have a unique name");
                    continue;
                }
                SetupUtil.checkMap(arena.getTemplateWorld());
                arena.updateConfig();
                ARENAS.put(arena.getName(), arena);
            } catch (IOException exception) {
                Logger.ERROR.log("Could not load config for arena " + config.getName());
                exception.printStackTrace();
            }
        }
    }

    public static Arena getFromName(String arenaName) {
        if (ARENAS.containsKey(arenaName)) return ARENAS.get(arenaName);
        return null;
    }

    public static boolean existsArena(String arenaName) {
        return ARENAS.containsKey(arenaName);
    }
}
