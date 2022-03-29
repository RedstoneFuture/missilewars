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

package de.butzlabben.missilewars;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * @author Butzlabben
 * @since 13.08.2018
 */
public class MessageConfig {

    private static final File DIR = MissileWars.getInstance().getDataFolder();
    private static final File FILE = new File(DIR, "messages.yml");
    private static YamlConfiguration cfg;

    public static void load() {

        // check if the directory "/MissileWars" exists
        if (!DIR.exists()) {
            DIR.mkdirs();
        }

        // check if the config file exists
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                Logger.ERROR.log("Could not create messages.yml!");
                e.printStackTrace();
            }
        }

        // try to load the config
        try {
            cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            Logger.ERROR.log("Couldn't load messages.yml");
            e.printStackTrace();
            return;
        }

        // copy the config input
        cfg.options().copyDefaults(true);

        // validate the config options
        addDefaults();

        // re-save the config with only validated options
        saveConfig();
    }

    private static void addDefaults() {

        cfg.addDefault("prefix", "&6•&e● MissileWars &8▎  &7");

        cfg.addDefault("not_in_arena", "&cYou are not in an arena right now");
        cfg.addDefault("game_quit", "You lef this game");
        cfg.addDefault("not_enter_arena", "&cYou may not enter this arena right now");

        cfg.addDefault("game_starts_new_in", "Game starts new in &e%seconds% &7seconds");
        cfg.addDefault("game_ends_in_minutes", "Game ends in &e%minutes% &7minutes");
        cfg.addDefault("game_ends_in_seconds", "Game ends in &e%seconds% &7seconds");
        cfg.addDefault("game_starts_in", "Game starts in &e%seconds% &7seconds");

        cfg.addDefault("not_enough_players", "&cThere are not enough players online");
        cfg.addDefault("teams_unequal", "&cThe teams are unequal distributed");
        cfg.addDefault("game_starts", "&aThe game starts");

        cfg.addDefault("fall_protection", "&cFall protection inactive in %seconds% seconds");
        cfg.addDefault("fall_protection_inactive", "&cFall protection inactive");
        cfg.addDefault("fall_protection_deactivated", "&cFall protection deactivated by sneaking");

        cfg.addDefault("money", "You received &e%money% &7coins");
        cfg.addDefault("kick_inactivity", "&cYou were inactive on missilewars");

        cfg.addDefault("title_won", "%team%");
        cfg.addDefault("subtitle_won", "&7has won the game");
        cfg.addDefault("title_winner", "You");
        cfg.addDefault("subtitle_winner", "&7have won");
        cfg.addDefault("title_loser", "You");
        cfg.addDefault("subtitle_loser", "&7have lost");
        cfg.addDefault("title_draw", "Draw!");
        cfg.addDefault("subtitle_draw", "");

        cfg.addDefault("spectator", "&7You are now a spectator");
        cfg.addDefault("change_team_not_now", "&cNow you cannot change your team anymore");
        cfg.addDefault("already_in_team", "&cYou are already in this team");
        cfg.addDefault("cannot_change_difference", "&cYou cannot change your team");
        cfg.addDefault("team_changed", "You are now in %team%");
        cfg.addDefault("team_assigned", "You have been assigned to %team%");
        cfg.addDefault("lobby_joined", "&e%player% &7joined &8(&7%players%&8/&7%max_players%&8)");
        cfg.addDefault("not_higher", "&cYou can not go higher");
        cfg.addDefault("invalid_missile", "&cInvalid missile");
        cfg.addDefault("hurt_teammates", "&cYou must not hurt your teammates");
        cfg.addDefault("died", "%player% &7died");
        cfg.addDefault("died_explosion", "%player% &7was blown up");
        cfg.addDefault("player_left", "%player% &7left the game");

        cfg.addDefault("team_offline", "Everyone from %team% &7is offline");
        cfg.addDefault("team_buffed", "%team% &7was buffed as one player left the team");
        cfg.addDefault("team_nerved", "%team% &7was nerved as one player joined the team");

        cfg.addDefault("restart_after_game", "&7The server will restart after this game");
        cfg.addDefault("arena_leave", "&cYou are not allowed to leave the arena");
        cfg.addDefault("missile_place_deny", "&cYou are not allowed to place a missile here");

        cfg.addDefault("sign.0", "•● MissileWars ●•");
        cfg.addDefault("sign.1", "%state%");
        cfg.addDefault("sign.2", "%arena%");
        cfg.addDefault("sign.3", "%players%/%max_players%");
        cfg.addDefault("sign.state.lobby", "&aLobby");
        cfg.addDefault("sign.state.ingame", "&bIngame");
        cfg.addDefault("sign.state.ended", "&cRestarting...");
        cfg.addDefault("sign.state.error", "&cError...");

        cfg.addDefault("vote.success", "You successfully voted for the map %map%");
        cfg.addDefault("vote.finished", "The map %map% &7was elected");
        cfg.addDefault("vote.gui", "Vote for a map");

    }

    private static void saveConfig() {
        try {
            cfg.save(FILE);
        } catch (IOException e) {
            Logger.ERROR.log("Could not save messages.yml!");
            e.printStackTrace();
        }
    }

    public static String getMessage(String path) {
        return getPrefix() + getNativeMessage(path);
    }

    public static String getNativeMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getRawMessage(path));
    }

    private static String getRawMessage(String path) {
        return cfg.getString(path, "&cError while reading from messages.yml: " + path);
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', cfg.getString("prefix"));
    }
}
