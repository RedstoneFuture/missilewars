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
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.GameState;
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import de.butzlabben.missilewars.wrapper.abstracts.Lobby;
import de.butzlabben.missilewars.wrapper.abstracts.MapChooseProcedure;
import de.butzlabben.missilewars.wrapper.missile.Missile;
import de.butzlabben.missilewars.wrapper.missile.MissileFacing;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandAlias("mw|missilewars")
public class MWCommands extends BaseCommand {

    @Default
    @Description("Shows information about the MissileWars Plugin.")
    public void mwCommand(CommandSender sender) {

        sender.sendMessage(MessageConfig.getPrefix() + "MissileWars v" + MissileWars.getInstance().version + " by Butzlabben");

        if (sender.hasPermission("mw.quit"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw quit -  Quit a game");
        if (sender.hasPermission("mw.start"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw start - Starts the game");
        if (sender.hasPermission("mw.stop"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw stop - Stops the game");
        if (sender.hasPermission("mw.restart"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw start - Restarts the game");
        if (sender.hasPermission("mw.appendrestart"))
            sender.sendMessage(MessageConfig.getPrefix()
                    + "/mw appendrestart - Appends a restart after the next game ends");
        if (sender.hasPermission("mw.paste"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw paste - Pastes a missile");
        if (sender.hasPermission("mw.reload"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw reload - Reloads configurations");
        if (sender.hasPermission("mw.stats"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw stats - Shows stats");
        if (sender.hasPermission("mw.stats.recommendations"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw stats recommendations - Shows recommendations");
        if (sender.hasPermission("mw.stats.players"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw stats players - Shows player list");
        if (sender.hasPermission("mw.stats.list"))
            sender.sendMessage(MessageConfig.getPrefix() + "/mw stats list - Lists history of games");
    }

    @Subcommand("paste")
    @Description("Pastes a missile.")
    @Syntax("/mw paste <missile>")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.paste")
    public void pasteCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i].replaceAll("&.", ""));
            sb.append(" ");
        }
        Missile m = game.getArena().getMissileConfiguration().getMissileFromName(sb.toString().trim());
        if (m == null) {
            player.sendMessage(MessageConfig.getPrefix() + "§cUnknown missile");
            return;
        }
        MissileFacing mf = MissileFacing.getFacingPlayer(player, game.getArena().getMissileConfiguration());
        m.paste(player, mf, game);
    }

    @Subcommand("start")
    @Description("Starts the game.")
    @Syntax("/mw start")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.start")
    public void startCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(MessageConfig.getPrefix() + "§cGame already started");
            return;
        }
        if (game.isReady())
            game.startGame();
        else {
            if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING && game.getArena() == null) {
                player.sendMessage(MessageConfig.getPrefix() + "§cGame cannot be started");
            } else {
                Map.Entry<String, Integer> mostVotes = null;
                for (Map.Entry<String, Integer> arena : game.getVotes().entrySet()) {
                    if (mostVotes == null) {
                        mostVotes = arena;
                        continue;
                    }
                    if (arena.getValue() > mostVotes.getValue()) mostVotes = arena;
                }
                if (mostVotes == null) throw new IllegalStateException("Most votes object was null");
                Optional<Arena> arena = Arenas.getFromName(mostVotes.getKey());
                if (!arena.isPresent()) throw new IllegalStateException("Voted arena is not present");
                game.setArena(arena.get());
                player.sendMessage(MessageConfig.getPrefix() + "A map was elected. Use \"/mw start\" again to start the round");
            }
        }
    }

    @Subcommand("stop")
    @Description("Stops the game.")
    @Syntax("/mw stop")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.stop")
    public void stopCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        Bukkit.getScheduler().runTask(MissileWars.getInstance(), game::stopGame);
    }

    @Subcommand("restart")
    @Description("Restarts the game.")
    @Syntax("/mw restart")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.restart")
    public void restartCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        Game game = GameManager.getInstance().getGame(player.getLocation());

        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> {
            if (game.getState() == GameState.INGAME)
                game.stopGame();
            game.reset();
        });
    }

    @Subcommand("appendrestart")
    @Description("Appends a restart after the next game ends.")
    @Syntax("/mw appendrestart")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.appendrestart")
    public void appendRestartCommand(CommandSender sender, String[] args) {

        GameManager.getInstance().getGames().values().forEach(Game::appendRestart);
        sender.sendMessage(MessageConfig.getMessage("restart_after_game"));
    }

    @Subcommand("reload")
    @Description("Reload the plugin.")
    @Syntax("/mw reload")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.reload")
    public void onReload(CommandSender sender, String[] args) {

        Config.load();
        MessageConfig.load();
        Arenas.load();
        sender.sendMessage(MessageConfig.getPrefix() + "Reloaded configs");
    }

    @Subcommand("debug")
    @Description("Show debug info.")
    @Syntax("/mw debug")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.debug")
    public void onDebug(CommandSender sender, String[] args) {

        int i = 0;
        Logger.NORMAL.log("Starting to print debug information for MissileWars v" + MissileWars.getInstance().version);
        for (Game game : GameManager.getInstance().getGames().values()) {
            Logger.NORMAL.log("Printing state for arena " + game.getArena().getName() + ". Number: " + i);
            Logger.NORMAL.log(game.toString());
        }
        sender.sendMessage(MessageConfig.getPrefix() + "Printed debug message into the log file");
    }

    @Subcommand("restartall")
    @Description("Restart all games.")
    @Syntax("/mw restartall")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.reload")
    public void onRestartAll(CommandSender sender, String[] args) {

        sender.sendMessage(MessageConfig.getPrefix() + "§cWarning - Restarting all games. This may take a while");
        List<Lobby> arenaPropertiesList = GameManager.getInstance().getGames().values()
                .stream().map(Game::getLobby).collect(Collectors.toList());
        arenaPropertiesList.forEach(GameManager.getInstance()::restartGame);
        sender.sendMessage(MessageConfig.getPrefix() + "Reloaded configs");
    }

    private boolean senderIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return true;

        sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
        return false;
    }
}
