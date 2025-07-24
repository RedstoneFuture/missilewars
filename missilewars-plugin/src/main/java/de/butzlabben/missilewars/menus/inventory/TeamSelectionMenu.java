package de.butzlabben.missilewars.menus.inventory;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.TeamManager;
import de.butzlabben.missilewars.menus.MenuItem;
import de.butzlabben.missilewars.menus.MenuUtils;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TeamSelectionMenu {
    
    private final MWPlayer mwPlayer;
    private final Game game;
    private final TeamManager teamManager;
    private final MenuUtils menuUtils;
    ChestGui gui;
    
    ItemStack item1, item2, itemSpec;
    ItemStack item1Enchanted, item2Enchanted, itemSpecEnchanted;
    
    // A replacement item in the event that the selection is deactivated.
    ItemStack deactivatedItem;
    
    OutlinePane pane1, pane2, paneSpec;
    
    public TeamSelectionMenu(MWPlayer mwPlayer) {
        this.mwPlayer = mwPlayer;
        this.game = mwPlayer.getGame();
        this.teamManager = game.getTeamManager();
        this.menuUtils = new MenuUtils(game);
        
        initialItems();
    }
    
    public void initialItems() {
        
        deactivatedItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        MenuItem.hideMetaValues(deactivatedItem);
        
        if (mwPlayer.getPlayer().hasPermission("mw.change.team.player")) {
            item1 = teamManager.getTeam1().getMenuItem();
            item2 = teamManager.getTeam2().getMenuItem();
        } else {
            item1 = deactivatedItem.clone();
            item2 = deactivatedItem.clone();
        }
        
        if (mwPlayer.getPlayer().hasPermission("mw.change.team.spectator")) {
            itemSpec = teamManager.getTeamSpec().getMenuItem();
        } else {
            itemSpec = deactivatedItem.clone();
        }
        
        item1Enchanted = item1.clone();
        item2Enchanted = item2.clone();
        itemSpecEnchanted = itemSpec.clone();
        
        MenuItem.setEnchantment(item1Enchanted);
        MenuItem.setEnchantment(item2Enchanted);
        MenuItem.setEnchantment(itemSpecEnchanted);
    }
    
    public void openMenu() {
        gui = new ChestGui(3, getTitle());
        
        pane1 = new OutlinePane(2, 1, 1, 1);
        pane2 = new OutlinePane(6, 1, 1, 1);
        paneSpec = new OutlinePane(4, 1, 1, 1);
        
        updateGuiItems(mwPlayer);
        
        pane1.setOnClick(event -> {
            // prevent spam with the event handling
            if (menuUtils.isInteractDelay(mwPlayer, event)) return;
            menuUtils.setInteractDelay(mwPlayer.getPlayer());
            
            mwPlayer.getPlayer().performCommand("mw change 1");
            
            updateGuiItems(mwPlayer);
            gui.update();
        });
        
        pane2.setOnClick(event -> {
            // prevent spam with the event handling
            if (menuUtils.isInteractDelay(mwPlayer, event)) return;
            menuUtils.setInteractDelay(mwPlayer.getPlayer());
            
            mwPlayer.getPlayer().performCommand("mw change 2");
            
            updateGuiItems(mwPlayer);
            gui.update();
        });
        
        paneSpec.setOnClick(event -> {
            // prevent spam with the event handling
            if (menuUtils.isInteractDelay(mwPlayer, event)) return;
            menuUtils.setInteractDelay(mwPlayer.getPlayer());
            
            mwPlayer.getPlayer().performCommand("mw change spec");
            
            updateGuiItems(mwPlayer);
            gui.update();
        });
        
        
        gui.addPane(pane1);
        gui.addPane(pane2);
        gui.addPane(paneSpec);
        
        gui.show(mwPlayer.getPlayer());
    }
    
    public static String getTitle() {
        return Config.getTeamSelectionMenuTitle();
    }
    
    private void updateGuiItems(MWPlayer mwPlayer) {

        if (!pane1.getItems().isEmpty()) pane1.removeItem(pane1.getItems().get(0));
        if (!pane2.getItems().isEmpty()) pane2.removeItem(pane2.getItems().get(0));
        if (!paneSpec.getItems().isEmpty()) paneSpec.removeItem(paneSpec.getItems().get(0));
        
        if (mwPlayer.getTeam() == teamManager.getTeam1()) {
            pane1.addItem(new GuiItem(item1Enchanted));
            pane2.addItem(new GuiItem(item2));
            paneSpec.addItem(new GuiItem(itemSpec));
        } else if (mwPlayer.getTeam() == teamManager.getTeam2()) {
            pane1.addItem(new GuiItem(item1));
            pane2.addItem(new GuiItem(item2Enchanted));
            paneSpec.addItem(new GuiItem(itemSpec));
        } else if (mwPlayer.getTeam() == teamManager.getTeamSpec()) {
            pane1.addItem(new GuiItem(item1));
            pane2.addItem(new GuiItem(item2));
            paneSpec.addItem(new GuiItem(itemSpecEnchanted));
        }
    }
    
}
