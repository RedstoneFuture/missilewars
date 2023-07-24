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
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("mw|missilewars")
public class UserCommands extends BaseCommand {

    @Subcommand("vote")
    @CommandCompletion("@nothing")
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
        
        game.getMapVoting().addVote(player, args[0]);
    }

    @Subcommand("change")
    @CommandCompletion("@range:1-2")
    @CommandPermission("mw.change")
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
        
        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_CHANGE_TEAM_NOT_NOW));
            return;
        }
        
        if (!(args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("2"))) {
            sender.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_INVALID_TEAM_NUMBER));
            return;
        }
        
        MWPlayer mwPlayer = game.getPlayer(player);
        int teamNumber = Integer.parseInt(args[0]);
        Team to = teamNumber == 1 ? game.getTeam1() : game.getTeam2();
        
        // Is the same team?
        if (to == mwPlayer.getTeam()) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_ALREADY_IN_TEAM));
            return;
        }
        
        // Would the number of team members be too far apart?
        if (to != game.getNextTeam()) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_UNFAIR_TEAM_SIZE));
            return;
        }
        
        // Remove the player from the old team and add him to the new team
        to.addMember(mwPlayer);

        player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_TEAM_CHANGED).replace("%team%", to.getFullname()));
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
