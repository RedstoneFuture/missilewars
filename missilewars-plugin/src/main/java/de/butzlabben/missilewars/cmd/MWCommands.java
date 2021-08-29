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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MWCommands {

    @Command(name = "mw.paste", usage = "/mw paste <missile>", permission = "mw.paste", description = "Pastes a missile", inGameOnly = true)
    public void pasteCommand(CommandArgs args) {
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
            return;
        }

        Player p = (Player) sender;
        Game game = GameManager.getInstance().getGame(p.getLocation());
        if (game == null) {
            p.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length(); i++) {
            sb.append(args.getArgs(i).replaceAll("&.", ""));
            sb.append(" ");
        }
        Missile m = game.getArena().getMissileConfiguration().getMissileFromName(sb.toString().trim());
        if (m == null) {
            p.sendMessage(MessageConfig.getPrefix() + "§cUnknown missile");
            return;
        }
        MissileFacing mf = MissileFacing.getFacingPlayer(p, game.getArena().getMissileConfiguration());
        m.paste(p, mf, game);
    }

    @Command(name = "mw.start", usage = "/mw start", permission = "mw.start", description = "Starts the game", inGameOnly = true)
    public void startCommand(CommandArgs args) {
        CommandSender cs = args.getSender();
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
            return;
        }

        Player sender = (Player) cs;
        Game game = GameManager.getInstance().getGame(sender.getLocation());

        if (game == null) {
            sender.sendMessage(MessageConfig.getMessage("not_in_arena"));
            return;
        }

        if (game.getState() != GameState.LOBBY) {
            sender.sendMessage(MessageConfig.getPrefix() + "§cGame already started");
            return;
        }
        if (game.isReady())
            game.startGame();
        else {
            if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING && game.getArena() == null) {
                sender.sendMessage(MessageConfig.getPrefix() + "§cGame cannot be started");
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
                sender.sendMessage(MessageConfig.getPrefix() + "A map was elected. Use \"/mw start\" again to start the round");
            }
        }
    }

    @Command(name = "mw.stop", usage = "/mw stop", permission = "mw.stop", description = "Stops the game", inGameOnly = true)
    public void stopCommand(CommandArgs args) {
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

        Bukkit.getScheduler().runTask(MissileWars.getInstance(), game::stopGame);
    }


    @Command(name = "mw.restart", usage = "/mw restart", permission = "mw.restart", description = "Restarts the game", inGameOnly = true)
    public void restartCommand(CommandArgs args) {
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
        CommandSender cs = args.getSender();

        cs.sendMessage(MessageConfig.getPrefix() + "MissileWars v" + MissileWars.getInstance().version + " by Butzlabben");

        if (cs.hasPermission("mw.quit"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw quit -  Quit a game");
        if (cs.hasPermission("mw.start"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw start - Starts the game");
        if (cs.hasPermission("mw.stop"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw stop - Stops the game");
        if (cs.hasPermission("mw.restart"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw start - Restarts the game");
        if (cs.hasPermission("mw.appendrestart"))
            cs.sendMessage(MessageConfig.getPrefix()
                    + "/mw appendrestart - Appends a restart after the next game ends");
        if (cs.hasPermission("mw.paste"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw paste - Pastes a missile");
        if (cs.hasPermission("mw.reload"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw reload - Reloads configurations");
        if (cs.hasPermission("mw.stats"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw stats - Shows stats");
        if (cs.hasPermission("mw.stats.recommendations"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw stats recommendations - Shows recommendations");
        if (cs.hasPermission("mw.stats.players"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw stats players - Shows player list");
        if (cs.hasPermission("mw.stats.list"))
            cs.sendMessage(MessageConfig.getPrefix() + "/mw stats list - Lists history of games");
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
}
