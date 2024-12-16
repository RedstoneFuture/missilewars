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
import de.butzlabben.missilewars.configuration.game.GameConfig;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.util.geometry.GameArea;
import de.butzlabben.missilewars.util.serialization.Serializer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GameManager {

    @Getter
    private static final GameManager instance = new GameManager();
    private final Map<String, Game> games = new HashMap<>();


    public void disableAll() {
        games.values().forEach(Game::disableGameOnServerStop);
        games.clear();
    }

    public void restartAll() {
        List<Game> gamesListCache = new ArrayList<>(games.values());
        
        for (Game game : gamesListCache) {
            game.setState(GameState.END);
            restartGame(game.getGameConfig(), false);
        }
    }

    /**
     * This method is for starting up the server. The game (and included lobby) 
     * configurations are loaded here.
     */
    public void loadGamesOnStartup() {
        File[] gameFiles = null;
        if (Config.useMultipleGames()) {
            gameFiles = new File(Config.getGamesFolder()).listFiles();
        } else {
            File gamesFolder = new File(Config.getGamesFolder());
            File file = new File(gamesFolder, Config.getDefaultGame());
            if (file.exists()) {
                gameFiles = new File[] {file};
            }
        }
        if (gameFiles == null) gameFiles = new File[0];

        if (gameFiles.length == 0) {
            Logger.WARN.log("No game-configs found. Creating default one.");
            File gamesFolder = new File(Config.getGamesFolder());
            gamesFolder.mkdirs();
            File defaultConfig = new File(gamesFolder, Config.getDefaultGame());
            try {
                defaultConfig.createNewFile();
                Serializer.serialize(defaultConfig, new GameConfig());
            } catch (IOException exception) {
                Logger.ERROR.log("Could not create default arena config");
                Logger.ERROR.log("As there are no arenas present, the plugin is shutting down");
                exception.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(MissileWars.getInstance());
                return;
            }
            gameFiles = new File[] {defaultConfig};
        }

        for (File config : gameFiles) {
            if (!config.getName().endsWith(".yml") && !config.getName().endsWith(".yaml")) continue;

            debugStart(config);
        }
    }

    /**
     * This method attempts to read the game (and included lobby) configuration and 
     * build a game from it. Config mistakes are recognized and the config is saved again.
     *
     * @param gameFile (File) the arena configuration file
     */
    private void debugStart(File gameFile) {
        Logger.BOOT.log("Try to loading game from \"" + gameFile.getName() + "\"");

        try {
            GameConfig gameConfig = Serializer.deserialize(gameFile, GameConfig.class);

            if (gameConfig == null) {
                Logger.ERROR.log("Could not get game-config from \"" + gameFile.getName() + "\"");
                return;
            }

            if (getGame(gameConfig.getName()) != null) {
                Logger.ERROR.log("A game with the same name was already loaded. Names of games must be unique. " 
                        + "This game and his lobby will not be loaded.");
                return;
            }

            gameConfig.setFile(gameFile);
            restartGame(gameConfig, false);

        } catch (IOException exception) {
            Logger.ERROR.log("Could not load game from \"" + gameFile.getName() + "\"");
            exception.printStackTrace();
        }
    }

    /**
     * This method (re)starts a MissileWars game.
     *
     * @param targetGameConfig (GameConfig) the existing game-config of the game
     * @param forceStart  true, if it should also (re)start, if it's not an automatically 
     *                    starting game according to the game configuration
     */
    public void restartGame(GameConfig targetGameConfig, boolean forceStart) {
        if (!targetGameConfig.isAutoLoad() && !forceStart) return;

        String targetGameName = targetGameConfig.getName();

        // reset the old game
        Game game = getGame(targetGameName);
        if (game != null) {
            game.resetGame();
        }

        // delete the old game from the list
        games.remove(targetGameName);

        Logger.DEBUG.log("Old game disabled.");

        // read the game configuration and build a new game and lobby from it
        try {
            GameConfig gameConfig = Serializer.deserialize(targetGameConfig.getFile(), GameConfig.class);
            gameConfig.setFile(targetGameConfig.getFile());
            gameConfig.setArea(new GameArea(gameConfig.getLobbyConfig().getBukkitWorld(), gameConfig.getLobbyConfig().getAreaConfig()));
            gameConfig.updateConfig();

            Logger.BOOTDONE.log("Reloaded game \"" + targetGameName + "\" (" + targetGameConfig.getFile().getName() + ")");
            addGame(targetGameName, new Game(gameConfig));

        } catch (IOException exception) {
            Logger.ERROR.log("Could not load game from \"" + targetGameConfig.getFile().getName() + "\"");
            exception.printStackTrace();
        }
    }

    public Game getGame(String name) {
        return games.get(name);
    }

    public void addGame(String name, Game game) {
        games.put(name, game);
    }

    public int getGameAmount() {
        return games.size();
    }

    public Game getGame(Location location) {
        for (Game game : GameManager.getInstance().getGames().values()) {
            if (game.isIn(location)) {
                return game;
            }
        }
        return null;
    }
    
}
