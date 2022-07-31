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

import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.util.serialization.Serializer;
import de.butzlabben.missilewars.wrapper.abstracts.Lobby;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Getter
public class GameManager {

    @Getter
    private static final GameManager instance = new GameManager();
    private final HashMap<String, Game> games = new HashMap<>();


    public void disableAll() {
        games.values().forEach(Game::disableGameOnServerStop);
        games.clear();
    }

    public void loadGames() {
        File[] lobbyFiles = null;
        if (Config.isMultipleLobbies()) {
            lobbyFiles = new File(Config.getLobbiesFolder()).listFiles();
        } else {
            File file = new File(Config.getLobbiesFolder() + "/" + Config.getDefaultLobby());
            if (file.exists()) {
                lobbyFiles = new File[] {file};
            }
        }
        if (lobbyFiles == null) lobbyFiles = new File[0];

        if (lobbyFiles.length == 0) {
            Logger.WARN.log("No lobby configs found. Creating default one");
            File file = new File(Config.getLobbiesFolder() + "/" + Config.getDefaultLobby());
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

        loadGames(lobbyFiles);
    }

    private void loadGames(File[] lobbyFileList) {

        for (File lobbyFile : lobbyFileList) {
            if (lobbyFile == null)
                continue;
            if (!lobbyFile.getName().endsWith(".yml") && !lobbyFile.getName().endsWith(".yaml")) continue;

            Logger.BOOT.log("Loading lobby " + lobbyFile.getName());
            try {
                Lobby lobby = Serializer.deserialize(lobbyFile, Lobby.class);
                if (lobby == null) {
                    Logger.ERROR.log("Could not load lobby " + lobbyFile.getName());
                    continue;
                }
                Game potentialOtherGame = getGame(lobby.getName());
                if (potentialOtherGame != null) {
                    Logger.ERROR.log("A lobby with the same name was already loaded. Names of lobbies must be unique, this lobby will not be loaded");
                    continue;
                }
                lobby.setFile(lobbyFile);
                restartGame(lobby);
                Logger.BOOTDONE.log("Loaded lobby " + lobbyFile.getName());
            } catch (IOException exception) {
                Logger.ERROR.log("Could not load lobby " + lobbyFile.getName());
                exception.printStackTrace();
            }
        }
    }

    public void disableGame(String lobbyName) {
        Game game = getGame(lobbyName);
        if (game == null) return;

        game.resetGame();
        games.remove(lobbyName);

        Logger.DEBUG.log("Old Game disabled.");
    }

    public void restartGame(Lobby oldLobby) {
        String oldLobbyName = oldLobby.getName();
        disableGame(oldLobbyName);
        try {
            Lobby lobby = Serializer.deserialize(oldLobby.getFile(), Lobby.class);
            lobby.setFile(oldLobby.getFile());
            // Save for possible new values
            Serializer.serialize(oldLobby.getFile(), lobby);
            games.put(oldLobbyName, new Game(lobby));
        } catch (IOException exception) {
            Logger.ERROR.log("Could not load lobby " + oldLobby.getName());
            exception.printStackTrace();
        }
    }

    public Game getGame(String name) {
        return games.get(name);
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
