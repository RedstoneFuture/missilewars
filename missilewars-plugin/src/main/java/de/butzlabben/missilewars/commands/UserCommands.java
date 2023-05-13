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
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("mw|missilewars")
public class UserCommands extends BaseCommand {

    @Subcommand("vote")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.vote")
    public void voteCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getMessage("command.to_many_arguments"));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage("game.not_in_game_area"));
            return;
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(Messages.getMessage("vote.change_team_not_now"));
            return;
        }

        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING) {
            player.sendMessage(Messages.getMessage("vote.cant_vote"));
            return;
        }

        if (game.getArena() != null) {
            player.sendMessage(Messages.getMessage("vote.change_team_no_longer_now"));
            return;
        }

        String arenaName = args[0];
        Optional<Arena> arena = Arenas.getFromName(arenaName);
        if (!game.getVotes().containsKey(arenaName) || arena.isEmpty()) {
            player.sendMessage(Messages.getMessage("command.invalid_map"));
            return;
        }

        game.getVotes().put(arenaName, game.getVotes().get(arenaName) + 1);
        player.sendMessage(Messages.getMessage("vote.success").replace("%map%", arena.get().getDisplayName()));
    }

    @Subcommand("change")
    @CommandCompletion("@range:1-2")
    @CommandPermission("mw.change")
    public void changeCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage(Messages.getMessage("command.team_number_needed"));
            return;
        }

        if (args.length > 1) {
            player.sendMessage(Messages.getMessage("command.to_many_arguments"));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage("game.not_in_game_area"));
            return;
        }
        
        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(Messages.getMessage("team.change_team_not_now"));
            return;
        }
        
        if (!(args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("2"))) {
            sender.sendMessage(Messages.getMessage("command.invalid_team_number"));
            return;
        }
        
        MWPlayer mwPlayer = game.getPlayer(player);
        int teamNumber = Integer.parseInt(args[0]);
        Team to = teamNumber == 1 ? game.getTeam1() : game.getTeam2();
        
        // Is the same team?
        if (to == mwPlayer.getTeam()) {
            player.sendMessage(Messages.getMessage("team.already_in_team"));
            return;
        }
        
        // Would the number of team members be too far apart?
        if (to != game.getNextTeam()) {
            player.sendMessage(Messages.getMessage("team.unfair_team_size"));
            return;
        }
        
        // Remove the player from the old team and add him to the new team
        to.addMember(mwPlayer);

        player.sendMessage(Messages.getMessage("team.team_changed").replace("%team%", to.getFullname()));
    }

    @Subcommand("quit|leave")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.quit")
    public void quitCommand(CommandSender sender, String[] args) {

        if (!MWCommands.senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getMessage("command.to_many_arguments"));
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage("game.not_in_game_area"));
            return;
        }

        MissileWars.getInstance().getPlayerListener().registerPlayerArenaLeaveEvent(player, game);
        game.teleportToFallbackSpawn(player);
    }
}
