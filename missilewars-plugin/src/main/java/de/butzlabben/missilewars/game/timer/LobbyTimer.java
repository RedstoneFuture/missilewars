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

package de.butzlabben.missilewars.game.timer;

import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import de.butzlabben.missilewars.wrapper.abstracts.MapChooseProcedure;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import java.util.Map;
import java.util.Optional;

/**
 * @author Butzlabben
 * @since 11.01.2018
 */
public class LobbyTimer extends Timer implements Runnable {

    private final int startTime;
    private int remaining = 90; // for sending messages


    public LobbyTimer(Game game, int startTime) {
        super(game);
        this.startTime = startTime;
        seconds = startTime;
    }

    @Override
    public void tick() {
        if (getGame().getPlayers().values().size() == 0)
            return;

        for (MWPlayer mp : getGame().getPlayers().values()) {
            if (mp.getPlayer() != null) mp.getPlayer().setLevel(seconds);
            else {
                if (mp.getTeam() != null) mp.getTeam().removeMember(mp);
                getGame().getPlayers().remove(mp.getUUID());
            }
        }

        int size1 = getGame().getTeam1().getMembers().size();
        int size2 = getGame().getTeam2().getMembers().size();
        if (size1 == 0 || size2 == 0) {
            seconds = startTime;
            return;
        }
        --remaining;
        if (remaining == 0) {
            if (size1 + size1 < getGame().getLobby().getMinSize())
                return;
            seconds = startTime;
            remaining = 90;
            broadcast(MessageConfig.getMessage("not_enough_players"));
        }
        if (seconds == 120) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "120"));
            playPling();
        } else if (seconds == 60) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "60"));
            playPling();
        } else if (seconds == 30) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "30"));
            playPling();
        } else if (seconds == 10) {
            checkVote();
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "10"));
            playPling();
        } else if (seconds == 5) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "5"));
            playPling();
        } else if (seconds == 4) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "4"));
            playPling();
        } else if (seconds == 3) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "3"));
            playPling();
        } else if (seconds == 2) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "2"));
            playPling();
        } else if (seconds == 1) {
            broadcast(MessageConfig.getMessage("game_starts_in").replace("%seconds%", "1"));
            playPling();

        } else if (seconds == 0) {
            int diff = size1 - size2;
            if (diff >= 2 || diff <= -2) {
                broadcast(MessageConfig.getMessage("teams_unequal"));
                seconds = startTime;
                return;
            }
            broadcast(MessageConfig.getMessage("game_starts"));
            playPling();
            getGame().startGame();
            return;
        }
        --seconds;
    }

    private void playPling() {
        for (MWPlayer p : getGame().getPlayers().values()) {
            VersionUtil.playPling(p.getPlayer());
        }
    }

    private void checkVote() {
        Game game = getGame();
        if (game.getLobby().getMapChooseProcedure() != MapChooseProcedure.MAPVOTING) return;
        if (game.getArena() != null) return;

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
    }
}
