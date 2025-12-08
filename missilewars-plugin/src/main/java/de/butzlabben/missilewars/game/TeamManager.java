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

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.game.GameConfig;
import de.butzlabben.missilewars.game.enums.TeamType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TeamManager {
    
    private final Game game;
    private final GameConfig gameConfig;
    
    @Getter private Team team1;
    @Getter private Team team2;
    @Getter private Team teamSpec;
    
    private final Map<UUID, Team> offlinePlayerTeam = new HashMap<>();
    
    public TeamManager(Game game) {
        this.game = game;
        this.gameConfig = game.getGameConfig();
        
        team1 = new Team(gameConfig.getTeam1Config().getName(), gameConfig.getTeam1Config().getColor(), game, TeamType.PLAYER);
        team2 = new Team(gameConfig.getTeam2Config().getName(), gameConfig.getTeam2Config().getColor(), game, TeamType.PLAYER);
        teamSpec = new Team(gameConfig.getTeamConfigSpec().getName(), gameConfig.getTeamConfigSpec().getColor(), game, TeamType.SPECTATOR);
        
        team1.initialTeam();
        team2.initialTeam();
        teamSpec.initialTeam();
    }
    
    /**
     * This method provides the next best team that a new player should be 
     * added into, so that the game remains/becomes balanced in terms of team 
     * sizes. Depending on the team size ratio, either the smaller or a 
     * random team is returned.
     * 
     * @return (Team) the next team for balanced game sizes
     */
    public Team getNextPlayerTeam() {
        if (isSamePlayerTeamSize()) {
            Random randomizer = new Random();
            
            if (randomizer.nextBoolean()) {
                return team1;
            } else {
                return team2;
            }
        }
        
        return getSmallerPlayerTeam();
    }

    /**
     * This method returns the smaller player-team of Team1 and Team2.
     *
     * @return (Team) the smaller team
     */
    public Team getSmallerPlayerTeam() {
        if (team1.getMembers().size() > team2.getMembers().size()) {
            return team2;
        } else if (team1.getMembers().size() < team2.getMembers().size()) {
            return team1;
        }
        
        return null;
    }
    
    /**
     * This method returns the larger player-team of Team1 and Team2.
     *
     * @return (Team) the larger team
     */
    public Team getLargerPlayerTeam() {
        if (team1.getMembers().size() < team2.getMembers().size()) {
            return team2;
        } else if (team1.getMembers().size() > team2.getMembers().size()) {
            return team1;
        }
        
        return null;
    }
    
    private boolean isSamePlayerTeamSize() {
        return (team1.getMembers().size() == team2.getMembers().size());
    }
    
    public boolean hasEmptyPlayerTeam() {
        return ((team1.getMembers().isEmpty()) || (team2.getMembers().isEmpty()));
    }
    
    /**
     * This method checks whether a team switch would be fair based on 
     * the new team size. If no empty team results or if the team size 
     * difference does not exceed a certain value, the switch is 
     * considered acceptable.
     *
     * @param currentTeam the current team of the player
     * @param targetTeam the desired team
     * @return (boolean) 'true' if it's a fair team switch
     */
    public boolean isValidFairSwitch(Team currentTeam, Team targetTeam) {
        if (targetTeam.getTeamType() == TeamType.SPECTATOR) return true;
        
        // Prevention of an empty team in some cases.
        // (This should only be relevant if the method is also queried in the lobby.)
        if (!targetTeam.getMembers().isEmpty()) {
            if ((currentTeam.getTeamType() == TeamType.PLAYER) && (currentTeam.getMembers().size() == 1)) {
                Logger.DEBUG.log("Prevent team switch! Current player-team size: " + currentTeam.getMembers().size() 
                        + "; target player-team size: " + targetTeam.getMembers().size());
                return false;
            }
            if ((currentTeam.getTeamType() == TeamType.SPECTATOR) && (targetTeam.getEnemyTeam().getMembers().isEmpty())) {
                Logger.DEBUG.log("Prevent team switch! Target player-team size: " + targetTeam.getMembers().size() 
                        + "; enemy player-team size: " + targetTeam.getEnemyTeam().getMembers().size());
                return false;
            }
        }
        
        if (targetTeam == getSmallerPlayerTeam()) return true;
        
        // The person change is "pre-simulated" here (thus working with the new team sizes) 
        // to take into account the negative exponential influence of one player in relation 
        // to the team size.
        
        // prospective team sizes:
        int newTargetTeamSize = targetTeam.getMembers().size() + 1;
        int newEnemyTeamSize = targetTeam.getEnemyTeam().getMembers().size();
        if (currentTeam.getTeamType() == TeamType.PLAYER) newEnemyTeamSize = newEnemyTeamSize - 1;
        
        int diff = Math.abs(newTargetTeamSize - newEnemyTeamSize);
        
        // max team difference: XX% of target team size, minimal value = 1
        double maxDiff = Math.max(Math.max(newTargetTeamSize, newEnemyTeamSize) * 0.45, 1);
        
        if (diff <= maxDiff) return true;
        
        Logger.DEBUG.log("Prevent team switch! Max team difference: " + maxDiff + "; current difference: " + diff);
        return false;
    }
    
    public boolean hasBalancedTeamSizes() {
        if (hasEmptyPlayerTeam()) return false;
        
        int diff = Math.abs(team1.getMembers().size() - team2.getMembers().size());
        
        // max team difference: XX% of target team size, minimal value = 1
        double maxDiff = Math.max(Math.max(team1.getMembers().size(), team2.getMembers().size()) * 0.35, 1);
        
        if (diff <= maxDiff) return true;
        
        Logger.DEBUG.log("Prevent game start! Max team difference: " + maxDiff + "; current difference: " + diff);
        return false;
    }
}
