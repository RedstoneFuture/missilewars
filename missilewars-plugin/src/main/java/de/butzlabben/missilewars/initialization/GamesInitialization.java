package de.butzlabben.missilewars.initialization;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.displays.signs.SignUpdateRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GamesInitialization {
    
    public static void initialize() {
        
        // TODO Ref
        Arenas.load();
        GameManager.getInstance().loadGamesOnStartup();
        
        GameManager.getInstance().getGames().values().forEach(game -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!game.isIn(player.getLocation())) continue;
                game.teleportToLobbySpawn(player);
            }
        });
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(MissileWars.getInstance(), new SignUpdateRunnable(), 20, 20 * 10);
        
    }
    
}