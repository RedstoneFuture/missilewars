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
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.game.enums.VoteState;
import de.butzlabben.missilewars.player.MWPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapVoting {

    private final Map<MWPlayer, Arena> arenaVotes = new HashMap<>();
    private Game game;
    @Getter private VoteState state = VoteState.NULL;

    public MapVoting(Game game) {
        this.game = game;
    }

    /**
     * This method saves the incoming votes of the players, provided that all 
     * conditions for the voting process are met. If the conditions are not met, 
     * the player will be notified accordingly.
     * 
     * @param player (Player) the voter
     * @param arenaName (String) the voted arena name
     */
    public void addVote(Player player, String arenaName) {
        
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_CANT_VOTE));
            return;
        }
        
        if (state == VoteState.NULL) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_CHANGE_TEAM_NOT_NOW));
            return;
        } else if (state == VoteState.FINISH) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_CHANGE_TEAM_NO_LONGER_NOW));
            return;
        }

        Arena arena = Arenas.getFromName(arenaName);
        if (arena == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_INVALID_MAP));
            return;
        }
        
        if (!game.getLobby().getArenas().contains(arena)) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_MAP_NOT_AVAILABLE));
            return;
        }

        MWPlayer mwPlayer = game.getPlayer(player);

        if (arenaVotes.containsKey(mwPlayer)) {

            if (arenaVotes.get(mwPlayer) == arena) {
                player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_ARENA_ALREADY_SELECTED)
                        .replace("%map%", arena.getDisplayName()));
                return;
            }

            // remove old vote
            arenaVotes.remove(mwPlayer);
        }

        // add new vote
        arenaVotes.put(mwPlayer, arena);

        player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.VOTE_SUCCESS).replace("%map%", arena.getDisplayName()));
    }

    /**
     * This method returns the arena that has been voted the most by the players.
     *
     * @return (Arena) the winner arena for this vote
     */
    private Arena getVotedArena() {

        // If no one voted:
        if (arenaVotes.size() == 0) return game.getLobby().getArenas().get(0);

        Arena arena = arenaVotes.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()).orElseThrow()
                .getKey();

        return arena;
    }

    /**
     * This method unlocks the map voting.
     */
    public void startVote() {
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING)
            throw new IllegalStateException("Defined map choose procedure is not \"MAPVOTING\"");

        state = VoteState.RUNNING;
    }

    /**
     * This method locks the map voting again.
     */
    public void stopVote() {
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING)
            throw new IllegalStateException("Defined map choose procedure is not \"MAPVOTING\"");

        state = VoteState.FINISH;
    }

    /**
     * This method checks if there is only one arena map available for this lobby and 
     * therefore no map vote is necessary.
     * 
     * @return (Boolean) true, if only one map exists for this lobby
     */
    public boolean onlyOneArenaFound() {
        return (game.getLobby().getArenas().size() == 1);
    }

    /**
     * This method sets the selected arena of map voting for the current game.
     */
    public void setVotedArena() {
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING)
            throw new IllegalStateException("Defined map choose procedure is not \"MAPVOTING\"");

        if (onlyOneArenaFound()) return;
        if (state != VoteState.RUNNING) return;

        stopVote();

        Arena arena = game.getMapVoting().getVotedArena();
        if (arena == null) throw new IllegalStateException("Voted arena is not present");
        game.setArena(arena);

        game.broadcast(Messages.getMessage(true, Messages.MessageEnum.VOTE_FINISHED)
                .replace("%map%", game.getArena().getDisplayName()));

        game.finalGamePreparations();
    }
    
}
