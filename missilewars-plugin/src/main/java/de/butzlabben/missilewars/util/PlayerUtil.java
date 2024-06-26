package de.butzlabben.missilewars.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerUtil {

    /**
     * This method sends the desired message above the player's action bar.
     * 
     * @param player (Player) the target player
     * @param message (String) the actionbar message
     */
    public static void sendActionbarMsg(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * This method teleports the player to the specified location. Before the teleport, however, 
     * the velocity is set to zero so that the player does not take over any fall damage due to 
     * the previous fall / fly. This is mainly relevant in the 'Survival-Mode'.
     * 
     * @param player (Player) the target player
     * @param targetLocation (Location) the target teleport-location
     */
    public static void teleportSafely(Player player, Location targetLocation) {
        player.setVelocity(new Vector(0, 0, 0));
        player.teleport(targetLocation);
    }
    
}
