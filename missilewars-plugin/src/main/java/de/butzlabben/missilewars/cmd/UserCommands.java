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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
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

@CommandAlias("mw|missilewars")
public class UserCommands extends BaseCommand {

    @Subcommand("change")
    @Description("Changes your team.")
    @Syntax("/mw change <1|2>")
    @CommandCompletion("@range:1-2")
    @CommandPermission("mw.change")
    public void changeCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
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

        if (args.length != 1) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw vote <arena>");
            return;
        }

        if (args.length != 1) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw change <1|2>");
            return;
        }
        try {
            MWPlayer mwPlayer = game.getPlayer(player);
            int teamNumber = Integer.parseInt(args[0]);
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

    @Subcommand("vote")
    @Description("Stops the game.")
    @Syntax("/mw vote <arena>")
    @CommandPermission("mw.vote")
    public void voteCommand(CommandSender sender, String[] args) {

        // TODO more messageconfig
        if (!senderIsPlayer(sender)) return;
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

        if (args.length != 1) {
            player.sendMessage(MessageConfig.getPrefix() + "§c/mw vote <arena>");
            return;
        }

        String arenaName = args[0];
        Optional<Arena> arena = Arenas.getFromName(arenaName);
        if (!game.getVotes().containsKey(arenaName) || !arena.isPresent()) {
            player.sendMessage(MessageConfig.getPrefix() + "§cNo map with this title was found");
            return;
        }

        game.getVotes().put(arenaName, game.getVotes().get(arenaName) + 1);
        player.sendMessage(MessageConfig.getMessage("vote.success").replace("%map%", arena.get().getDisplayName()));
    }

    @Subcommand("quit|leave")
    @Description("Quit a game.")
    @Syntax("/mw quit")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.quit")
    public void onQuit(CommandSender sender, String[] args) {

        // TODO message config
        if (!senderIsPlayer(sender)) return;
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

    private boolean senderIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return true;

        sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
        return false;
    }
}
