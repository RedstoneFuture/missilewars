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
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("mw|missilewars")
public class UserCommands extends BaseCommand {
    
    @Subcommand("vote")
    @CommandCompletion("@arenas")
    @CommandPermission("mw.vote")
    public void voteCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_MAP_NEEDED));
            return;
        }

        if (args.length > 1) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }
        
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_CANT_VOTE));
            return;
        }
        
        game.getMapVoting().addVote(player, args[0]);
    }
    
    @Subcommand("mapmenu")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.vote")
    public void mapmenuCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }
        
        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }
        
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_CANT_VOTE));
            return;
        }
        
        // The GUI can also be opened when it is too late to vote according to the settings.
        
        MWPlayer mwPlayer = game.getPlayer(player);
        mwPlayer.getMapVoteMenu().openMenu();
    }

    @Subcommand("change|switch|team")
    @CommandCompletion("@teams")
    @CommandPermission("mw.change.use")
    public void changeCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_TEAM_NUMBER_NEEDED));
            return;
        }

        if (args.length > 1) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }
        
        // Is team change only in lobby allowed?
        if (game.getState() != GameState.LOBBY) {
            if (!game.getArena().isTeamchangeOngoingGame()) {
                player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_CHANGE_TEAM_NOT_NOW));
                return;
            }
            
        // Is too late for team change (last seconds of lobby waiting-time)?
        } else if (game.getArena() != null) {
            if (!game.getArena().isTeamchangeOngoingGame()) {
                if (game.getTaskManager().getTimer().getSeconds() < 10) {
                    player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_CHANGE_TEAM_NO_LONGER_NOW));
                    return;
                }
            }
        }
        
        MWPlayer mwPlayer = game.getPlayer(player);
        
        Team from = mwPlayer.getTeam();
        Team to;
        
        switch (args[0]) {
            case "1":
                if (!player.hasPermission("mw.change.playerteam")) {
                    Messages.getMessage(true, Messages.MessageEnum.NO_PERMISSION);
                    return;
                }
                to = game.getTeam1();
                break;
            case "2":
                if (!player.hasPermission("mw.change.playerteam")) {
                    Messages.getMessage(true, Messages.MessageEnum.NO_PERMISSION);
                    return;
                }
                to = game.getTeam2();
                break;
            case "spec":
            case "spectator":
                if (!player.hasPermission("mw.change.spectator")) {
                    Messages.getMessage(true, Messages.MessageEnum.NO_PERMISSION);
                    return;
                }
                to = game.getTeamSpec();
                break;
            default:
                sender.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_INVALID_TEAM));
                return;
        }
        
        // Is the same team?
        if (from == to) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_ALREADY_IN_TEAM));
            return;
        }

        // Would the number of team members be too far apart?
        if (!game.isValidFairSwitch(from, to)) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_UNFAIR_TEAM_SIZE));
            return;
        }
        
        // Remove the player from the old team and add him to the new team
        to.addMember(mwPlayer);
        
        if (to.getTeamType() == TeamType.SPECTATOR) {
            if (game.getState() != GameState.LOBBY) {
                player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.ARENA_SPECTATOR));
            }
        } else {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_TEAM_ASSIGNED).replace("%team%", to.getFullname()));
        }
        
        game.getScoreboardManager().updateScoreboard();
        mwPlayer.getGameJoinMenu().getMenu();
    }
    
    @Subcommand("teammenu")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.change.use")
    public void teammenuCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }
        
        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }
        
        // The GUI can also be opened if the settings indicate that it is too late to change teams.
        
        MWPlayer mwPlayer = game.getPlayer(player);
        mwPlayer.getTeamSelectionMenu().openMenu();
    }
    
    @Subcommand("quit|leave")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.quit")
    public void quitCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_TO_MANY_ARGUMENTS));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_NOT_IN_GAME_AREA));
            return;
        }

        MissileWars.getInstance().getPlayerListener().registerPlayerArenaLeaveEvent(player, game);
        game.teleportToFallbackSpawn(player);
    }
}
