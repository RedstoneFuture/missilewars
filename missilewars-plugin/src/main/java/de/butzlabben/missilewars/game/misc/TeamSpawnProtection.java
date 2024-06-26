package de.butzlabben.missilewars.game.misc;

import de.butzlabben.missilewars.game.Team;
import de.butzlabben.missilewars.game.enums.TeamType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class TeamSpawnProtection {

    /**
     * This method regenerates the player-team spawn by resetting the ground and 
     * the area and replacing all previous blocks at this point.
     * 
     * @param targetTeam (Team) the target player-team
     */
    public static void regenerateSpawn(Team targetTeam) {
        if (targetTeam.getTeamType() == TeamType.SPECTATOR) return;

        Location teamSpawn = targetTeam.getSpawn();
        
        // floor:
        fill(teamSpawn, -1, -1, -1, 1, -1, 1, Material.BEDROCK);
        
        // AIR area:
        fill(teamSpawn, -1, 0, -1, 1, 2, 1, Material.AIR);
        
    }
    
    private static void fill(Location basisPos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Material material) {

        World targetWorld = basisPos.getWorld();
        Location targetLoc;
        
        if (targetWorld == null) return;
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    
                    targetLoc = basisPos.clone().add(x, y, z);
                    targetWorld.getBlockAt(targetLoc).setType(material);
                }
            }
        }
    }
    
}
