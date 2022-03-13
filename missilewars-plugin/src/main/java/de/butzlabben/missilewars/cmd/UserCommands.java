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

package de.butzlabben.missilewars.cmd;

import com.pro_crafting.mc.commandframework.Command;
import com.pro_crafting.mc.commandframework.CommandArgs;
import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.GameState;
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import de.butzlabben.missilewars.wrapper.abstracts.MapChooseProcedure;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class UserCommands {

    @Command(name = "mw.change", usage = "/mw change <1|2>", permission = "mw.change", description = "Changes your team", inGameOnly = true)
    public void changeCommand(CommandArgs args) {
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
            return;
        }

        Player player = (Player) sender;
        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(MessageConfig.getPrefix() + "§cThe game is not in the right state to change your team right now");
            return;
        }

        if (args.length() != 1) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw vote <arena>");
            return;
        }

        if (args.length() != 1) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw change <1|2>");
            return;
        }
        try {
            MWPlayer mwPlayer = game.getPlayer(player);
            int teamNumber = Integer.parseInt(args.getArgs(0));
            Team to = teamNumber == 1 ? game.getTeam1() : game.getTeam2();
            int otherCount = to.getEnemyTeam().getMembers().size() - 1;
            int toCount = to.getMembers().size() + 1;
            int diff = toCount - otherCount;
            if (diff > 1) {
                player.sendMessage(MessageConfig.getMessage("cannot_change_difference"));
                return;
            }

            // Remove the player from the old team and add him to the new team
            to.addMember(mwPlayer);

            player.sendMessage(MessageConfig.getMessage("team_changed").replace("%team%", to.getFullname()));
        } catch (NumberFormatException exception) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw change <1|2>");
        }
    }


    @Command(name = "mw.vote", usage = "/mw vote <arena>", description = "Stops the game", inGameOnly = true)
    public void voteCommand(CommandArgs args) {
        // TODO more messageconfig
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
            return;
        }

        Player player = (Player) sender;
        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(MessageConfig.getPrefix() + "§cThe game is not in the right state to vote right now");
            return;
        }

        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING) {
            player.sendMessage(MessageConfig.getPrefix() + "§cYou can't vote in this game");
            return;
        }

        if (game.getArena() != null) {
            player.sendMessage(MessageConfig.getPrefix() + "§cA map was already elected");
            return;
        }

        if (args.length() != 1) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw vote <arena>");
            return;
        }

        String arenaName = args.getArgs(0);
        Optional<Arena> arena = Arenas.getFromName(arenaName);
        if (!game.getVotes().containsKey(arenaName) || !arena.isPresent()) {
            player.sendMessage(MessageConfig.getPrefix() + "§cNo map with this title was found");
            return;
        }

        game.getVotes().put(arenaName, game.getVotes().get(arenaName) + 1);
        player.sendMessage(MessageConfig.getMessage("vote.success").replace("%map%", arena.get().getDisplayName()));
    }

    @Command(name = "mw.quit", inGameOnly = true, usage = "/mw quit", permission = "mw.quit", description = "Quit a game")
    public void onQuit(CommandArgs args) {
        // TODO message config
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
            return;
        }

        Player player = (Player) sender;
        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }
        MWPlayer mwPlayer = game.getPlayer(player);
        if (mwPlayer == null) {
            player.sendMessage(MessageConfig.getPrefix() + "§cYou are not a member in this arena. Something went wrong pretty badly :(");
            return;
        }
        Location endSpawn = game.getLobby().getAfterGameSpawn();
        if (GameManager.getInstance().getGame(endSpawn) != null) {
            endSpawn = Config.getFallbackSpawn();
        }
        player.teleport(endSpawn);
        player.sendMessage(MessageConfig.getMessage("game_quit"));
    }
}
