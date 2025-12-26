package de.butzlabben.missilewars.menus.inventory;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.component.PercentageBar;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.arena.ArenaConfig;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.menus.MenuItem;
import de.butzlabben.missilewars.menus.MenuUtils;
import de.butzlabben.missilewars.player.MWPlayer;
import de.redstoneworld.redutilities.items.HeadHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MapVoteMenu {
    
    private final Map<String, ArenaConfig> arenaDisplayNames = new HashMap<>();
    
    private final MWPlayer mwPlayer;
    private final Game game;
    private final MenuUtils menuUtils;
    ChestGui gui;
    
    PaginatedPane paginatedPane;
    
    OutlinePane backwards;
    OutlinePane forwards;
    GuiItem backwardsItem;
    GuiItem forwardsItem;
    
    final ItemStack backwardsItemActive = HeadHelper.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDliMmJlZTM5YjZlZjQ3ZTE4MmQ2ZjFkY2E5ZGVhODQyZmNkNjhiZGE5YmFjYzZhNmQ2NmE4ZGNkZjNlYyJ9fX0=");
    final ItemStack backwardsItemInactive = HeadHelper.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQyZmRlOGI4MmU4YzFiOGMyMmIyMjY3OTk4M2ZlMzVjYjc2YTc5Nzc4NDI5YmRhZGFiYzM5N2ZkMTUwNjEifX19");
    final ItemStack forwardsItemActive = HeadHelper.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQxZmY2YmM2N2E0ODEyMzJkMmU2NjllNDNjNGYwODdmOWQyMzA2NjY1YjRmODI5ZmI4Njg5MmQxM2I3MGNhIn19fQ==");
    final ItemStack forwardsItemInactive = HeadHelper.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA2MjYyYWYxZDVmNDE0YzU5NzA1NWMyMmUzOWNjZTE0OGU1ZWRiZWM0NTU1OWEyZDZiODhjOGQ2N2I5MmVhNiJ9fX0=");
    
    public MapVoteMenu(MWPlayer mwPlayer) {
        this.mwPlayer = mwPlayer;
        this.game = mwPlayer.getGame();
        this.menuUtils = new MenuUtils(game);
        
        MenuItem.setDisplayName(backwardsItemActive, Config.MapVoteMenuItems.BACKWARDS_ITEM_ACTIVE.getMessage());
        MenuItem.setDisplayName(backwardsItemInactive, Config.MapVoteMenuItems.BACKWARDS_ITEM_INACTIVE.getMessage());
        MenuItem.setDisplayName(forwardsItemActive, Config.MapVoteMenuItems.FORWARDS_ITEM_ACTIVE.getMessage());
        MenuItem.setDisplayName(forwardsItemInactive, Config.MapVoteMenuItems.FORWARDS_ITEM_INACTIVE.getMessage());
        
        for (ArenaConfig arenaConfig : game.getGameConfig().getArenas()) {
            arenaDisplayNames.put(arenaConfig.getDisplayName(), arenaConfig);
        }
        
        gui = new ChestGui(6, getTitle());
        paginatedPane = new PaginatedPane(0, 0, 9, 6);
        
        gui.addPane(paginatedPane);
    }
    
    public void openMenu() {
        updateGuiItems();
        gui.show(mwPlayer.getPlayer());
    }
    
    public static String getTitle() {
        return Config.getMapVoteMenuTitle();
    }
    
    private void updateGuiItems() {
        
        backwards = new OutlinePane(3, 5, 1, 1);
        forwards = new OutlinePane(5, 5, 1, 1);
        
        int maxPages = (int) Math.ceil(game.getGameConfig().getArenas().size() / 5d);
        int offset = 0;
        for (int page = 1; page <= maxPages; page++) {
            
            // vertical arena item list for vote:
            OutlinePane arenas = new OutlinePane(0, 0, 1, 5);
            
            PercentageBar voteResultBar;
            
            for (int n = 1; n <= 5; n++) {
                
                // Are there any other arenas?
                int nextArenaId = (offset * 5) + n - 1;
                if (game.getGameConfig().getArenas().size() < (nextArenaId + 1)) break;
                
                ArenaConfig arenaConfig = game.getGameConfig().getArenas().get(nextArenaId);
                
                // arena item:
                ItemStack item = new ItemStack(Material.valueOf(arenaConfig.getDisplayMaterial().toUpperCase()));
                MenuItem.hideMetaValues(item);
                MenuItem.setDisplayName(item, Config.MapVoteMenuItems.MAP_ITEM.getMessage()
                        .replace("{arena-name}", arenaConfig.getDisplayName()));
                if (game.getMapVoting().isVotedMapOfPlayer(arenaConfig, mwPlayer)) MenuItem.setEnchantment(item);
                
                arenas.addItem(new GuiItem(item));
                
                // vote percent display
                voteResultBar = new PercentageBar(1, n - 1, 8, 1);
                voteResultBar.setPercentage((float) (game.getMapVoting().getPercentOf(arenaConfig) / 100));
                
                ItemStack impactDisplayItem = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
                MenuItem.hideMetaValues(item);
                MenuItem.setDisplayName(impactDisplayItem, Config.MapVoteMenuItems.VOTE_RESULT_BAR.getMessage()
                        .replace("{vote-percent}", game.getMapVoting().getPercentOfMsg(arenaConfig)));
                voteResultBar.setFillItem(new GuiItem(impactDisplayItem));
                
                ItemStack backgroundItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                MenuItem.hideMetaValues(backgroundItem);
                voteResultBar.setBackgroundItem(new GuiItem(backgroundItem));
                
                paginatedPane.addPane(page - 1, voteResultBar);
            }
            
            
            arenas.setOnClick(event -> {
                event.setCancelled(true);
                
                // prevent spam with the event handling
                if (menuUtils.isInteractDelay(mwPlayer, event)) return;
                menuUtils.setInteractDelay(mwPlayer.getPlayer());
                
                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                String arenaName = arenaDisplayNames.get(itemName).getName();
                mwPlayer.getPlayer().performCommand("mw vote " + arenaName);
                
                updateGuiForAllPlayer();
            });
            
            backwards.setOnClick(event -> {
                event.setCancelled(true);
                
                if (isFirstPage()) return;
                
                paginatedPane.setPage(paginatedPane.getPage() - 1);
                backwardsItem.setItem(getBackwardsItem());
                forwardsItem.setItem(getForwardsItem());
                gui.update();
            });
            
            forwards.setOnClick(event -> {
                event.setCancelled(true);
                
                if (isLastPage()) return;
                
                paginatedPane.setPage(paginatedPane.getPage() + 1);
                backwardsItem.setItem(getBackwardsItem());
                forwardsItem.setItem(getForwardsItem());
                gui.update();
            });
            
            
            paginatedPane.addPane(page - 1, arenas);
            paginatedPane.addPane(page - 1, backwards);
            paginatedPane.addPane(page - 1, forwards);
            offset++;
        }
        
        backwardsItem = new GuiItem(getBackwardsItem());
        forwardsItem = new GuiItem(getForwardsItem());
        
        backwards.addItem(backwardsItem);
        forwards.addItem(forwardsItem);
        
        gui.update();
        
    }
    
    private void updateGuiForAllPlayer() {
        // Update the GUI for all players looking at it:
        game.getPlayers().forEach((uuid, mwPlayer1) -> mwPlayer1.getMapVoteMenu().updateGuiItems());
    }
    
    private boolean isFirstPage() {
        return (paginatedPane.getPage() == 0);
    }
    
    private boolean isLastPage() {
        return ((paginatedPane.getPage() + 1) >= paginatedPane.getPages());
    }
    
    private ItemStack getBackwardsItem() {
        if (isFirstPage()) {
            return backwardsItemInactive;
        } else {
            return backwardsItemActive;
        }
    }
    
    private ItemStack getForwardsItem() {
        if (isLastPage()) {
            return forwardsItemInactive;
        } else {
            return forwardsItemActive;
        }
    }
}