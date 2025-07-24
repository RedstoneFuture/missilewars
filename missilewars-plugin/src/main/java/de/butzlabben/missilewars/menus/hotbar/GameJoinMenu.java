package de.butzlabben.missilewars.menus.hotbar;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.menus.MenuItem;
import de.butzlabben.missilewars.player.MWPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class GameJoinMenu {


    private final MWPlayer mwPlayer;
    @SuppressWarnings("unused")
    private final Game game;
    
    // configured hotbar items:
    @Getter @Setter public static Map<Integer, Map<Integer, MenuItem>> menuItems;
    
    // finale hotbar items based of the current requirement-check:
    @Getter public Map<Integer, MenuItem> finalMenuItems = new HashMap<>();
    
    public GameJoinMenu(MWPlayer mwPlayer) {
        this.mwPlayer = mwPlayer;
        this.game = mwPlayer.getGame();
    }
    
    public void getMenu() {
        if (finalMenuItems != null) finalMenuItems.clear();
        
        for (Map<Integer, MenuItem> itemsPerSlot : menuItems.values()) {

            // Convert the keys into a sorted list to sort the priority values:
            List<Integer> priorityList = new ArrayList<>(itemsPerSlot.keySet());
            priorityList.sort(Collections.reverseOrder());


            // Iterate over the sorted priority values from the biggest to the lowest and check the requirements:
            for (Integer priority : priorityList) {

                MenuItem item = itemsPerSlot.get(priority);
                
                if (item.getItemRequirement().isRequirementsGiven(mwPlayer)) {
                    // The requirements are fulfilled. Send the final item to the player inventory:
                    item.sendToPlayer(mwPlayer);
                    finalMenuItems.put(item.getSlot(), item);
                    Logger.DEBUG.log("GameJoinMenu: - Slot " + item.getSlot() + ": Item with priority '" + priority 
                            + "' was added to the inventory menu of " + mwPlayer.getPlayer().getName());
                    break;
                }
            }
            
        }
    }
    
}


