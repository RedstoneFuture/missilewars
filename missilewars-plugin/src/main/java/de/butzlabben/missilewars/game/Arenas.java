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
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Arenas {

    @Getter private static final List<Arena> arenas = new ArrayList<>();

    public static void load() {
        arenas.clear();

        File folder = new File(Config.getArenaFolder());

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
            files = new File[]{defaultArena};
        }

        for (File config : files) {
            if (!config.getName().endsWith(".yml") && !config.getName().endsWith(".yaml")) continue;
            try {
                Arena arena = Serializer.deserialize(config, Arena.class);
                arena.setFile(config);
                if (getFromName(arena.getName()).isPresent()) {
                    Logger.WARN.log("There are several arenas configured with the name \"" + arena.getName() + "\". Arenas must have a unique name");
                    continue;
                }
                SetupUtil.checkMap(arena.getTemplateWorld());
                // Save for possible new values
                Serializer.serialize(config, arena);
                arenas.add(arena);
            } catch (IOException exception) {
                Logger.ERROR.log("Could not load config for arena " + config.getName());
                exception.printStackTrace();
            }
        }
    }

    public static Optional<Arena> getFromName(String name) {
        return arenas.stream().filter(arena -> arena.getName().equalsIgnoreCase(name)).findFirst();
    }
}
