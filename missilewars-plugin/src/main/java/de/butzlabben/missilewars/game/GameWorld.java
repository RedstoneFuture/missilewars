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
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.Lobby;
import de.butzlabben.missilewars.configuration.Messages;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

@Getter
@ToString(exclude = {"game", "lock"})
public class GameWorld {

    private final String templateName;
    private final String worldNameTemplate;
    private final Game game;
    private final Object lock = new Object();
    private String worldName;

    public GameWorld(Game game, String templateName) {
        this.templateName = templateName;
        this.game = game;
        this.worldNameTemplate = "mw-" + templateName;
    }

    public boolean isWorld(World world) {
        if (world == null) return false;

        if ((worldName == null) || (worldName.isEmpty())) throw new IllegalArgumentException("GameWorld must be loaded first: 'gameWorld.load()'");
        return world.getName().equals(worldName);
    }

    public World getWorld() {
        if ((worldName == null) || (worldName.isEmpty())) throw new IllegalArgumentException("GameWorld must be loaded first: 'gameWorld.load()'");
        return Bukkit.getWorld(worldName);
    }

    public void kickInactivity() {
        synchronized (lock) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.isDead() && p.getWorld().getName().equals(worldName)) {
                    p.kickPlayer(Messages.getMessage("arena.kick_inactivity"));
                }
            });
        }
    }

    public void sendPlayersBack() {
        synchronized (lock) {
            World w = Bukkit.getWorld(worldName);
            if (w == null)
                return;
            Lobby lobby = game.getLobby();
            w.getEntities().stream().filter((f) -> f instanceof Player).forEach(p -> p.teleport(lobby.getAfterGameSpawn()));
        }
    }

    public void unload() {
        synchronized (lock) {
            World w = Bukkit.getWorld(worldName);
            if (w == null)
                return;
            Logger.DEBUG.log("Unloading old world");
            for (Entity e : w.getEntities()) {
                if (e instanceof Player) {
//                    e.remove();
                    Logger.DEBUG.log("Removing: " + e.getName());
                }
            }
            Bukkit.getWorlds().remove(w);
            Bukkit.unloadWorld(w, false);
        }
    }

    public void delete() {
        synchronized (lock) {
            Logger.DEBUG.log("Deleting old world");
            File file = new File(worldName);
            FileUtils.deleteQuietly(file);
            if (file.exists() || file.isDirectory()) {
                Logger.WARN.log("Could not delete old world!");
                file.delete();
            }
        }
    }

    public void load() {
        synchronized (lock) {
            int i = 0;
            File file;
            do {
                worldName = worldNameTemplate + "-" + i;
                file = new File(Bukkit.getWorldContainer(), worldName);
                i++;
            } while (file.exists() || file.isDirectory());

            File arenasFolder = new File(Config.getArenasFolder());
            File newFile = new File(arenasFolder, templateName);

            try {
                FileUtils.copyDirectory(newFile, file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File uid = new File(file, "uid.dat");
            if (uid.isFile()) FileUtils.deleteQuietly(uid);

            Logger.DEBUG.log("Loading new gameworld");
            World world = Bukkit.createWorld(new WorldCreator(worldName));
            Bukkit.getWorlds().add(world);
            
            world.setGameRule(GameRule.DO_TILE_DROPS, game.getArena().isDoTileDrops());
            world.setGameRule(GameRule.KEEP_INVENTORY, game.getArena().isKeepInventory());
        }
    }

}
