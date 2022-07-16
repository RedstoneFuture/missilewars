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

public class MWCommands {

    @Command(name = "mw.paste", usage = "/mw paste <missile>", permission = "mw.paste", description = "Pastes a missile", inGameOnly = true)
    public void pasteCommand(CommandArgs args) {

        CommandSender sender = args.getSender();
        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        String arguments = getAllNextArgumentsAsString(args, false);
        Missile m = game.getArena().getMissileConfiguration().getMissileFromName(arguments.trim());
        if (m == null) {
            player.sendMessage(MessageConfig.getPrefix() + "§cUnknown missile");
            return;
        }
        MissileFacing mf = MissileFacing.getFacingPlayer(player, game.getArena().getMissileConfiguration());
        m.paste(player, mf, game);
    }

    @Command(name = "mw.start", usage = "/mw start", permission = "mw.start", description = "Starts the game", inGameOnly = true)
    public void startCommand(CommandArgs args) {

        CommandSender sender = args.getSender();
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

    @Command(name = "mw.stop", usage = "/mw stop", permission = "mw.stop", description = "Stops the game", inGameOnly = true)
    public void stopCommand(CommandArgs args) {

        CommandSender sender = args.getSender();
        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        Bukkit.getScheduler().runTask(MissileWars.getInstance(), game::stopGame);
    }


    @Command(name = "mw.restart", usage = "/mw restart", permission = "mw.restart", description = "Restarts the game", inGameOnly = true)
    public void restartCommand(CommandArgs args) {

        CommandSender sender = args.getSender();
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

    @Command(name = "mw.appendrestart", usage = "/mw appendrestart", permission = "mw.appendrestart", description = "Appends a restart after the next game ends")
    public void appendRestartCommand(CommandArgs args) {
        GameManager.getInstance().getGames().values().forEach(Game::appendRestart);
        args.getSender().sendMessage(MessageConfig.getMessage("restart_after_game"));
    }

    @Command(name = "mw", aliases = "missilewars", usage = "/mw", description = "Shows information about the MissileWars Plugin")
    public void mwCommand(CommandArgs args) {

        CommandSender sender = args.getSender();

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

    @Command(name = "mw.reload", permission = "mw.reload", usage = "/mw reload")
    public void onReload(CommandArgs args) {

        CommandSender sender = args.getSender();

        Config.load();
        MessageConfig.load();
        Arenas.load();
        sender.sendMessage(MessageConfig.getPrefix() + "Reloaded configs");
    }

    @Command(name = "mw.debug", permission = "mw.debug", usage = "/mw debug")
    public void onDebug(CommandArgs args) {

        CommandSender sender = args.getSender();

        int i = 0;
        Logger.NORMAL.log("Starting to print debug information for MissileWars v" + MissileWars.getInstance().version);
        for (Game game : GameManager.getInstance().getGames().values()) {
            Logger.NORMAL.log("Printing state for arena " + game.getArena().getName() + ". Number: " + i);
            Logger.NORMAL.log(game.toString());
        }
        sender.sendMessage(MessageConfig.getPrefix() + "Printed debug message into the log file");
    }

    @Command(name = "mw.restartall", permission = "mw.reload", usage = "/mw restartall")
    public void onRestartAll(CommandArgs args) {

        CommandSender sender = args.getSender();

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

    /**
     * This method returns all next command arguments as one String line.
     * Separated with a " " between the arguments. It also removes all
     * unnecessary "&" signs.
     *
     * @param args Argument Array
     * @return (String) all next arguments
     */
    private String getAllNextArgumentsAsString(CommandArgs args, boolean filterColorCode) {
        StringBuilder sb = new StringBuilder();
        String arguments;

        for (int i = 0; i < args.length(); i++) {
            sb.append(" ");
        }
        arguments = sb.toString();

        if (filterColorCode) arguments.replaceAll("&.", "");

        return arguments;
    }
}
