package de.butzlabben.missilewars.menus;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuUtils {
    
    private final Game game;

    public MenuUtils(Game game) {
        this.game = game;
    }
    
    public Game getGame() {
        return game;
    }
    
    /**
     * This method gets the interaction protection variable for a player.
     *
     * @param player (Player) the target player
     */
    public boolean isInteractDelay(Player player) {
        MWPlayer mwPlayer = getGame().getPlayer(player);
        if (mwPlayer == null) return false;

        return mwPlayer.isPlayerInteractEventCancel();
    }
    
    public boolean isInteractDelay(MWPlayer mwPlayer, InventoryClickEvent event) {
        if (isInteractDelay(mwPlayer.getPlayer())) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    /**
     * This method sets an interaction protection variable for a player for
     * a short time.
     *
     * @param player (Player) the target player
     */
    public void setInteractDelay(Player player) {
        MWPlayer mwPlayer = getGame().getPlayer(player);
        if (mwPlayer == null) return;

        mwPlayer.setPlayerInteractEventCancel(true);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> mwPlayer.setPlayerInteractEventCancel(false), 10);
    }
}
