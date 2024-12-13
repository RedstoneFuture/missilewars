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
     * This method is for starting up the server. The game lobby configurations
     * are loaded here.
     */
    public void loadGamesOnStartup() {
        File[] lobbyFiles = null;
        if (Config.isMultipleLobbies()) {
            lobbyFiles = new File(Config.getLobbiesFolder()).listFiles();
        } else {
            File lobbiesFolder = new File(Config.getLobbiesFolder());
            File file = new File(lobbiesFolder, Config.getDefaultLobby());
            if (file.exists()) {
                lobbyFiles = new File[] {file};
            }
        }
        if (lobbyFiles == null) lobbyFiles = new File[0];

        if (lobbyFiles.length == 0) {
            Logger.WARN.log("No lobby configs found. Creating default one");
            File lobbiesFolder = new File(Config.getLobbiesFolder());
            File file = new File(lobbiesFolder, Config.getDefaultLobby());
            try {
                file.createNewFile();
                Serializer.serialize(file, new GameConfig());
            } catch (IOException exception) {
                Logger.ERROR.log("Could not create default arena config");
                Logger.ERROR.log("As there are no arenas present, the plugin is shutting down");
                exception.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(MissileWars.getInstance());
                return;
            }
            lobbyFiles = new File[] {file};
        }

        for (File lobbyFile : lobbyFiles) {
            if (lobbyFile == null) continue;
            if (!lobbyFile.getName().endsWith(".yml") && !lobbyFile.getName().endsWith(".yaml")) continue;

            debugStart(lobbyFile);
        }
    }

    /**
     * This method attempts to read the game lobby configuration and build a game
     * from it. Config mistakes are recognized and the config is saved again.
     *
     * @param lobbyFile (File) the arena configuration file
     */
    private void debugStart(File lobbyFile) {
        Logger.BOOT.log("Try to loading lobby of \"" + lobbyFile.getName() + "\"");

        try {
            GameConfig gameConfig = Serializer.deserialize(lobbyFile, GameConfig.class);

            if (gameConfig == null) {
                Logger.ERROR.log("Could not load lobby of \"" + lobbyFile.getName() + "\"");
                return;
            }

            if (getGame(gameConfig.getName()) != null) {
                Logger.ERROR.log("A lobby with the same name was already loaded. Names of lobbies must be unique, this lobby will not be loaded");
                return;
            }

            gameConfig.setFile(lobbyFile);
            restartGame(gameConfig, false);

        } catch (IOException exception) {
            Logger.ERROR.log("Could not load lobby of \"" + lobbyFile.getName() + "\"");
            exception.printStackTrace();
        }
    }

    /**
     * This method (re)starts a MissileWars game.
     *
     * @param targetGameConfig (Lobby) the existing lobby of the game
     * @param forceStart  true, if it should also (re)start, if it's not an automatically
     *                    starting game according to the lobby configuration
     */
    public void restartGame(GameConfig targetGameConfig, boolean forceStart) {
        if (!targetGameConfig.isAutoLoad() && !forceStart) return;

        String targetLobbyName = targetGameConfig.getName();

        // reset the old game
        Game game = getGame(targetLobbyName);
        if (game != null) {
            game.resetGame();
        }

        // delete the old game from the list
        games.remove(targetLobbyName);

        Logger.DEBUG.log("Old Game disabled.");

        // read the game lobby configuration and build a new game and lobby from it
        try {
            GameConfig gameConfig = Serializer.deserialize(targetGameConfig.getFile(), GameConfig.class);
            gameConfig.setFile(targetGameConfig.getFile());
            gameConfig.setArea(new GameArea(gameConfig.getBukkitWorld(), gameConfig.getAreaConfig()));
            gameConfig.updateConfig();

            Logger.BOOTDONE.log("Reloaded lobby \"" + targetLobbyName + "\" (" + targetGameConfig.getFile().getName() + ")");
            addGame(targetLobbyName, new Game(gameConfig));

        } catch (IOException exception) {
            Logger.ERROR.log("Could not load lobby of \"" + targetGameConfig.getFile().getName() + "\"");
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
