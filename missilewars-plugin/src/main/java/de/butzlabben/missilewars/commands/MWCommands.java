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
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.game.missile.Missile;
import de.butzlabben.missilewars.game.missile.MissileFacing;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

@CommandAlias("mw|missilewars")
public class MWCommands extends BaseCommand {

    @Default
    @Description("Shows information about the MissileWars Plugin.")
    public void mwCommand(CommandSender sender) {

        sender.sendMessage(Messages.getPrefix() + "MissileWars v" + MissileWars.getInstance().version + " by Butzlabben");

        sendHelpMessage(sender, "mw.vote", "/mw vote", "Vote for a arena.");
        sendHelpMessage(sender, "mw.change", "/mw change <1|2>", "Changes your team.");
        sendHelpMessage(sender, "mw.quit", "/mw quit", "Quit a game.");

        sendHelpMessage(sender, "mw.stats", "/mw stats [from] [arena]", "Shows stats.");
        sendHelpMessage(sender, "mw.stats.recommendations", "/mw stats recommendations [from] [arena]", "Shows recommendations.");
        sendHelpMessage(sender, "mw.stats.players", "/mw stats players [from] [arena]", "Shows player list.");
        sendHelpMessage(sender, "mw.stats.list", "/mw stats list [from] [arena]", "Lists history of games.");

        sendHelpMessage(sender, "mw.listgames", "/mw listgames", "List the active games.");
        sendHelpMessage(sender, "mw.paste", "/mw paste <missile>", "Pastes a missile.");
        sendHelpMessage(sender, "mw.start", "/mw start [lobby]", "Starts the game.");
        sendHelpMessage(sender, "mw.stop", "/mw stop [lobby]", "Stops the game.");
        sendHelpMessage(sender, "mw.appendrestart", "/mw appendrestart [lobby]", "Appends a restart after the next game ends.");
        sendHelpMessage(sender, "mw.reload", "/mw reload", "Reload the plugin.");
        sendHelpMessage(sender, "mw.debug", "/mw debug", "Show debug info.");
        sendHelpMessage(sender, "mw.restartall", "/mw restartall", "Restart all games.");

        sendHelpMessage(sender, "mw.setup", "/mw setup <main|lobby|arena> ...", "Setup the MW locations or the lobby/arena locations.");
    }
    
    @Subcommand("listgames|list|games")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.listgames")
    public void listgamesCommand(CommandSender sender, String[] args) {

        sender.sendMessage(Messages.getPrefix() + "Current games:");

        for (Game game : GameManager.getInstance().getGames().values()) {
            sender.sendMessage("§e " + game.getLobby().getName() + "§7 -- Name: »" + game.getLobby().getDisplayName() + "§7« | Status: " + game.getState());
            sender.sendMessage("§8 - §f" + "Load with startup: §7" + game.getLobby().isAutoLoad());
            sender.sendMessage("§8 - §f" + "Current Arena: §7" + game.getArena().getName() + "§7 -- Name: »" + game.getArena().getDisplayName() + "§7«");
            sender.sendMessage("§8 - §f" + "Total players: §7" + game.getPlayers().size() + "x");
            sender.sendMessage("§8 - §f" + "Team 1: §7" + game.getTeam1().getColor() + game.getTeam1().getName()
                    + " §7with " + game.getTeam1().getMembers().size() + " players");
            sender.sendMessage("§8 - §f" + "Team 2: §7" + game.getTeam2().getColor() + game.getTeam2().getName()
                    + " §7with " + game.getTeam2().getMembers().size() + " players");
        }

    }

    @Subcommand("paste")
    @CommandCompletion("@missiles")
    @CommandPermission("mw.paste")
    public void pasteCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(Messages.getPrefix() + "§cMissile needed.");
            return;
        }

        if (args.length > 1) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        Game game = GameManager.getInstance().getGame(player.getLocation());
        if (game == null) {
            player.sendMessage(Messages.getMessage("not_in_arena"));
            return;
        }

        Missile missile = game.getArena().getMissileConfiguration().getMissileFromName(args[0]);
        if (missile == null) {
            player.sendMessage(Messages.getPrefix() + "§cUnknown missile.");
            return;
        }

        MissileFacing missileFacing = MissileFacing.getFacingPlayer(player, game.getArena().getMissileConfiguration());
        missile.paste(player, missileFacing, game);
    }

    @Subcommand("start")
    @CommandCompletion("@games")
    @CommandPermission("mw.start")
    public void startCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        // Check optional game argument:
        Game game;
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(Messages.getPrefix() + "§cGame not found.");
                return;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(Messages.getMessage("not_in_arena"));
                return;
            }
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(Messages.getPrefix() + "§cGame already started");
            return;
        }

        if (game.isReady())
            game.startGame();
        else {
            if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING && game.getArena() == null) {
                player.sendMessage(Messages.getPrefix() + "§cGame cannot be started");
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
                if (arena.isEmpty()) throw new IllegalStateException("Voted arena is not present");
                game.setArena(arena.get());
                player.sendMessage(Messages.getPrefix() + "A map was elected. Use \"/mw start\" again to start the round");
            }
        }
    }

    @Subcommand("stop")
    @CommandCompletion("@games")
    @CommandPermission("mw.stop")
    public void stopCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        // Check optional game argument:
        Game game;
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(Messages.getPrefix() + "§cGame not found.");
                return;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(Messages.getMessage("not_in_arena"));
                return;
            }
        }

        game.getTeam1().setGameResult(GameResult.DRAW);
        game.getTeam2().setGameResult(GameResult.DRAW);
        if (game.getState() == GameState.INGAME) game.stopGame();
    }

    @Subcommand("appendrestart")
    @CommandCompletion("@games")
    @CommandPermission("mw.appendrestart")
    public void appendrestartCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        // Check optional game argument:
        Game game;
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(Messages.getPrefix() + "§cGame not found.");
                return;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(Messages.getMessage("not_in_arena"));
                return;
            }
        }

        GameManager.getInstance().getGames().values().forEach(Game::appendRestart);
        sender.sendMessage(Messages.getMessage("restart_after_game"));
    }

    @Subcommand("reload")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.reload")
    public void reloadCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        Config.load();
        Messages.load();
        Arenas.load();
        sender.sendMessage(Messages.getPrefix() + "Reloaded configs");
    }

    @Subcommand("debug")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.debug")
    public void debugCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        int i = 0;
        Logger.NORMAL.log("Starting to print debug information for MissileWars v" + MissileWars.getInstance().version);
        for (Game game : GameManager.getInstance().getGames().values()) {
            Logger.NORMAL.log("Printing state for arena " + game.getArena().getName() + ". Number: " + i);
            Logger.NORMAL.log(game.toString());
        }
        sender.sendMessage(Messages.getPrefix() + "Printed debug message into the log file");
    }

    @Subcommand("restartall")
    @CommandCompletion("@nothing")
    @CommandPermission("mw.restartall")
    public void restartallCommand(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(Messages.getPrefix() + "§cToo many arguments.");
            return;
        }

        sender.sendMessage(Messages.getPrefix() + "§cWarning - Restarting all games. This may take a while");
        GameManager.getInstance().restartAll();
        sender.sendMessage(Messages.getPrefix() + "Restarted all games.");
    }

    static boolean senderIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return true;

        sender.sendMessage(Messages.getPrefix() + "§cYou are not a player");
        return false;
    }

    static void sendHelpMessage(CommandSender sender, String permission, String command, String description) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(permission)) return;
        }
        sender.sendMessage(Messages.getPrefix() + command + " - " + description);
    }
}
