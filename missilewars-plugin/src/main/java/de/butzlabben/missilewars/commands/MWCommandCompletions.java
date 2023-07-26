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

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MWCommandCompletions {

    private final CommandCompletions<BukkitCommandCompletionContext> commandCompletions;

    public MWCommandCompletions(PaperCommandManager commandManager) {
        this.commandCompletions = commandManager.getCommandCompletions();

        registerGamesResult();
        registerMissilesResult();
        registerArenasResult();
    }

    private void registerGamesResult() {
        commandCompletions.registerCompletion("games", c -> GameManager.getInstance().getGames().keySet());
    }

    private void registerMissilesResult() {
        commandCompletions.registerCompletion("missiles", c -> {
            CommandSender sender = c.getSender();

            if (!(sender instanceof Player)) return null;
            Player player = (Player) sender;

            Game game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) return null;

            return game.getArena().getMissileConfiguration().getMissileNames();
        });
    }

    private void registerArenasResult() {
        commandCompletions.registerCompletion("arenas", c -> {
            CommandSender sender = c.getSender();

            if (!(sender instanceof Player)) return null;
            Player player = (Player) sender;

            Game game = GameManager.getInstance().getGame(player.getLocation());
            if (game == null) return null;

            return game.getLobby().getPossibleArenas();
        });
    }

}
