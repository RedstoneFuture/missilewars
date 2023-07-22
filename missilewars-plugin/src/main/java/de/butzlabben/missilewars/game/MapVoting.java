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

package de.butzlabben.missilewars.game;

import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.enums.VoteState;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapVoting {

    private final Map<MWPlayer, Arena> arenaVotes = new HashMap<>();
    private Game game;
    public VoteState state = VoteState.NULL;

    public MapVoting(Game game) {
        this.game = game;
    }

    public void addVote(Player player, Arena arena) {

        if (game.getArena() != null) {
            //TODO Message
            player.sendMessage(Messages.getPrefix() + Messages.getMessage("vote.arenaAlreadySelected").replace("%map%", arena.getDisplayName()));
            return;
        }

        if (!game.getLobby().getArenas().contains(arena)) {
            //TODO Message
            player.sendMessage(Messages.getPrefix() + Messages.getMessage("vote.arenaNotExist").replace("%map%", arena.getDisplayName()));
            return;
        }

        MWPlayer mwPlayer = game.getPlayer(player);

        // remove old vote
        if (arenaVotes.containsKey(mwPlayer)) {
            arenaVotes.remove(mwPlayer);
        }

        // add new vote
        arenaVotes.put(mwPlayer, arena);

        player.sendMessage(Messages.getMessage("vote.success").replace("%map%", arena.getDisplayName()));
    }

    /**
     * This method returns the arena that has been voted the most by the players.
     *
     * @return (Arena) the winner arena for this vote
     */
    public Arena getVotedArena() {

        Arena arena = arenaVotes.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()).orElseThrow()
                .getKey();

        return arena;
    }

    public void startVote() {
        state = VoteState.RUNNING;
    }

    public void stopVote() {
        state = VoteState.FINISH;
    }

}
