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

package de.butzlabben.missilewars.configuration;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.util.SetupUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;


/**
 * @author Butzlabben
 * @since 13.08.2018
 */
public class Messages {

    private static final File DIR = MissileWars.getInstance().getDataFolder();
    private static final File FILE = new File(DIR, "messages.yml");
    private static YamlConfiguration cfg;
    private static boolean isNewConfig = false;

    public static void load() {

        // check if the directory and the file exists or create it new
        isNewConfig = SetupUtil.isNewConfig(DIR, FILE);

        // try to load the config
        cfg = SetupUtil.getLoadedConfig(FILE);

        // copy the config input
        cfg.options().copyDefaults(true);

        // validate the config options
        addDefaults();

        // re-save the config with only validated options
        SetupUtil.safeFile(FILE, cfg);
    }

    private static void addDefaults() {

        cfg.addDefault("prefix", "&6•&e● MissileWars &8▎ &7");

        cfg.addDefault("debug.reload_config", "&7Reloaded configs.");
        cfg.addDefault("debug.restart_all_games_warn", "&cWarning: Restarting all games. This may take a while.");
        cfg.addDefault("debug.restart_all_games", "&7Restarted all games.");
        cfg.addDefault("debug.printed_debug_msg", "&7Printed debug message into the log file.");
        
        cfg.addDefault("server.restart_after_game", "&7The server will restart after this game.");

        cfg.addDefault("command.only_players", "&cYou are not a player.");
        cfg.addDefault("command.to_many_arguments", "&cToo many arguments.");
        cfg.addDefault("command.invalid_missile", "&cThe specified missile was not found.");
        cfg.addDefault("command.invalid_game", "&cThe specified game was not found.");
        cfg.addDefault("command.invalid_map", "&cThe specified map was not found.");
        cfg.addDefault("command.invalid_team_number", "&cThe team number is invalid. Use \"1\" or \"2\" to specify the target team.");
        cfg.addDefault("command.missile_needed", "&cPlease specify the missile.");
        cfg.addDefault("command.team_number_needed", "&cPlease specify the team number.");

        cfg.addDefault("game.map_selected", "&7A map was selected. Use \"/mw start\" again to start the round.");
        cfg.addDefault("game.player_joined", "&e%player% &7joined the game (%team%&7).");
        cfg.addDefault("game.player_left", "&e%player% &7left the game (%team%&7).");
        cfg.addDefault("game.left", "&7You left the running MissileWars game.");
        cfg.addDefault("game.not_in_game_area", "&cYou are not in an arena right now.");
        cfg.addDefault("game.not_enter_arena", "&cYou may not enter this arena right now.");
        cfg.addDefault("game.already_startet", "&cGame already started.");
        cfg.addDefault("game.can_not_startet", "&cGame cannot be started.");
        
        cfg.addDefault("lobby_timer.game_starts_in", "&7Game starts in &e%seconds% &7seconds.");

        cfg.addDefault("game_timer.game_ends_in_minutes", "&7Game ends in &e%minutes% &7minutes.");
        cfg.addDefault("game_timer.game_ends_in_seconds", "&7Game ends in &e%seconds% &7seconds.");

        cfg.addDefault("endgame_timer.game_starts_new_in", "&7Game starts new in &e%seconds% &7seconds.");

        cfg.addDefault("lobby.player_joined", "&e%player% &7joined the game &8(&7%players%&8/&7%max_players%&8)");
        cfg.addDefault("lobby.player_left", "&e%player% &7left the game &8(&7%players%&8/&7%max_players%&8)");
        cfg.addDefault("lobby.left", "&7You left the MissileWars lobby.");
        cfg.addDefault("lobby.not_enough_players", "&cThere are not enough players online.");
        cfg.addDefault("lobby.teams_unequal", "&cThe teams are unequal distributed.");
        cfg.addDefault("lobby.game_starts", "&aThe game starts.");

        cfg.addDefault("team.change_team_not_now", "&cThe game is not in the right state to change your team right now.");
        cfg.addDefault("team.change_team_no_longer_now", "&cNow you cannot change your team anymore.");
        cfg.addDefault("team.already_in_team", "&cYou are already in this team.");
        cfg.addDefault("team.unfair_team_size", "&cChanging the team would make the number of team members more uneven.");
        cfg.addDefault("team.team_changed", "&7You are now in %team%&7.");
        cfg.addDefault("team.team_assigned", "&7You have been assigned to %team%&7.");
        cfg.addDefault("team.all_teammates_offline", "&7Everyone from %team% &7is offline.");
        cfg.addDefault("team.team_buffed", "%team% &7was buffed as one player left the team.");
        cfg.addDefault("team.team_nerved", "%team% &7was nerved as one player joined the team.");
        cfg.addDefault("team.hurt_teammates", "&cYou must not hurt your teammates.");
        
        cfg.addDefault("arena.spectator", "&7You are now a spectator.");
        cfg.addDefault("arena.arena_leave", "&cYou are not allowed to leave the arena.");
        cfg.addDefault("arena.missile_place_deny", "&cYou are not allowed to place a missile here.");
        cfg.addDefault("arena.not_higher", "&cYou can not go higher.");
        cfg.addDefault("arena.kick_inactivity", "&cYou were inactive on MissileWars.");

        cfg.addDefault("died.normal", "&7%player% &7died.");
        cfg.addDefault("died.explosion", "&7%player% &7was blown up.");

        cfg.addDefault("fall_protection.start", "&cFall protection inactive in %seconds% seconds.");
        cfg.addDefault("fall_protection.end", "&cFall protection inactive.");
        cfg.addDefault("fall_protection.deactivated", "&cFall protection deactivated by sneaking.");

        cfg.addDefault("game_result.title_won", "&7%team%");
        cfg.addDefault("game_result.subtitle_won", "&6has won the game!");
        cfg.addDefault("game_result.title_winner", "&2Your team");
        cfg.addDefault("game_result.subtitle_winner", "&ahas won!");
        cfg.addDefault("game_result.title_loser", "&4Your team");
        cfg.addDefault("game_result.subtitle_loser", "&chas lost!");
        cfg.addDefault("game_result.title_draw", "&7Draw!");
        cfg.addDefault("game_result.subtitle_draw", "");
        cfg.addDefault("game_result.money", "&7You received &e%money% &7coins.");
        
        cfg.addDefault("vote.success", "&7You successfully voted for the map %map%.");
        cfg.addDefault("vote.finished", "&7The map %map% &7was elected.");
        cfg.addDefault("vote.gui", "Vote for a map");
        cfg.addDefault("vote.cant_vote", "&cYou can not vote in this game.");
        cfg.addDefault("vote.change_team_not_now", "&cThe game is not in the right state to vote right now.");
        cfg.addDefault("vote.change_team_no_longer_now", "&cA map was already selected.");

        cfg.addDefault("signedit.sign_created", "&7Sign was successfully created and connected.");
        cfg.addDefault("signedit.sign_removed", "&7You have successfully removed this missilewars sign.");
        cfg.addDefault("signedit.lobby_not_found", "&cCould not find lobby %input%.");
        cfg.addDefault("signedit.sign_remove_desc", "&cYou have to be sneaking in order to remove this sign.");

        cfg.addDefault("sign.0", "•● MissileWars ●•");
        cfg.addDefault("sign.1", "%state%");
        cfg.addDefault("sign.2", "%arena%");
        cfg.addDefault("sign.3", "&7%players%&8/&7%max_players%");
        cfg.addDefault("sign.state.lobby", "&aLobby");
        cfg.addDefault("sign.state.ingame", "&bIngame");
        cfg.addDefault("sign.state.ended", "&cRestarting...");
        cfg.addDefault("sign.state.error", "&cError...");

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
