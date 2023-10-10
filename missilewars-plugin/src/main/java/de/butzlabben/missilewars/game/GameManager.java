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
import de.butzlabben.missilewars.configuration.Lobby;
import de.butzlabben.missilewars.util.geometry.GameArea;
import de.butzlabben.missilewars.util.serialization.Serializer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
        var iterator = games.values().iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iterator.hasNext()) {
            restartGame(iterator.next().getLobby(), false);
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
                Serializer.serialize(file, new Lobby());
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
            Lobby lobby = Serializer.deserialize(lobbyFile, Lobby.class);

            if (lobby == null) {
                Logger.ERROR.log("Could not load lobby of \"" + lobbyFile.getName() + "\"");
                return;
            }

            if (getGame(lobby.getName()) != null) {
                Logger.ERROR.log("A lobby with the same name was already loaded. Names of lobbies must be unique, this lobby will not be loaded");
                return;
            }

            lobby.setFile(lobbyFile);
            restartGame(lobby, false);

        } catch (IOException exception) {
            Logger.ERROR.log("Could not load lobby of \"" + lobbyFile.getName() + "\"");
            exception.printStackTrace();
        }
    }

    /**
     * This method (re)starts a MissileWars game.
     *
     * @param targetLobby (Lobby) the existing lobby of the game
     * @param forceStart  true, if it should also (re)start, if it's not an automatically
     *                    starting game according to the lobby configuration
     */
    public void restartGame(Lobby targetLobby, boolean forceStart) {
        if (!targetLobby.isAutoLoad() && !forceStart) return;

        String targetLobbyName = targetLobby.getName();

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
            Lobby lobby = Serializer.deserialize(targetLobby.getFile(), Lobby.class);
            lobby.setFile(targetLobby.getFile());
            lobby.setArea(new GameArea(lobby.getBukkitWorld(), lobby.getAreaConfig()));
            lobby.updateConfig();

            Logger.BOOTDONE.log("Reloaded lobby \"" + targetLobbyName + "\" (" + targetLobby.getFile().getName() + ")");
            addGame(targetLobbyName, new Game(lobby));

        } catch (IOException exception) {
            Logger.ERROR.log("Could not load lobby of \"" + targetLobby.getFile().getName() + "\"");
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
