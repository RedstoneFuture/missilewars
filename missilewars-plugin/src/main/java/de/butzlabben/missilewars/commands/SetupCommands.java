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
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("mw|missilewars")
@Subcommand("setup")
public class SetupCommands extends BaseCommand {

    private Game game;
    private Player player;

    @Default
    @CommandPermission("mw.setup")
    public void setupCommands(CommandSender sender, String[] args) {
        sender.sendMessage(PluginMessages.getPrefix() + "§fSetup usage: §7/mw setup <main|lobby|arena> ...");
    }

    @Subcommand("main")
    public class MainSetupCommands extends BaseCommand {

        @Subcommand("fallbackspawn")
        public class FallbackspawnSetup extends BaseCommand {

            @Subcommand("set")
            @CommandCompletion("@nothing")
            public void set(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;

                Config.setFallbackSpawn(player.getLocation());
                player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'fallbackSpawn' to " + player.getLocation() + ".");
            }

            @Subcommand("teleport|tp")
            @CommandCompletion("@nothing")
            public void teleport(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;

                player.teleport(Config.getFallbackSpawn());
                player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'fallbackSpawn'.");
            }

        }
    }

    /**
     * This method checks if the command sender is a valid ingame player.
     *
     * @param sender = the command sender
     *
     * @return true, if it's an ingame player
     */
    private boolean senderIsPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            player = (Player) sender;
            return true;
        }

        sender.sendMessage(PluginMessages.getPrefix() + "§cYou are not a player");
        return false;
    }

    @Subcommand("arena")
    public class ArenaSetupCommands extends BaseCommand {

        @Subcommand("spectatorspawn")
        public class SpectatorspawnSetup extends BaseCommand {

            @Subcommand("set")
            @CommandCompletion("@games @nothing")
            public void set(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                game.getArena().setSpectatorSpawn(player.getLocation());
                game.getArena().updateConfig();
                player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'spectatorSpawn' to " + player.getLocation() + ".");
            }

            @Subcommand("teleport|tp")
            @CommandCompletion("@games @nothing")
            public void teleport(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                player.teleport(game.getArena().getSpectatorSpawn());
                player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'spectatorSpawn'.");
            }

        }

        @Subcommand("team1spawn")
        public class Team1spawnSetup extends BaseCommand {

            @Subcommand("set")
            @CommandCompletion("@games @nothing")
            public void set(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                game.getArena().setTeam1Spawn(player.getLocation());
                game.getArena().updateConfig();
                player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'team1Spawn' to " + player.getLocation() + ".");
            }

            @Subcommand("teleport|tp")
            @CommandCompletion("@games @nothing")
            public void teleport(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                player.teleport(game.getArena().getTeam1Spawn());
                player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'team1Spawn'.");
            }

        }

        @Subcommand("team2spawn")
        public class Team2spawnSetup extends BaseCommand {

            @Subcommand("set")
            @CommandCompletion("@games @nothing")
            public void set(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                game.getArena().setTeam2Spawn(player.getLocation());
                game.getArena().updateConfig();
                player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'team2Spawn' to " + player.getLocation() + ".");
            }

            @Subcommand("teleport|tp")
            @CommandCompletion("@games @nothing")
            public void teleport(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                player.teleport(game.getArena().getTeam2Spawn());
                player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'team2Spawn'.");
            }

        }

        @Subcommand("area")
        public class AreaSetup extends BaseCommand {

            @Subcommand("pos1")
            public class Pos1Setup extends BaseCommand {

                @Subcommand("set")
                @CommandCompletion("@games @nothing")
                public void set(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    game.getArena().getArea().setPosition1(player.getLocation());
                    game.getArena().setAreaConfig(game.getArena().getArea().getAreaConfiguration());
                    game.getArena().updateConfig();
                    player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'arena area' (position 1) to " + player.getLocation() + ".");
                }

                @Subcommand("teleport|tp")
                @CommandCompletion("@games @nothing")
                public void teleport(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    player.teleport(game.getArena().getArea().getPosition1());
                    player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'arena area' (position 1): " + game.getArena().getArea().getPosition1().toString());
                }

            }

            @Subcommand("pos2")
            public class Pos2Setup extends BaseCommand {

                @Subcommand("set")
                @CommandCompletion("@games @nothing")
                public void set(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    game.getArena().getArea().setPosition2(player.getLocation());
                    game.getArena().setAreaConfig(game.getArena().getArea().getAreaConfiguration());
                    game.getArena().updateConfig();
                    player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'arena area' (position 2) to " + player.getLocation() + ".");
                }

                @Subcommand("teleport|tp")
                @CommandCompletion("@games @nothing")
                public void teleport(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    player.teleport(game.getArena().getArea().getPosition2());
                    player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'arena area' (position 2): " + game.getArena().getArea().getPosition2().toString());
                }

            }

        }
    }

    @Subcommand("lobby")
    public class LobbySetupCommands extends BaseCommand {

        @Subcommand("spawnpoint")
        public class SpawnpointSetup extends BaseCommand {

            @Subcommand("set")
            @CommandCompletion("@games @nothing")
            public void set(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                game.getLobby().setSpawnPoint(player.getLocation());
                game.getLobby().updateConfig();
                player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'spawnPoint' to " + player.getLocation() + ".");
            }

            @Subcommand("teleport|tp")
            @CommandCompletion("@games @nothing")
            public void teleport(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                player.teleport(game.getLobby().getSpawnPoint());
                player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'spawnPoint'.");
            }

        }

        @Subcommand("aftergamespawn")
        public class AftergamespawnSetup extends BaseCommand {

            @Subcommand("set")
            @CommandCompletion("@games @nothing")
            public void set(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                game.getLobby().setAfterGameSpawn(player.getLocation());
                game.getLobby().updateConfig();
                player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'afterGameSpawn' to " + player.getLocation() + ".");
            }

            @Subcommand("teleport|tp")
            @CommandCompletion("@games @nothing")
            public void teleport(CommandSender sender, String[] args) {
                if (!senderIsPlayer(sender)) return;
                if (!isValidGame(args)) return;

                player.teleport(game.getLobby().getAfterGameSpawn());
                player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'afterGameSpawn'.");
            }

        }

        @Subcommand("area")
        public class AreaSetup extends BaseCommand {

            @Subcommand("pos1")
            public class Pos1Setup extends BaseCommand {

                @Subcommand("set")
                @CommandCompletion("@games @nothing")
                public void set(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    game.getLobby().getArea().setPosition1(player.getLocation());
                    game.getLobby().setAreaConfig(game.getLobby().getArea().getAreaConfiguration());
                    game.getLobby().updateConfig();
                    player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'lobby area' (position 1) to " + player.getLocation() + ".");
                }

                @Subcommand("teleport|tp")
                @CommandCompletion("@games @nothing")
                public void teleport(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    player.teleport(game.getLobby().getArea().getPosition1());
                    player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'lobby area' (position 1): " + game.getLobby().getArea().getPosition1().toString());
                }

            }

            @Subcommand("pos2")
            public class Pos2Setup extends BaseCommand {

                @Subcommand("set")
                @CommandCompletion("@games @nothing")
                public void set(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    game.getLobby().getArea().setPosition2(player.getLocation());
                    game.getLobby().setAreaConfig(game.getLobby().getArea().getAreaConfiguration());
                    game.getLobby().updateConfig();
                    player.sendMessage(PluginMessages.getPrefix() + "§fSet new 'lobby area' (position 2) to " + player.getLocation() + ".");
                }

                @Subcommand("teleport|tp")
                @CommandCompletion("@games @nothing")
                public void teleport(CommandSender sender, String[] args) {
                    if (!senderIsPlayer(sender)) return;
                    if (!isValidGame(args)) return;

                    player.teleport(game.getLobby().getArea().getPosition2());
                    player.sendMessage(PluginMessages.getPrefix() + "§fTeleported to 'lobby area' (position 2): " + game.getLobby().getArea().getPosition2().toString());
                }

            }

        }
    }

    /**
     * This method checks if the player execute the command on a valid
     * game world (lobby or area).
     *
     * @return true, if it's a MissileWars game world
     */
    private boolean isValidGame(String[] args) {

        // Check optional game argument:
        if (args.length == 1) {
            game = GameManager.getInstance().getGame(args[0]);
            if (game == null) {
                player.sendMessage(PluginMessages.getPrefix() + "§cGame not found.");
                return false;
            }
        } else {
            game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) {
                player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_NOT_IN_GAME_AREA));
                return false;
            }
        }

        return true;
    }
}
