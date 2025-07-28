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

package de.butzlabben.missilewars.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.*;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.game.enums.VoteState;
import de.butzlabben.missilewars.game.schematics.objects.Missile;
import de.butzlabben.missilewars.game.timer.LobbyTimer;
import de.butzlabben.missilewars.initialization.ConfigLoader;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("mw|missilewars")
public class MWCommands extends BaseCommand {

    @Default
    @Description("Shows information about the MissileWars Plugin.")
    public void mwCommand(CommandSender sender) {

        sendHelpMessage(sender, "mw.mapmenu", "/mw mapmenu", "Open the map-vote menu.");
        sendHelpMessage(sender, "mw.vote", "/mw vote <arena>", "Vote for a arena.");
        sendHelpMessage(sender, "mw.teammenu", "/mw teammenu", "Open the team-change menu.");
        sendHelpMessage(sender, "mw.change.use", "/mw change <1|2|spec>", "Change your team.");
        sendHelpMessage(sender, "mw.quit", "/mw quit", "Quit a game.");

        sendHelpMessage(sender, "mw.stats", "/mw stats [from] [arena]", "Shows stats.");
        sendHelpMessage(sender, "mw.stats.recommendations", "/mw stats recommendations [from] [arena]", "Shows recommendations.");
        sendHelpMessage(sender, "mw.stats.players", "/mw stats players [from] [arena]", "Shows player list.");
        sendHelpMessage(sender, "mw.stats.list", "/mw stats list [from] [arena]", "Lists history of games.");

        sendHelpMessage(sender, "mw.listgames", "/mw listgames", "List the active games.");
        sendHelpMessage(sender, "mw.move", "/mw move <player> <1|2|spec>", "Change the team of a specific player.");
        sendHelpMessage(sender, "mw.paste", "/mw paste <missile> [flags, e.g. '-tempblock:true']", "Pastes a missile.");
        sendHelpMessage(sender, "mw.start", "/mw start [lobby]", "Starts the game.");
        sendHelpMessage(sender, "mw.stop", "/mw stop [lobby]", "Stops the game.");
        sendHelpMessage(sender, "mw.appendrestart", "/mw appendrestart [lobby]", "Appends a restart after the next game ends.");
        sendHelpMessage(sender, "mw.reload", "/mw reload", "Reload the plugin.");
        sendHelpMessage(sender, "mw.debug", "/mw debug", "Show debug info.");
        sendHelpMessage(sender, "mw.restartall", "/mw restartall", "Restart all games.");

        sendHelpMessage(sender, "mw.version", "/mw version", "Show the plugin version.");
        sendHelpMessage(sender, "mw.setup", "/mw setup <main|lobby|arena> ...", "Setup the MW locations or the lobby/arena locations.");
    }
    
    @Subcommand("version")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.version")
    public void versionCommand(CommandSender sender, String[] args) {
        
        sender.sendMessage(PluginMessages.getPrefix() + "Installed version: " + MissileWars.getInstance().version + " by RedstoneFuture & Butzlabben");
    }

    @Subcommand("listgames|list|games")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.listgames")
    public void listgamesCommand(CommandSender sender, String[] args) {

        sender.sendMessage(PluginMessages.getPrefix() + "Current games:");
        
        sender.sendMessage(" ");
        
        for (Game game : GameManager.getInstance().getGames().values()) {
            TeamManager teamManager = game.getTeamManager();
            
            sender.sendMessage("§e " + game.getGameConfig().getName() 
                    + "§7 -- Name: »" + game.getGameConfig().getDisplayName() 
                    + "§7« | Status: " + game.getState());
            
            sender.sendMessage("§8 - §f" + "Load with startup: §7" + game.getGameConfig().isAutoLoad());
            
            sender.sendMessage("§8 - §f" + "Current Arena: §7" + ((game.getArenaConfig() != null) ? game.getArenaConfig().getName() : "?") 
                    + "§7 -- Name: »" + ((game.getArenaConfig() != null) ? game.getArenaConfig().getDisplayName() : "?") + "§7«");
            
            sender.sendMessage("§8 - §f" + "Total players: §7" + game.getTotalGameUserAmount() + "x");
            
            sender.sendMessage("§8 - §f" + "Team 1: §7" + teamManager.getTeam1().getColor() + teamManager.getTeam1().getName()
                    + " §7with " + teamManager.getTeam1().getMembers().size() + " players");
            
            sender.sendMessage("§8 - §f" + "Team 2: §7" + teamManager.getTeam2().getColor() + teamManager.getTeam2().getName()
                    + " §7with " + teamManager.getTeam2().getMembers().size() + " players");
            
            sender.sendMessage("§8 - §f" + "Spectators: §7" + teamManager.getTeamSpec().getColor() + teamManager.getTeamSpec().getName()
                    + " §7with " + teamManager.getTeamSpec().getMembers().size() + " players");
            
            sender.sendMessage(" ");
        }

    }
    
    @Subcommand("move")
    @CommandCompletion("@game-players @teams @nothing")
    @CommandPermission("mw.move")
    public void moveCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TEAM_NUMBER_NEEDED));
            return;
        }

        if (args.length > 2) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }
        
        Player targetPlayer = MissileWars.getInstance().getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_PLAYER_NOT_ONLINE)
                    .replace("%input%", args[0]));
            return;
        }
        
        MWPlayer targetMwPlayer = game.getPlayer(targetPlayer);
        if (targetMwPlayer == null) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_PLAYER_NOT_IN_GAME)
                    .replace("%input%", args[0]));
            return;
        }
        
        // The "isTeamchangeOngoingGame()" check is skipped here.
        
        
        Team from = targetMwPlayer.getTeam();
        Team to;
        
        switch (args[1]) {
            case "1":
            case "team1":
                to = game.getTeamManager().getTeam1();
                break;
            case "2":
            case "team2":
                to = game.getTeamManager().getTeam2();
                break;
            case "spec":
            case "spectator":
                to = game.getTeamManager().getTeamSpec();
                break;
            default:
                sender.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_TEAM));
                return;
        }
        
        // Is the same team?
        if (from == to) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_MOVE_ALREADY_IN_TEAM)
                    .replace("%player%", targetPlayer.getName()));
            return;
        }
        
        if (game.getState() != GameState.END) {
            // Is the player the last one from his team?
            if ((from.getTeamType() == TeamType.PLAYER) && (from.getMembers().size() == 1)) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_MOVE_IS_LAST_PLAYER)
                    .replace("%from%", from.getFullname()));
                return;
            }
            
        } else {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_MOVE_TEAM_NOT_NOW));
            return;
            
        }
        
        // The "isValidFairSwitch()" validation and max-user check is skipped here.
        
        sender.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_MOVE_MOVED_SENDER)
                .replace("%player%", targetPlayer.getName())
                .replace("%from%", from.getFullname())
                .replace("%to%", to.getFullname()));
        
        targetPlayer.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.TEAM_MOVE_MOVED_TARGET)
                .replace("%sender%", player.getName())
                .replace("%from%", from.getFullname())
                .replace("%to%", to.getFullname()));
        
        game.getGameJoinManager().runPlayerTeamSwitch(targetMwPlayer, to);
    }

    @Subcommand("paste")
    @CommandCompletion("@missiles @missile-flags @missile-flags @missile-flags @missile-flags @nothing")
    @CommandPermission("mw.paste")
    public void pasteCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_MISSILE_NEEDED));
            return;
        }

        if (args.length > 5) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }

        Missile missile = (Missile) game.getArenaConfig().getMissileConfig().getSchematicFromFileName(args[0]);
        if (missile == null) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_MISSILE)
                    .replace("%input%", args[0]));
            return;
        }
        
        boolean hasTempBlock = Config.isTempBlockEnabled();
        Material tempBlockMaterial = Config.getTempBlockMaterial();
        int tempBlockDelay = Config.getUpdateDelay();
        int tempBlockRadius = Config.getUpdateRadius();
        
        for (int i = 1; i < args.length; i++) {
            if (args[i].toLowerCase().startsWith("-tempblock:")) {
                hasTempBlock = Boolean.parseBoolean(args[i].split(":", 2)[1]);
                continue;
            }
            if (args[i].toLowerCase().startsWith("-tempblock_material:")) {
                tempBlockMaterial = MaterialUtil.getMaterial(args[i].split(":", 2)[1]);
                continue;
            }
            if (args[i].toLowerCase().startsWith("-tempblock_delay:")) {
                tempBlockDelay = Integer.parseInt(args[i].split(":", 2)[1]);
                continue;
            }
            if (args[i].toLowerCase().startsWith("-tempblock_radius:")) {
                tempBlockRadius = Integer.parseInt(args[i].split(":", 2)[1]);
                continue;
            }
        }
        
        missile.paste(game, player, hasTempBlock, tempBlockMaterial, tempBlockDelay, tempBlockRadius);
        
        sender.sendMessage(PluginMessages.getPrefix() + "Missile §7" + missile.getDisplayName() + " §fis placed.");
        sender.sendMessage("§8 - §f" + "Temp-Block (for block-updater) enabled: §7" + hasTempBlock);
        sender.sendMessage("§8 - §f" + "Temp-Block material: §7" + tempBlockMaterial);
        sender.sendMessage("§8 - §f" + "Temp-Block delay: §7" + tempBlockDelay + " server ticks");
        sender.sendMessage("§8 - §f" + "Temp-Block radius: §7" + tempBlockRadius + " blocks");
        sender.sendMessage(" ");
        
    }

    @Subcommand("start")
    @CommandCompletion("@games @nothing")
    @CommandPermission("mw.start")
    public void startCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        // Check optional game argument:
        Game game;
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_GAME)
                        .replace("%input%", args[0]));
                return;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_IN_GAME_AREA));
                return;
            }
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_ALREADY_STARTET));
            return;
        }

        if (!game.isReady()) {
            if (game.getMapVoting().getState() == VoteState.RUNNING) {
                game.getMapVoting().setVotedArena();
            } else {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_CAN_NOT_STARTET));
                return;
            }
        }

        LobbyTimer lobbyTimer = (LobbyTimer) game.getTaskManager().getTimer();
        lobbyTimer.executeGameStart();
    }

    @Subcommand("stop")
    @CommandCompletion("@games @nothing")
    @CommandPermission("mw.stop")
    public void stopCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        // Check optional game argument:
        Game game;
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_GAME)
                        .replace("%input%", args[0]));
                return;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_IN_GAME_AREA));
                return;
            }
        }

        game.getTeamManager().getTeam1().setGameResult(GameResult.DRAW);
        game.getTeamManager().getTeam2().setGameResult(GameResult.DRAW);
        if (game.getState() == GameState.INGAME) game.stopGame();
    }

    @Subcommand("appendrestart")
    @CommandCompletion("@games @nothing")
    @CommandPermission("mw.appendrestart")
    public void appendrestartCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        // Check optional game argument:
        Game game;
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_INVALID_GAME)
                        .replace("%input%", args[0]));
                return;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_IN_GAME_AREA));
                return;
            }
        }

        GameManager.getInstance().getGames().values().forEach(Game::appendRestart);
        sender.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.SERVER_RESTART_AFTER_GAME));
    }

    static boolean senderIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return true;

        sender.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_ONLY_PLAYERS));
        return false;
    }

    @Subcommand("reload")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.reload")
    public void reloadCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        ConfigLoader.loadMainConfigs();
        Arenas.load();

        player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.DEBUG_RELOAD_CONFIG));
    }

    @Subcommand("debug")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.debug")
    public void debugCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        int i = 0;
        Logger.NORMAL.log("Starting to print debug information for MissileWars v" + MissileWars.getInstance().version);
        for (Game game : GameManager.getInstance().getGames().values()) {
            Logger.NORMAL.log("Printing state for arena " + game.getArenaConfig().getName() + ". Number: " + i);
            Logger.NORMAL.log(game.toString());
        }

        player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.DEBUG_PRINTED_DEBUG_MSG));
    }

    @Subcommand("restartall")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.restartall")
    public void restartallCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        if (GameManager.getInstance().getGames().size() > 10) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.DEBUG_RESTART_ALL_GAMES_WARN));
        }

        GameManager.getInstance().restartAll();
        player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.DEBUG_RESTART_ALL_GAMES));
    }

    static void sendHelpMessage(CommandSender sender, String permission, String command, String description) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(permission)) return;
        }
        sender.sendMessage(PluginMessages.getPrefix() + command + " - " + description);
    }
    
    static void sendHelpMessage(CommandSender sender, String command, String description) {
        sender.sendMessage(PluginMessages.getPrefix() + command + " - " + description);
    }
}
