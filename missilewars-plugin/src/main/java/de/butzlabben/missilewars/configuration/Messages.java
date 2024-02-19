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
import lombok.Getter;
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
        cfg = SetupUtil.getLoadedConfig(FILE);
    }

    private static void addDefaults() {

        for (MessageEnum msg : MessageEnum.values()) {
            cfg.addDefault(msg.getPath(), msg.getDefaultMsg());
        }
    }

    public static String getMessage(boolean prefix, MessageEnum msg) {
        if (prefix) return getPrefix() + getConfigMessage(msg);
        return getConfigMessage(msg);
    }

    public static String getPrefix() {
        return getConfigMessage(MessageEnum.PREFIX);
    }

    private static String getConfigMessage(MessageEnum msg) {
        return ChatColor.translateAlternateColorCodes('&', cfg.getString(msg.getPath(),
                "&cError while reading from messages.yml: '" + msg.getPath() + "'"));
    }

    @Getter
    public enum MessageEnum {
        PREFIX("prefix", "&6•&e● MissileWars &8▎ &7"),

        DEBUG_RELOAD_CONFIG("debug.reload_config", "&7Reloaded configs."),
        DEBUG_RESTART_ALL_GAMES_WARN("debug.restart_all_games_warn", "&cWarning: Restarting all games. This may take a while."),
        DEBUG_RESTART_ALL_GAMES("debug.restart_all_games", "&7Restarted all games."),
        DEBUG_PRINTED_DEBUG_MSG("debug.printed_debug_msg", "&7Printed debug message into the log file."),

        SERVER_RESTART_AFTER_GAME("server.restart_after_game", "&7The server will restart after this game."),

        COMMAND_ONLY_PLAYERS("command.only_players", "&cThe command can only be executed ingame by a player."),
        COMMAND_TO_MANY_ARGUMENTS("command.to_many_arguments", "&cToo many arguments."),
        COMMAND_MISSILE_NEEDED("command.missile_needed", "&cPlease specify the missile."),
        COMMAND_MAP_NEEDED("command.map_needed", "&cPlease specify the map."),
        COMMAND_TEAM_NUMBER_NEEDED("command.team_number_needed", "&cPlease specify the team number."),
        COMMAND_INVALID_MISSILE("command.invalid_missile", "&cThe specified missile %input% was not found."),
        COMMAND_INVALID_SHIELD("command.invalid_shield", "&cThe specified shield %input% was not found."),
        COMMAND_INVALID_GAME("command.invalid_game", "&cThe specified game %input% was not found."),
        COMMAND_INVALID_MAP("command.invalid_map", "&cThe specified map %input% was not found."),
        COMMAND_INVALID_TEAM_NUMBER("command.invalid_team_number", "&cThe team number is invalid. Use \"1\" or \"2\" to specify the target team."),
        
        GAME_PLAYER_JOINED("game.player_joined", "&e%player% &7joined the game (%team%&7)."),
        GAME_PLAYER_LEFT("game.player_left", "&e%player% &7left the game (%team%&7)."),
        GAME_LEFT("game.left", "&7You left the running MissileWars game."),
        GAME_NOT_IN_GAME_AREA("game.not_in_game_area", "&cYou are not in an arena right now."),
        GAME_NOT_ENTER_ARENA("game.not_enter_arena", "&cYou may not enter this arena right now."),
        GAME_ALREADY_STARTET("game.already_startet", "&cGame already started."),
        GAME_CAN_NOT_STARTET("game.can_not_startet", "&cGame cannot be started."),

        LOBBY_TIMER_GAME_STARTS_IN("lobby_timer.game_starts_in", "&7Game starts in &e%seconds% &7seconds."),

        GAME_TIMER_GAME_ENDS_IN_MINUTES("game_timer.game_ends_in_minutes", "&7Game ends in &e%minutes% &7minutes."),
        GAME_TIMER_GAME_ENDS_IN_SECONDS("game_timer.game_ends_in_seconds", "&7Game ends in &e%seconds% &7seconds."),

        ENDGAME_TIMER_GAME_STARTS_NEW_IN("endgame_timer.game_starts_new_in", "&7Game starts new in &e%seconds% &7seconds."),

        LOBBY_PLAYER_JOINED("lobby.player_joined", "&e%player% &7joined the game &8(&7%players%&8/&7%max_players%&8)"),
        LOBBY_PLAYER_LEFT("lobby.player_left", "&e%player% &7left the game &8(&7%players%&8/&7%max_players%&8)"),
        LOBBY_LEFT("lobby.left", "&7You left the MissileWars lobby."),
        LOBBY_NOT_ENOUGH_PLAYERS("lobby.not_enough_players", "&cThere are not enough players online."),
        LOBBY_TEAMS_UNEQUAL("lobby.teams_unequal", "&cThe teams are unequal distributed."),
        LOBBY_GAME_STARTS("lobby.game_starts", "&aThe game starts."),

        TEAM_CHANGE_TEAM_NOT_NOW("team.change_team_not_now", "&cThe game is not in the right state to change your team right now."),
        TEAM_CHANGE_TEAM_NO_LONGER_NOW("team.change_team_no_longer_now", "&cNow you cannot change your team anymore."),
        TEAM_ALREADY_IN_TEAM("team.already_in_team", "&cYou are already in this team."),
        TEAM_UNFAIR_TEAM_SIZE("team.unfair_team_size", "&cChanging the team would make the number of team members more uneven."),
        TEAM_TEAM_CHANGED("team.team_changed", "&7You are now in %team%&7."),
        TEAM_TEAM_ASSIGNED("team.team_assigned", "&7You have been assigned to %team%&7."),
        TEAM_ALL_TEAMMATES_OFFLINE("team.all_teammates_offline", "&7Everyone from %team% &7is offline."),
        TEAM_TEAM_BUFFED("team.team_buffed", "%team% &7was buffed as one player left the team."),
        TEAM_TEAM_NERVED("team.team_nerved", "%team% &7was nerved as one player joined the team."),
        TEAM_HURT_TEAMMATES("team.hurt_teammates", "&cYou must not hurt your teammates."),

        ARENA_SPECTATOR("arena.spectator", "&7You are now a spectator."),
        ARENA_ARENA_LEAVE("arena.arena_leave", "&cYou are not allowed to leave the arena."),
        ARENA_MISSILE_PLACE_DENY("arena.missile_place_deny", "&cYou are not allowed to place a missile here."),
        ARENA_NOT_HIGHER("arena.not_higher", "&cYou can not go higher."),
        ARENA_KICK_INACTIVITY("arena.kick_inactivity", "&cYou were inactive on MissileWars."),

        DIED_NORMAL("died.normal", "&7%player% &7died."),
        DIED_EXPLOSION("died.explosion", "&7%player% &7was blown up."),

        FALL_PROTECTION_START("fall_protection.start", "&cFall protection inactive in %seconds% seconds."),
        FALL_PROTECTION_END("fall_protection.end", "&cFall protection inactive."),
        FALL_PROTECTION_DEACTIVATED("fall_protection.deactivated", "&cFall protection deactivated by sneaking."),

        GAME_RESULT_TITLE_WON("game_result.title_won", "&7%team%"),
        GAME_RESULT_SUBTITLE_WON("game_result.subtitle_won", "&6has won the game!"),
        GAME_RESULT_TITLE_WINNER("game_result.title_winner", "&2Your team"),
        GAME_RESULT_SUBTITLE_WINNER("game_result.subtitle_winner", "&ahas won!"),
        GAME_RESULT_TITLE_LOSER("game_result.title_loser", "&4Your team"),
        GAME_RESULT_SUBTITLE_LOSER("game_result.subtitle_loser", "&chas lost!"),
        GAME_RESULT_TITLE_DRAW("game_result.title_draw", "&7Draw!"),
        GAME_RESULT_SUBTITLE_DRAW("game_result.subtitle_draw", ""),
        GAME_RESULT_MONEY("game_result.money", "&7You received &e%money% &7coins."),

        VOTE_SUCCESS("vote.success", "&7You successfully voted for the map %map%."),
        VOTE_FINISHED("vote.finished", "&7The map %map% &7was selected."),
        VOTE_GUI("vote.gui", "Vote for a map"),
        VOTE_CANT_VOTE("vote.cant_vote", "&cYou can not vote in this game."),
        VOTE_CHANGE_TEAM_NOT_NOW("vote.change_team_not_now", "&cThe game is not in the right state to vote right now."),
        VOTE_CHANGE_TEAM_NO_LONGER_NOW("vote.change_team_no_longer_now", "&cA map was already selected."),
        VOTE_MAP_NOT_AVAILABLE("vote.map_not_available", "&cThe selected map is not available for this game."),
        VOTE_ARENA_ALREADY_SELECTED("vote.arena_already_selected", "&cYou have already voted for this arena."),

        SIGNEDIT_SIGN_CREATED("signedit.sign_created", "&7Sign was successfully created and connected."),
        SIGNEDIT_SIGN_REMOVED("signedit.sign_removed", "&7You have successfully removed this missilewars sign."),
        SIGNEDIT_LOBBY_NOT_FOUND("signedit.lobby_not_found", "&cCould not find lobby %input%."),
        SIGNEDIT_SIGN_REMOVE_DESC("signedit.sign_remove_desc", "&cYou have to be sneaking in order to remove this sign."),

        GAME_STATE_NO_GAME("game_state.no_game", "&cNo Game."),
        GAME_STATE_LOBBY("game_state.lobby", "&aLobby"),
        GAME_STATE_INGAME("game_state.ingame", "&bIngame"),
        GAME_STATE_END("game_state.ended", "&8Restarting..."),
        GAME_STATE_ERROR("game_state.error", "&cError..."),
        
        SIGN_0("sign.0", "•● MissileWars ●•"),
        SIGN_1("sign.1", "%state%"),
        SIGN_2("sign.2", "%arena%"),
        SIGN_3("sign.3", "&7%players%&8/&7%max_players%"),

        STATS_NOT_ENABLED("stats.not_enabled", "&cThe Fight Stats are not enabled!"),
        STATS_FETCHING_PLAYERS("stats.fetching_players", "Fetching not cached player names: %current_size%/%real_size%"),
        STATS_LOADING_DATA("stats.loading_data", "Loading data..."),
        STATS_WRONG_DATE_FORMAT("stats.wrong_date_format", "&cPlease use the date format \"dd.MM.yyyy\"."),
        STATS_TOO_FEW_GAMES("stats.too_few_games", "&cPlease play more than 10 games to enable the Fight Stats for you.");

        private final String path;
        private final String defaultMsg;

        MessageEnum(String path, String defaultMsg) {
            this.path = path;
            this.defaultMsg = defaultMsg;
        }

    }

}
